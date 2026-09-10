import org.pedro.*;

import java.util.Scanner;


void main() throws SaldoInsuficienteException, ValorInvalidoException {

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

    if(c.equals("1")){
        contaCriada = new ContaCorrente(1, p, 5000);
    }
    else {
        contaCriada = new ContaPoupanca(2, p);
    }

    while(true){

        System.out.println("=== SISTEMA BANCARIO ===");
        System.out.println("Digite uma operação: ");
        System.out.println("1. Depositar");
        System.out.println("2. Sacar");
        System.out.println("3. Ver extrato");
        System.out.println("0. Sair");
        String op = sc.nextLine();

        switch (op){
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
                for (Transacao t : contaCriada.getHistorico()){
                    System.out.println(t);
                }
                break;
            case "0":
                System.exit(0);
        }
    }

}