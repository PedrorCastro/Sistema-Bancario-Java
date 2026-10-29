package org.pedro.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import org.pedro.exception.SaldoInsuficienteException;
import org.pedro.exception.ValorInvalidoException;
import org.pedro.model.Cliente;
import org.pedro.model.Conta;
import org.pedro.model.ContaCorrente;
import org.pedro.model.ContaPoupanca;
import org.pedro.model.Transacao;
import org.pedro.service.ClienteService;
import org.pedro.service.ContaService;
import org.pedro.service.TransacaoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * [ALTERADO]
 * Mudanças:
 * 1. Implementado fluxo completo de autenticação e consulta de contas existentes no banco de dados.
 * 2. Menu Inicial desacoplado:
 *    - Opção 1: Acessar conta existente (Login via CPF ou Número de Conta diretamente do banco).
 *    - Opção 2: Criar nova conta (com reconhecimento de cliente já existente ou cadastro de novo).
 *    - Opção 0: Sair.
 * 3. Suporte a múltiplas contas por titular: se o cliente tiver mais de uma conta no banco, o sistema lista
 *    todas e permite escolher qual operar.
 * 4. Possibilidade de informar o número da conta desejado ou gerar automaticamente.
 * 5. Código modularizado em métodos especializados (acessarConta, cadastrarNovaConta, menuOperacoes).
 */
@Component
@Profile("!test")
public class MenuConsole implements CommandLineRunner {

    private final ClienteService clienteService;
    private final ContaService contaService;
    private final TransacaoService transacaoService;

    public MenuConsole(ClienteService clienteService, ContaService contaService, TransacaoService transacaoService) {
        this.clienteService = clienteService;
        this.contaService = contaService;
        this.transacaoService = transacaoService;
    }

