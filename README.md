# 🏦 Sistema Bancário em Java

Sistema bancário desenvolvido em **Java** com foco na aplicação prática dos principais conceitos de **Programação Orientada a Objetos (POO)**.

A aplicação simula operações bancárias básicas por meio de uma interface de linha de comando (CLI), permitindo criar um cliente, escolher o tipo de conta, realizar depósitos, saques e consultar o histórico de transações.

## 📌 Sobre o Projeto

O projeto foi desenvolvido com o objetivo de praticar conceitos fundamentais do desenvolvimento backend com Java, incluindo:

* Programação Orientada a Objetos
* Abstração
* Herança
* Encapsulamento
* Polimorfismo
* Classes abstratas
* Enumerações
* Tratamento de exceções
* Coleções com `List`
* Registro de transações
* Manipulação de datas com `LocalDateTime`
* Gerenciamento de dependências e build com Maven

A estrutura utiliza uma classe abstrata `Conta` como base para diferentes tipos de contas, como **Conta Corrente** e **Conta Poupança**.

---

## ⚙️ Funcionalidades

### 👤 Cadastro de cliente

Durante a execução, o sistema solicita:

* Nome
* CPF
* E-mail

Os dados são armazenados em um objeto `Cliente`, que também mantém a relação com suas contas bancárias.

### 🏦 Tipos de conta

O sistema atualmente possui dois tipos:

#### Conta Corrente

Possui um limite de crédito configurável para operações de saque.

O valor disponível para saque considera:

```text
Saldo atual + Limite da conta
```

#### Conta Poupança

Permite saques somente quando o cliente possui saldo suficiente na conta.

---

### 💰 Depósito

Permite adicionar valores ao saldo da conta.

O sistema realiza uma validação para impedir transações com valores menores ou iguais a zero. Cada depósito aprovado é registrado no histórico da conta.

### 💸 Saque

Permite retirar valores da conta respeitando as regras específicas de cada tipo de conta.

O sistema possui tratamento para:

* Valor inválido
* Saldo insuficiente

### 📄 Extrato

Todas as operações realizadas são armazenadas no histórico da conta.

Cada transação possui:

* Valor
* Tipo da operação
* Data e hora

Atualmente são suportados os tipos:

```text
DEPOSITO
SAQUE
```

---

## 🧠 Conceitos de POO Aplicados

### Abstração

A classe `Conta` é definida como uma classe abstrata e concentra atributos e comportamentos comuns às diferentes contas bancárias.

```java
public abstract class Conta {
    ...
    public abstract void sacar(double valor);
}
```

### Herança

As classes específicas reutilizam a estrutura da classe `Conta`:

```text
              Conta
                │
        ┌───────┴────────┐
        │                │
ContaCorrente      ContaPoupanca
```

### Encapsulamento

Os atributos das entidades são privados e acessados por métodos, mantendo o controle sobre o estado dos objetos.

Exemplo:

```java
private double saldo;

public double getSaldo() {
    return saldo;
}
```

A alteração do saldo é controlada pela própria classe `Conta` e por suas subclasses.

### Polimorfismo

O método `sacar()` é definido na classe abstrata `Conta` e implementado de maneira diferente em cada tipo de conta.

```java
@Override
public void sacar(double valor) {
    ...
}
```

Isso permite que diferentes contas possuam regras específicas para a mesma operação.

---

## 🛡️ Tratamento de Exceções

O projeto utiliza exceções específicas para representar situações inválidas durante as operações bancárias.

Entre os cenários tratados estão:

* Saldo insuficiente
* Valor de transação inválido

Exemplo de utilização:

```java
try {
    contaCriada.sacar(valor);
} catch (SaldoInsuficienteException e) {
    System.out.println("Erro: " + e.getMessage());
} catch (ValorInvalidoException e) {
    System.out.println("Erro: " + e.getMessage());
}
```

Essa abordagem evita que erros durante uma operação encerrem a aplicação de maneira inesperada.

---

## 📂 Estrutura do Projeto

```text
Sistema-Bancario-Java/
│
├── .idea/
│
├── src/
│   └── main/
│       └── java/
│           └── org/
│               └── pedro/
│                   ├── Cliente.java
│                   ├── Conta.java
│                   ├── ContaCorrente.java
│                   ├── ContaPoupanca.java
│                   ├── Transacao.java
│                   ├── TipoTransacao.java
│                   ├── SaldoInsuficienteException.java
│                   └── ValorInvalidoException.java
│
├── .gitignore
├── pom.xml
└── README.md
```

O repositório utiliza Maven e atualmente possui `pom.xml` configurado para compilação com **Java 25**.

