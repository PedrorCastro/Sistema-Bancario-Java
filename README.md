# 🏦 Sistema Bancário em Java

Sistema bancário desenvolvido em **Java** com **Spring Boot**, demonstrando a aplicação prática dos principais conceitos de **Programação Orientada a Objetos (POO)** e a transição para uma arquitetura profissional em camadas com persistência em banco de dados relacional via **Spring Data JPA**.

A aplicação simula operações bancárias por meio de uma interface de linha de comando (CLI) interativa e resiliente, permitindo cadastrar clientes, abrir contas (Corrente ou Poupança), realizar depósitos, saques, transferências entre contas e consultar extratos com histórico completo de transações persistidas.

---

## 📌 Sobre o Projeto

O projeto foi desenvolvido para consolidar conceitos essenciais do ecossistema backend Java moderno:

* **Programação Orientada a Objetos (POO)**: Abstração, Herança, Encapsulamento e Polimorfismo.
* **Arquitetura em Camadas**: Separação clara de responsabilidades entre Modelo (`model`), Repositório (`repository`), Serviço (`service`), Controle/CLI (`controller`) e Exceções (`exception`).
* **Princípios SOLID**: Em especial, Responsabilidade Única (SRP) e Inversão de Controle/Injeção de Dependências.
* **Spring Boot & Spring Data JPA**: Mapeamento Objeto-Relacional (ORM) com Hibernate, persistência transacional com `@Transactional` e repositórios baseados em interfaces.
* **Banco de Dados Relacional H2**: Banco de dados relacional em memória com console web para inspeção rápida.
* **Tratamento Robusto de Exceções**: Exceções de domínio personalizadas (`RuntimeException`) e interface de terminal resiliente a falhas de digitação.
* **Testes Automatizados**: Suíte de testes unitários com JUnit 5 e testes de integração com Spring Test (`@SpringBootTest`).
* **Gerenciamento de Build**: Maven padronizado em **Java 17 LTS**.

---

## ⚙️ Funcionalidades

### 👤 Cadastro e Consulta de Cliente
* Solicita nome, CPF e e-mail.
* Persiste o cliente no banco de dados e impede duplicidade de CPF.
* O cliente mantém relação bidirecional com suas contas bancárias (`@OneToMany`).

### 🏦 Tipos de Conta
A estrutura utiliza uma classe abstrata `Conta` com herança JPA (`@Inheritance(strategy = InheritanceType.JOINED)`):

#### Conta Corrente (`ContaCorrente`)
* Possui um limite de crédito configurável (ex: R$ 5.000).
* O valor disponível para saque considera:
  $$\text{Saldo Disponível} = \text{Saldo Atual} + \text{Limite}$$
* Permite saldo negativo controlado até o limite contratado.

#### Conta Poupança (`ContaPoupanca`)
* Não possui limite de crédito.
* Permite saques apenas quando há saldo próprio suficiente na conta.

---

### 💰 Operações Financeiras

1. **Depósito**:
   * Valida para impedir valores menores ou iguais a zero ou nulos.
   * Credita o saldo e registra uma transação do tipo `DEPOSITO` no histórico e no banco de dados.

2. **Saque**:
   * Valida as regras de negócio de cada conta polimorficamente.
   * Trata situações de saldo insuficiente (`SaldoInsuficienteException`) e valores inválidos (`ValorInvalidoException`).
   * Debita o saldo e registra uma transação do tipo `SAQUE`.

3. **Transferência**:
   * Operação atômica (`@Transactional`): debita da conta de origem e credita na conta de destino.
   * Valida se as contas existem e impede transferências para a mesma conta.
   * Registra transações do tipo `TRANSFERENCIA` nas duas contas participantes.

4. **Extrato**:
   * Consulta o histórico persistido das operações realizadas pela conta filtrado por intervalo de datas.
   * Exibe tipo da operação, valor e timestamp (`LocalDateTime`).

---

## 🧠 Conceitos de POO e Arquitetura

### Abstração e Herança
A classe base `Conta` define a estrutura fundamental e o comportamento comum:
```java
@Entity
@Table(name = "tb_contas")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Conta {
    ...
    public abstract void sacar(Long valor) throws SaldoInsuficienteException, ValorInvalidoException;
}
```
As classes filhas `ContaCorrente` e `ContaPoupanca` reutilizam essa base e especializam as regras de saque.

### Polimorfismo
O método `sacar()` possui comportamentos distintos dependendo do tipo da conta em tempo de execução:
```java
// ContaCorrente considera saldo + limite
long saldoDisponivel = this.getSaldo() + this.limite;

// ContaPoupanca considera estritamente o saldo em conta
if (valor <= this.getSaldo()) { ... }
```

