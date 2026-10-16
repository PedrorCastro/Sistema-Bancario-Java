package org.pedro;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class MenuConsole implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        Scanner sc = new Scanner(System.in);
        Conta contaCriada;

        System.out.println("Qual o seu nome: ");
        String nome = sc.nextLine();

        System.out.println("Olá, " + nome + "!");
        System.out.println("Qual o seu CPF: ");
        String cpf = sc.nextLine();
        System.out.println("Qual o seu email: ");
        String email = sc.nextLine();
        Cliente p = new Cliente(nome, cpf, email);

        System.out.println("Qual conta você quer criar?");
        System.out.println("1. Conta Corrente");
        System.out.println("2. Conta Poupanca");
        String c = sc.nextLine();

        switch (c) {
            case "1":
                contaCriada = new ContaCorrente(1, p, 5000);
                break;
            case "2":
            contaCriada = new ContaPoupanca(2, p);
            break;
            case null, default:
                throw new Exception("Erro: " + c);
        }

        while (true) {

            System.out.println("=== SISTEMA BANCARIO ===");
            System.out.println("Digite uma operação: ");
            System.out.println("1. Depositar");
            System.out.println("2. Sacar");
            System.out.println("3. Ver extrato");
            System.out.println("0. Sair");
            String op = sc.nextLine();

            switch (op) {
                case "1":
                    System.out.println("Digite o valor a ser depositado: ");
                    double valor = sc.nextDouble();
                    sc.nextLine();
                    try {
                        contaCriada.depositar(valor);
                    } catch (ValorInvalidoException e) {
                        System.out.println("Erro: " + e.getMessage());
                    }
                    break;
                case "2":
                    System.out.println("Digite o valor a ser sacado: ");
                    double valor2 = sc.nextDouble();
                    sc.nextLine();
                    try {
                        contaCriada.sacar(valor2);
                    } catch (SaldoInsuficienteException e) {
                        System.out.println("Erro: " + e.getMessage());
                    } catch (ValorInvalidoException e) {
                        System.out.println("Erro: " + e.getMessage());
                    }
                    break;
                case "3":
                    System.out.println("Extrato: ");
                    for (Transacao t : contaCriada.getHistorico()) {
                        System.out.println(t);
                    }
                    break;
                case "0":
                    System.exit(0);
                    break;
                case null, default:
                    throw new IllegalArgumentException("Tipo de transação inválido: " + op);
            }
        }
    }
}