---

## 🛠️ Tecnologias

| Tecnologia          | Utilização                             |
| ------------------- | -------------------------------------- |
| ☕ Java 25           | Desenvolvimento da aplicação           |
| 📦 Maven            | Gerenciamento e build do projeto       |
| 🧩 POO              | Modelagem das entidades e regras       |
| 📝 Java Collections | Armazenamento de contas e transações   |
| 🕐 LocalDateTime    | Registro de data e hora das transações |
| 💻 CLI              | Interface de interação com o usuário   |

---

## 🚀 Como Executar

### Pré-requisitos

Certifique-se de possuir instalado:

* **JDK 25**
* **Maven**
* IDE de sua preferência, como IntelliJ IDEA, Eclipse ou VS Code

### 1. Clone o repositório

```bash
git clone https://github.com/PedrorCastro/Sistema-Bancario-Java.git
```

### 2. Entre no diretório

```bash
cd Sistema-Bancario-Java
```

### 3. Compile o projeto

```bash
mvn clean compile
```

### 4. Execute a aplicação

A aplicação pode ser executada pela sua IDE através da classe:

```text
src/main/java/org/pedro/Main.java
```

---

## 🖥️ Exemplo de Utilização

Ao iniciar a aplicação, o usuário informa seus dados:

```text
Qual o seu nome:
Pedro

Olá, Pedro!

Qual o seu CPF:
000.000.000-00

Qual o seu email:
pedro@email.com

Qual conta você quer criar?
1. Conta Corrente
2. Conta Poupanca
```

Após criar a conta, o sistema apresenta o menu:

```text
=== SISTEMA BANCARIO ===

Digite uma operação:
1. Depositar
2. Sacar
3. Ver extrato
0. Sair
```

### Exemplo de extrato

```text
Extrato:

Transacao{
    valor=500.0,
    tipo=DEPOSITO,
    data=2026-09-30T12:00:00
}

Transacao{
    valor=100.0,
    tipo=SAQUE,
    data=2026-09-30T12:05:00
}
```

---

## 🔄 Fluxo da Aplicação

```text
                 ┌───────────────┐
                 │     Início    │
                 └───────┬───────┘
                         │
                         ▼
                ┌─────────────────┐
                │ Cadastro Cliente │
                └────────┬────────┘
                         │
                         ▼
                ┌─────────────────┐
                │ Escolha da Conta│
                └────────┬────────┘
                         │
              ┌──────────┴──────────┐
              ▼                     ▼
      ┌───────────────┐     ┌───────────────┐
      │ Conta Corrente│     │ Conta Poupança│
      └───────┬───────┘     └───────┬───────┘
              │                     │
              └──────────┬──────────┘
                         ▼
                 ┌──────────────┐
                 │ Menu Bancário│
                 └──────┬───────┘
                        │
             ┌──────────┼──────────┐
             ▼          ▼          ▼
         Depositar     Sacar     Extrato
```

---

## 🎯 Objetivos de Aprendizado

Este projeto faz parte do processo de desenvolvimento das minhas habilidades em **Java e Backend**, com foco em transformar conceitos teóricos de orientação a objetos em uma aplicação funcional.

Principais objetivos:

* Praticar modelagem de domínio
* Aplicar princípios de POO
* Trabalhar com herança e abstração
* Implementar regras de negócio
* Utilizar exceções personalizadas
* Trabalhar com coleções
* Registrar operações utilizando objetos de domínio
* Utilizar Maven em um projeto Java

---

## 🔮 Próximas Evoluções

Algumas funcionalidades que podem ser adicionadas futuramente:

* [ ] Transferência entre contas
* [ ] PIX
* [ ] Autenticação de usuários
* [ ] Persistência em banco de dados
* [ ] Integração com MySQL ou PostgreSQL
* [ ] API REST com Spring Boot
* [ ] Testes unitários com JUnit
* [ ] Documentação da API com Swagger/OpenAPI
* [ ] Validação de CPF e e-mail
* [ ] Histórico persistente de transações
* [ ] Containerização com Docker

---

## 👨‍💻 Autor

**Pedro Rodrigues de Castro Bezerra**

Estudante de Ciência da Computação | Desenvolvedor de Software Júnior

Foco em desenvolvimento **Backend com Java, Spring Boot, APIs REST e bancos de dados relacionais**.

### 🔗 Links

* GitHub: [PedrorCastro](https://github.com/PedrorCastro)
* Projeto: [Sistema-Bancario-Java](https://github.com/PedrorCastro/Sistema-Bancario-Java)

---

## 📄 Licença

Este projeto foi desenvolvido para fins educacionais e de portfólio.