    @Override
    public void run(String... args) throws Exception {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n=========================================");
            System.out.println("    BEM-VINDO AO SISTEMA BANCÁRIO      ");
            System.out.println("=========================================");
            System.out.println("1. Acessar conta existente (Já sou cliente)");
            System.out.println("2. Criar nova conta bancária");
            System.out.println("0. Sair do sistema");
            System.out.print("Escolha uma opção: ");
            String opcaoInicial = sc.nextLine().trim();

            Conta contaAtiva = null;

            switch (opcaoInicial) {
                case "1":
                    contaAtiva = acessarConta(sc);
                    break;
                case "2":
                    contaAtiva = cadastrarNovaConta(sc);
                    break;
                case "0":
                    System.out.println("\nObrigado por utilizar o Sistema Bancário. Até logo!");
                    return;
                default:
                    System.out.println("Opção inválida! Digite 1, 2 ou 0.");
            }

            // Se o usuário selecionou ou criou uma conta com sucesso, entra no menu financeiro
            if (contaAtiva != null) {
                menuOperacoes(sc, contaAtiva);
            }
        }
    }

    /**
     * Fluxo de Login: Lê do banco de dados por Número da Conta ou por CPF.
     */
    private Conta acessarConta(Scanner sc) {
        System.out.println("\n--- ACESSAR CONTA EXISTENTE ---");
        System.out.print("Digite o número da sua conta ou seu CPF: ");
        String identificador = sc.nextLine().trim();

        if (identificador.isEmpty()) {
            System.out.println("Identificador não pode ser vazio.");
            return null;
        }

        // Tentativa 1: Verificar se foi digitado um número de conta
        try {
            int numeroConta = Integer.parseInt(identificador);
            Optional<Conta> contaPorNumero = contaService.buscarPorNumero(numeroConta);
            if (contaPorNumero.isPresent()) {
                Conta c = contaPorNumero.get();
                System.out.println("\nConta localizada com sucesso!");
                System.out.println("Titular: " + (c.getTitular() != null ? c.getTitular().getNome() : "Não informado"));
                return c;
            }
        } catch (NumberFormatException e) {
            // Não é um número inteiro, prossegue para busca por CPF
        }

        // Tentativa 2: Buscar pelo CPF do cliente
        Optional<Cliente> clienteOpt = clienteService.buscarPorCpf(identificador);
        if (clienteOpt.isEmpty()) {
            System.out.println("Nenhum cliente ou conta foi encontrado com o dado informado: " + identificador);
            return null;
        }

        Cliente cliente = clienteOpt.get();
        System.out.println("\nOlá, " + cliente.getNome() + "!");

        List<Conta> contasDoCliente = contaService.listarPorCliente(cliente);
        if (contasDoCliente.isEmpty()) {
            System.out.println("Você está cadastrado, mas ainda não possui nenhuma conta bancária aberta.");
            return null;
        }

        // Se tiver apenas 1 conta, seleciona automaticamente
        if (contasDoCliente.size() == 1) {
            Conta contaUnica = contasDoCliente.get(0);
            System.out.println("Acessando sua conta " + contaUnica.getClass().getSimpleName() + " Nº " + contaUnica.getNumero());
            return contaUnica;
        }

        // Se tiver mais de uma conta vinculada ao mesmo CPF, permite escolher
        System.out.println("Você possui " + contasDoCliente.size() + " contas vinculadas:");
        for (int i = 0; i < contasDoCliente.size(); i++) {
            Conta c = contasDoCliente.get(i);
            System.out.println((i + 1) + ". " + c.getClass().getSimpleName() + " Nº " + c.getNumero() + " | Saldo: R$ " + consultarSaldoAtual(c.getId()));
        }

        while (true) {
            System.out.print("Escolha o número da conta que deseja acessar (1 a " + contasDoCliente.size() + ") ou 0 para cancelar: ");
            String escolhaStr = sc.nextLine().trim();
            try {
                int escolha = Integer.parseInt(escolhaStr);
                if (escolha == 0) return null;
                if (escolha >= 1 && escolha <= contasDoCliente.size()) {
                    return contasDoCliente.get(escolha - 1);
                }
                System.out.println("Opção fora da faixa.");
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida.");
            }
        }
    }

    /**
     * Fluxo de Cadastro: Reconhece se o cliente já existe para não duplicar dados pessoais.
     */
    private Conta cadastrarNovaConta(Scanner sc) {
        System.out.println("\n--- ABERTURA DE CONTA BANCÁRIA ---");
        System.out.print("Digite o seu CPF: ");
        String cpf = sc.nextLine().trim();

        if (cpf.isEmpty()) {
            System.out.println("CPF não pode ser vazio.");
            return null;
        }

        Cliente cliente;
        Optional<Cliente> clienteExistente = clienteService.buscarPorCpf(cpf);

        if (clienteExistente.isPresent()) {
            cliente = clienteExistente.get();
            System.out.println("Cadastro localizado: Olá novamente, " + cliente.getNome() + "!");
        } else {
            System.out.print("Digite seu nome completo: ");
            String nome = sc.nextLine().trim();
            System.out.print("Digite seu e-mail: ");
            String email = sc.nextLine().trim();

            try {
                cliente = clienteService.cadastrarCliente(new Cliente(nome, cpf, email));
                System.out.println("Cliente cadastrado no banco com sucesso!");
            } catch (Exception e) {
                System.out.println("Erro ao cadastrar cliente: " + e.getMessage());
                return null;
            }
        }

        // Escolha do tipo de conta
        Conta novaConta = null;
        while (novaConta == null) {
            System.out.println("\nQual tipo de conta deseja abrir?");
            System.out.println("1. Conta Corrente (com limite especial de R$ 5.000)");
            System.out.println("2. Conta Poupança (sem limite)");
            System.out.print("Escolha (1 ou 2): ");
            String tipoConta = sc.nextLine().trim();

            if (!tipoConta.equals("1") && !tipoConta.equals("2")) {
                System.out.println("Opção inválida! Escolha 1 ou 2.");
                continue;
            }

            // Definição do número da conta
            int numeroConta = definirNumeroConta(sc);

            try {
                if (tipoConta.equals("1")) {
                    novaConta = contaService.criarConta(new ContaCorrente(numeroConta, cliente, 5000.00));
                    System.out.println("Conta Corrente nº " + numeroConta + " aberta com sucesso!");
                } else {
                    novaConta = contaService.criarConta(new ContaPoupanca(numeroConta, cliente));
                    System.out.println("Conta Poupança nº " + numeroConta + " aberta com sucesso!");
                }
            } catch (Exception e) {
                System.out.println("Erro ao criar conta: " + e.getMessage());
                return null;
            }
        }

        return novaConta;
    }

    /**
     * Permite que o cliente digite o número da conta desejado ou gere um automático.
     */
    private int definirNumeroConta(Scanner sc) {
        while (true) {
            System.out.print("Digite o número desejado para sua conta (ou pressione ENTER para gerar automaticamente): ");
            String entrada = sc.nextLine().trim();

            if (entrada.isEmpty()) {
                // Gera número semi-aleatório de 5 dígitos
                int gerado = (int) (10000 + (System.currentTimeMillis() % 90000));
                if (contaService.buscarPorNumero(gerado).isEmpty()) {
                    System.out.println("Número gerado automaticamente: " + gerado);
                    return gerado;
                }
            } else {
                try {
                    int numero = Integer.parseInt(entrada);
                    if (numero <= 0) {
                        System.out.println("O número da conta deve ser positivo.");
                        continue;
                    }
                    if (contaService.buscarPorNumero(numero).isPresent()) {
                        System.out.println("O número " + numero + " já está em uso por outro cliente. Escolha outro.");
                        continue;
                    }
                    return numero;
                } catch (NumberFormatException e) {
                    System.out.println("Número inválido! Digite apenas algarismos.");
                }
            }
        }
    }

    /**
     * Menu Financeiro da Conta Ativa.
     */
    private void menuOperacoes(Scanner sc, Conta conta) {
        boolean ativa = true;

        while (ativa) {
            Long saldoAtual = consultarSaldoAtual(conta.getId());
            String tipoNome = conta.getClass().getSimpleName();

            System.out.println("\n-----------------------------------------");
            System.out.println("  CONTA: " + tipoNome + " Nº " + conta.getNumero());
            System.out.println("  TITULAR: " + (conta.getTitular() != null ? conta.getTitular().getNome() : "Desconhecido"));
            System.out.println("  SALDO ATUAL: R$ " + saldoAtual);
            if (conta instanceof ContaCorrente cc) {
                System.out.println("  LIMITE DE CRÉDITO: R$ " + cc.getLimite());
                System.out.println("  DISPONÍVEL TOTAL: R$ " + (saldoAtual + cc.getLimite()));
            }
            System.out.println("-----------------------------------------");
            System.out.println("1. Depositar");
            System.out.println("2. Sacar");
            System.out.println("3. Transferir para outra conta");
            System.out.println("4. Ver extrato de transações");
            System.out.println("0. Voltar ao menu principal (Desconectar)");
            System.out.print("Digite a operação desejada: ");
            String op = sc.nextLine().trim();

            switch (op) {
                case "1":
                    Long valorDeposito = lerValorLong(sc, "Digite o valor a depositar: ");
                    if (valorDeposito != null) {
                        try {
                            transacaoService.depositar(conta.getId(), valorDeposito);
                            System.out.println("Depósito realizado com sucesso!");
                            System.out.println("Novo saldo: R$ " + consultarSaldoAtual(conta.getId()));
                        } catch (ValorInvalidoException e) {
                            System.out.println("Erro: " + e.getMessage());
                        }
                    }
                    break;

                case "2":
                    Long valorSaque = lerValorLong(sc, "Digite o valor a sacar: ");
                    if (valorSaque != null) {
                        try {
                            transacaoService.sacar(conta.getId(), valorSaque);
                            System.out.println("Saque realizado com sucesso!");
                            System.out.println("Novo saldo: R$ " + consultarSaldoAtual(conta.getId()));
                        } catch (SaldoInsuficienteException | ValorInvalidoException e) {
                            System.out.println("Erro no saque: " + e.getMessage());
                        }
                    }
                    break;

                case "3":
                    System.out.print("Digite o número da conta de destino: ");
                    String numDestinoStr = sc.nextLine().trim();
                    try {
                        int numDestino = Integer.parseInt(numDestinoStr);
                        Conta contaDestino = contaService.buscarPorNumero(numDestino).orElse(null);
                        if (contaDestino == null) {
                            System.out.println("Conta de destino número " + numDestino + " não encontrada no banco.");
                            break;
                        }
                        Long valorTransferencia = lerValorLong(sc, "Digite o valor a transferir: ");
                        if (valorTransferencia != null) {
                            transacaoService.transferir(conta.getId(), contaDestino.getId(), valorTransferencia);
                            System.out.println("Transferência realizada com sucesso!");
                            System.out.println("Novo saldo: R$ " + consultarSaldoAtual(conta.getId()));
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Número de conta inválido.");
                    } catch (Exception e) {
                        System.out.println("Erro na transferência: " + e.getMessage());
                    }
                    break;

                case "4":
                    System.out.println("\n--- EXTRATO BANCÁRIO (ÚLTIMOS 30 DIAS) ---");
                    LocalDateTime inicio = LocalDateTime.now().minusDays(30);
                    LocalDateTime fim = LocalDateTime.now().plusDays(1);
                    List<Transacao> extrato = transacaoService.gerarExtrato(conta.getId(), inicio, fim);
                    if (extrato.isEmpty()) {
                        System.out.println("Nenhuma transação registrada no período.");
                    } else {
                        for (Transacao t : extrato) {
                            System.out.println(t);
                        }
                    }
                    System.out.println("------------------------------------------");
                    break;

                case "0":
                    System.out.println("Desconectando da conta Nº " + conta.getNumero() + "...");
                    ativa = false;
                    break;

                default:
                    System.out.println("Opção inválida! Escolha uma das opções numéricas.");
            }
        }
    }

    private Long consultarSaldoAtual(Long contaId) {
        return contaService.buscarPorId(contaId)
                .map(Conta::getSaldo)
                .orElse(0L);
    }

    private Long lerValorLong(Scanner sc, String mensagem) {
        System.out.print(mensagem);
        String entrada = sc.nextLine().trim();
        try {
            long valor = Long.parseLong(entrada);
            if (valor <= 0) {
                System.out.println("Erro: o valor deve ser maior que zero.");
                return null;
            }
            return valor;
        } catch (NumberFormatException e) {
            System.out.println("Erro: digite um número inteiro válido.");
            return null;
        }
    }
}