### Encapsulamento
Atributos protegidos por modificadores privados/protegidos, expondo apenas operações seguras por métodos e protegendo o estado financeiro da conta contra modificações externas indevidas.

---

## 🛡️ Tratamento de Exceções

As regras de negócio lançam exceções de domínio padronizadas estendendo `RuntimeException`:

* `SaldoInsuficienteException`: Lançada quando a operação excede o saldo ou saldo + limite.
* `ValorInvalidoException`: Lançada ao informar valores nulos, negativos ou zero.

Ao estenderem `RuntimeException`, integram-se perfeitamente com a anotação `@Transactional` do Spring, garantindo o **rollback automático** caso ocorra qualquer inconsistência durante uma transação bancária.

---

## 📂 Estrutura do Projeto

```text
Sistema-Bancario-Java/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/pedro/
│   │   │       ├── SistemaBancarioApplication.java   # Ponto de entrada Spring Boot
│   │   │       ├── controller/
│   │   │       │   └── MenuConsole.java               # CLI interativa (CommandLineRunner)
│   │   │       ├── exception/
│   │   │       │   ├── SaldoInsuficienteException.java
│   │   │       │   └── ValorInvalidoException.java
│   │   │       ├── model/
│   │   │       │   ├── Cliente.java                   # Entidade JPA
│   │   │       │   ├── Conta.java                     # Entidade base JPA
│   │   │       │   ├── ContaCorrente.java             # Subclasse JPA
│   │   │       │   ├── ContaPoupanca.java             # Subclasse JPA
│   │   │       │   ├── TipoTransacao.java             # Enum (DEPOSITO, SAQUE, TRANSFERENCIA)
│   │   │       │   └── Transacao.java                 # Entidade JPA
│   │   │       ├── repository/
│   │   │       │   ├── ClienteRepository.java         # Spring Data JPA
│   │   │       │   ├── ContaRepository.java           # Spring Data JPA
│   │   │       │   └── TransacaoRepository.java       # Spring Data JPA
│   │   │       └── service/
│   │   │           ├── ClienteService.java            # Regras e transações de Cliente
│   │   │           ├── ContaService.java              # Regras e transações de Conta
│   │   │           └── TransacaoService.java          # Depósito, saque, transferência, extrato
│   │   └── resources/
│   │       └── application.properties                 # Configurações do H2 e JPA
│   └── test/
│       └── java/
│           ├── ContaCorrenteTest.java                 # Teste unitário
│           ├── ContaPoupancaTest.java                 # Teste unitário
│           └── org/pedro/
│               ├── SistemaBancarioApplicationTests.java # Teste de integração Spring Boot
│               └── model/
│                   ├── ContaCorrenteTest.java         # Testes de unidade expandidos
│                   └── ContaPoupancaTest.java         # Testes de unidade expandidos
├── pom.xml
└── README.md
```

---

## 🛠️ Tecnologias

| Tecnologia | Finalidade |
|---|---|
| ☕ **Java 17 LTS** | Linguagem principal do projeto |
| 🍃 **Spring Boot 4 / 3** | Framework base da aplicação |
| 💾 **Spring Data JPA / Hibernate** | Mapeamento ORM e abstração de persistência |
| 🐘 **PostgreSQL 16 (Docker)** | Banco de dados relacional principal em container |
| 💾 **Rotina de Backup Docker** | Container automatizado com dumps periódicos compactados (.sql.gz) |
| 🗄️ **H2 Database** | Banco em memória para execução rápida de testes automatizados |
| 🧪 **JUnit 5 & AssertJ** | Testes automatizados unitários e de integração |
| 📦 **Maven** | Gerenciador de dependências e automação de build |

---

## 🚀 Como Executar

### Pré-requisitos
* **JDK 17** ou superior instalado e configurado no `PATH`.
* **Maven** instalado.
* **Docker e Docker Compose** instalados (ex: Docker Desktop).

### 1. Clonar o repositório
```bash
git clone https://github.com/PedrorCastro/Sistema-Bancario-Java.git
cd Sistema-Bancario-Java
```

### 2. Subir o Banco de Dados PostgreSQL e o Backup via Docker
Execute o Docker Compose na raiz do projeto:
```bash
docker compose up -d
```
Isso iniciará:
1. **`bancodb-postgres`**: PostgreSQL 16 na porta `5432` com volume persistente local.
2. **`bancodb-backup`**: Container automático que gera dumps compactados (`.sql.gz`) na pasta `./backups` da sua máquina.

### 3. Compilar e executar os testes
Os testes rodam isoladamente em memória via perfil de teste (H2), garantindo velocidade sem depender do banco:
```bash
mvn clean test
```

