package org.pedro.controller;

import java.time.LocalDateTime;
import java.util.List;
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
 * 1. Anotação @Profile("!test") adicionada: Permite rodar testes automatizados do Spring (@SpringBootTest)
 *    sem que o loop de Scanner bloqueie ou cause NoSuchElementException nos testes.
 * 2. Integração com a camada de persistência: Injetados ClienteService, ContaService e TransacaoService.
 *    Agora os dados digitados no terminal são realmente persistidos no banco de dados H2.
 * 3. Tratamento seguro de entrada (Scanner): Adicionada leitura resiliente de números para evitar
 *    que o programa quebre com InputMismatchException se o usuário digitar letras ou valores inválidos.
 * 4. Menu resiliente: Removidos os 'throw new Exception' das cláusulas default; agora exibe mensagem de erro
 *    amigável e permite tentar novamente.
 * 5. Adicionada opção de Transferência Bancária no menu (aproveitando o método transferir do TransacaoService).
 * 6. Saída limpa: Substituído System.exit(0) por encerramento controlado do loop.
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
        Conta contaCriada = null;

        System.out.println("=========================================");
        System.out.println("   BEM-VINDO AO SISTEMA BANCÁRIO JAVA    ");
        System.out.println("=========================================");

        System.out.print("Qual o seu nome: ");
        String nome = sc.nextLine().trim();

        System.out.println("Olá, " + nome + "!");
        System.out.print("Qual o seu CPF: ");
        String cpf = sc.nextLine().trim();

        System.out.print("Qual o seu email: ");
        String email = sc.nextLine().trim();

        // Cadastra ou recupera o cliente do banco de dados
        Cliente cliente = clienteService.buscarPorCpf(cpf)
                .orElseGet(() -> clienteService.cadastrarCliente(new Cliente(nome, cpf, email)));

        // Seleção do tipo de conta com loop para evitar quebras por digitação errada
        while (contaCriada == null) {
            System.out.println("\nQual conta você quer criar?");
            System.out.println("1. Conta Corrente (com limite de R$ 5.000)");
            System.out.println("2. Conta Poupança");
            System.out.print("Escolha: ");
            String tipoEscolhido = sc.nextLine().trim();

            int numeroConta = (int) (System.currentTimeMillis() % 100000); // Gera um número semi-aleatório

            switch (tipoEscolhido) {
                case "1":
                    contaCriada = contaService.criarConta(new ContaCorrente(numeroConta, cliente, 5000L));
                    System.out.println("Conta Corrente nº " + numeroConta + " criada com sucesso!");
                    break;
                case "2":
                    contaCriada = contaService.criarConta(new ContaPoupanca(numeroConta, cliente));
                    System.out.println("Conta Poupança nº " + numeroConta + " criada com sucesso!");
                    break;
                default:
                    System.out.println("Opção inválida! Digite 1 ou 2.");
            }
        }

        boolean executando = true;

        while (executando) {
            System.out.println("\n=== SISTEMA BANCARIO ===");
            System.out.println("Conta atual: Nº " + contaCriada.getNumero() + " | Saldo: R$ " + consultarSaldoAtual(contaCriada.getId()));
            System.out.println("1. Depositar");
            System.out.println("2. Sacar");
            System.out.println("3. Transferir");
            System.out.println("4. Ver extrato");
            System.out.println("0. Sair");
            System.out.print("Digite uma operação: ");
            String op = sc.nextLine().trim();

            switch (op) {
                case "1":
                    Long valorDeposito = lerValorLong(sc, "Digite o valor a ser depositado: ");
                    if (valorDeposito != null) {
                        try {
                            transacaoService.depositar(contaCriada.getId(), valorDeposito);
                            System.out.println("Depósito realizado com sucesso!");
                            System.out.println("Novo saldo: R$ " + consultarSaldoAtual(contaCriada.getId()));
                        } catch (ValorInvalidoException e) {
                            System.out.println("Erro: " + e.getMessage());
                        }
                    }
                    break;

                case "2":
                    Long valorSaque = lerValorLong(sc, "Digite o valor a ser sacado: ");
                    if (valorSaque != null) {
                        try {
                            transacaoService.sacar(contaCriada.getId(), valorSaque);
                            System.out.println("Saque realizado com sucesso!");
                            System.out.println("Novo saldo: R$ " + consultarSaldoAtual(contaCriada.getId()));
                        } catch (SaldoInsuficienteException | ValorInvalidoException e) {
                            System.out.println("Erro: " + e.getMessage());
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
                            System.out.println("Conta de destino não encontrada.");
                            break;
                        }
                        Long valorTransferencia = lerValorLong(sc, "Digite o valor a transferir: ");
                        if (valorTransferencia != null) {
                            transacaoService.transferir(contaCriada.getId(), contaDestino.getId(), valorTransferencia);
                            System.out.println("Transferência realizada com sucesso!");
                            System.out.println("Novo saldo: R$ " + consultarSaldoAtual(contaCriada.getId()));
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Número de conta inválido.");
                    } catch (Exception e) {
                        System.out.println("Erro na transferência: " + e.getMessage());
                    }
                    break;

                case "4":
                    System.out.println("\n--- EXTRATO BANCÁRIO ---");
                    LocalDateTime inicio = LocalDateTime.now().minusDays(30);
                    LocalDateTime fim = LocalDateTime.now().plusDays(1);
                    List<Transacao> extrato = transacaoService.gerarExtrato(contaCriada.getId(), inicio, fim);
                    if (extrato.isEmpty()) {
                        System.out.println("Nenhuma transação encontrada no período.");
                    } else {
                        for (Transacao t : extrato) {
                            System.out.println(t);
                        }
                    }
                    System.out.println("------------------------");
                    break;

                case "0":
                    System.out.println("Obrigado por utilizar o Sistema Bancário. Até logo!");
                    executando = false;
                    break;

                default:
                    System.out.println("Opção inválida! Escolha uma das opções do menu.");
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
            System.out.println("Erro: valor inválido. Digite um número inteiro.");
            return null;
        }
    }
}