### 4. Executar a aplicação
Pela linha de comando via Maven:
```bash
mvn spring-boot:run
```
Ou executando a classe principal pela sua IDE:
```text
src/main/java/org/pedro/SistemaBancarioApplication.java
```

---

## 💾 Gerenciamento de Backups Locais (Docker)

Além dos backups automáticos diários em `./backups`, você pode disparar comandos manuais a qualquer momento:

* **Gerar backup manual imediato:**
  ```bash
  docker exec -t bancodb-postgres pg_dump -U postgres -d bancodb > ./backups/backup_manual.sql
  ```

* **Restaurar um backup:**
  ```powershell
  # No PowerShell do Windows:
  Get-Content ./backups/backup_manual.sql | docker exec -i bancodb-postgres psql -U postgres -d bancodb
  ```

* **Parar os containers sem perder os dados:**
  ```bash
  docker compose down
  ```

---

## 📝 Histórico de Refatoração e Melhorias Aplicadas

Abaixo está o registro detalhado das correções arquiteturais e de código implementadas no projeto:

| Componente | O que foi alterado | Motivo da mudança |
|---|---|---|
| **`Cliente`** | Adicionado `@Id Long id`, construtor no-args, `@Table` e removida lista duplicada `conta` | Exigência da JPA para entidades; corrigiu falha de compilação/inicialização do `ClienteRepository`. |
| **`Conta`** | Corrigido relacionamento de `historico` para `@OneToMany`, removido `cliente` redundante (mantido `titular`) e adicionado `@Inheritance` | Listas não podem ser `@ManyToOne`; unificação de referências e definição da estratégia de tabelas no banco. |
| **`ContaCorrente` / `ContaPoupanca`** | Anotadas com `@Entity`, adicionados construtores no-args e construtor com sobrecarga compatível | Permitir persistência polimórfica das subclasses e evitar ambiguidade do compilador Java. |
| **`Transacao`** | Adicionada anotação `@Enumerated(EnumType.STRING)` e atributo `@ManyToOne Conta conta` | Evita corrupção ordinal de dados no banco e viabiliza as consultas `findByContaId` no repositório. |
| **`SaldoInsuficienteException`** | Alterada para estender `RuntimeException` | Padronização com `ValorInvalidoException` e garantia de rollback automático em anotações `@Transactional`. |
| **`ClienteService`** | Métodos `buscarPorCpf` corrigidos para retornar `Optional<Cliente>`, e injeção por construtor | O método anterior era `void` e descartava a consulta; injeção por construtor segue a boa prática do Spring. |
| **`ContaService`** | Métodos `buscarPorId`, `buscarPorNumero` e `listarPorCliente` corrigidos para retornar dados | Os métodos anteriores eram `void` e não entregavam os objetos consultados ao chamador. |
| **`TransacaoService`** | Eliminação de transações duplicadas/órfãs, inclusão de validação de mesma conta e suporte a `TRANSFERENCIA` | Manter a integridade contábil do extrato e sincronizar com o banco via cascata JPA. |
| **`MenuConsole`** | Integrado aos Services, adicionada leitura resiliente no `Scanner`, inclusão de Transferência e `@Profile("!test")` | Persistir de fato os dados no H2 e permitir execução de testes do Spring sem travar esperando entrada do teclado. |
| **`application.properties`** | Criado arquivo de configuração em `src/main/resources/` com encoding UTF-8 | Habilitação do banco H2 em memória, console web e configuração das tabelas Hibernate. |
| **Testes Unitários** | Adicionado teste de contexto `@SpringBootTest` e testes completos cobrindo os caminhos felizes de depósito e saque | Garantir que o Spring inicializa com sucesso e aumentar a confiabilidade do código. |

---

## 🔮 Próximas Evoluções

- [x] Persistência em banco de dados relacional (PostgreSQL + H2 para testes)
- [x] Integração completa com Spring Data JPA
- [x] Transferência bancária atômica entre contas
- [x] Testes unitários e de integração (JUnit 5 + Spring Boot Test)
- [x] Integração com PostgreSQL via Docker e rotina de backup local
- [ ] API REST com controladores (`@RestController`) e DTOs
- [ ] Documentação interativa da API com OpenAPI / Swagger
- [ ] Validação formal de dígitos de CPF (algoritmo receita federal)
- [ ] Autenticação e autorização via JWT / Spring Security

---

## 👨‍💻 Autor

**Pedro Rodrigues de Castro Bezerra**  
Estudante de Ciência da Computação | Desenvolvedor Backend Java / Spring Boot  
* GitHub: [PedrorCastro](https://github.com/PedrorCastro)
