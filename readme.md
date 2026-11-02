# 🏦 Sistema Bancário em Java

Sistema bancário desenvolvido em **Java** com **Spring Boot**, demonstrando a aplicação prática dos principais conceitos de **Programação Orientada a Objetos (POO)** e a evolução para uma arquitetura profissional em camadas.

O projeto utiliza **Spring Data JPA** para persistência em banco de dados relacional, disponibiliza uma **API REST** estruturada com DTOs e possui configuração de segurança com **Spring Security**.

A aplicação simula operações bancárias por meio de uma interface de linha de comando (CLI) interativa e controladores REST, permitindo cadastrar clientes, abrir contas (Corrente ou Poupança) e gerenciar transações com histórico persistido.

---

## 📌 Sobre o Projeto

O projeto foi desenvolvido para consolidar conceitos essenciais do ecossistema Backend Java moderno:

- **Programação Orientada a Objetos (POO):** Abstração, Herança, Encapsulamento e Polimorfismo.
- **Arquitetura em Camadas:** Separação de responsabilidades entre Modelo (`model`), Repositório (`repository`), Serviço (`service`), Controladores REST (`controller`), Console (`controller/Console`), DTOs e Exceções (`exception`).
- **Princípios SOLID:** Aplicação do princípio da Responsabilidade Única (SRP), Inversão de Controle (IoC) e Injeção de Dependências.
- **Spring Boot & Spring Data JPA:** Mapeamento Objeto-Relacional (ORM) com Hibernate, persistência transacional com `@Transactional` e repositórios baseados em interfaces.
- **Banco de Dados Relacional H2 & PostgreSQL:** Utilização do H2 em memória para testes e PostgreSQL em container Docker para persistência.
- **Padronização com DTOs (Data Transfer Objects):** Separação entre entidades de banco de dados e objetos de transferência, evitando referências circulares e serializações recursivas indesejadas do Jackson.
- **Tratamento de Exceções:** Exceções de domínio personalizadas e **tratamento de erros global via `@ControllerAdvice`**, garantindo respostas HTTP padronizadas e sem exposição de stacktraces.
- **Precisão Financeira:** Utilização de `BigDecimal` para manipulação de saldos e transações, evitando erros de precisão comuns em tipos de ponto flutuante.
- **Testes Automatizados:** Testes unitários com JUnit 5 e testes de integração com Spring Test (`@SpringBootTest`).
- **Gerenciamento de Build:** Maven para gerenciamento de dependências e automação de build.

---

## ⚙️ Funcionalidades

### 👤 Cadastro e Consulta de Clientes

- Cadastro de clientes com nome, CPF e e-mail.
- Persistência dos dados no banco de dados.
- Validação para impedir duplicidade de CPF.
- Relacionamento bidirecional entre clientes e suas contas bancárias utilizando `@OneToMany`.

### 🏦 Tipos de Conta

A estrutura utiliza uma classe abstrata `Conta`, com herança JPA através de:

`@Inheritance(strategy = InheritanceType.JOINED)`

#### Conta Corrente (`ContaCorrente`)

- Possui limite de crédito configurável.
- O valor disponível para saque considera o saldo atual mais o limite.
- Permite saldo negativo controlado até o limite contratado.

#### Conta Poupança (`ContaPoupanca`)

- Não possui limite de crédito.
- Permite saques somente quando há saldo próprio suficiente na conta.

### 💸 Operações Bancárias

- Depósitos.
- Saques.
- Transferências entre contas.
- Validação de saldo disponível e limites.
- Transferências bancárias atômicas, garantindo consistência nas operações.
- Histórico de transações persistido no banco de dados.
- Consulta de extrato bancário.

---

## 📡 Endpoints da API REST

A aplicação disponibiliza controladores REST para gerenciamento de clientes, contas e transações.

Abaixo estão alguns dos principais endpoints documentados.

### 1. Listar Contas

Retorna uma lista com todas as contas cadastradas, utilizando DTOs para evitar exposição direta das entidades JPA.

**GET** `/contas`

**Resposta (200 OK):**

```json
[
  {
    "id": 1,
    "tipoConta": "ContaCorrente",
    "saldo": 5000.00,
    "clienteId": 1
  }
]
```

### 2. Buscar Conta por ID

Retorna os dados de uma conta específica.

**GET** `/contas/{id}`

Exemplo: `GET /contas/1`

**Resposta (200 OK):**

```json
{
  "id": 1,
  "tipoConta": "ContaCorrente",
  "saldo": 5000.00,
  "clienteId": 1
}
```

### 3. Criar Nova Conta

Cadastra uma nova conta (Corrente ou Poupança) vinculada a um cliente existente.

**POST** `/contas`

**Body (JSON):**

```json
{
  "numero": 12345,
  "clienteId": 1,
  "saldo": 1000.00,
  "tipoConta": "CORRENTE"
}
```

**Resposta (201 Created):**

```json
{
  "id": 2,
  "tipoConta": "ContaCorrente",
  "saldo": 1000.00,
  "clienteId": 1
}
```

### 4. Deletar Conta

Remove uma conta do sistema através do seu identificador.

**DELETE** `/contas/{id}`

Exemplo: `DELETE /contas/2`

**Resposta (204 No Content):**

Sem conteúdo retornado em caso de sucesso.

---

## 📂 Estrutura do Projeto

```text
Sistema-Bancario-Java/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/pedro/
│   │   │       ├── SistemaBancarioApplication.java
│   │   │       │
│   │   │       ├── config/
│   │   │       │   └── SecurityConfig.java
│   │   │       │
│   │   │       ├── controller/
│   │   │       │   ├── ClienteController.java
│   │   │       │   ├── ContaController.java
│   │   │       │   ├── TransacaoController.java
│   │   │       │   └── Console/
│   │   │       │       └── MenuConsole.java
│   │   │       │
│   │   │       ├── dto/
│   │   │       │   ├── request/
│   │   │       │   └── response/
│   │   │       │
│   │   │       ├── exception/
│   │   │       │   ├── SaldoInsuficienteException.java
│   │   │       │   ├── ValorInvalidoException.java
│   │   │       │   └── GlobalExceptionHandler.java
│   │   │       │
│   │   │       ├── model/
│   │   │       │   ├── Cliente.java
│   │   │       │   ├── Conta.java
│   │   │       │   ├── ContaCorrente.java
│   │   │       │   ├── ContaPoupanca.java
│   │   │       │   ├── TipoTransacao.java
│   │   │       │   └── Transacao.java
│   │   │       │
│   │   │       ├── repository/
│   │   │       │   ├── ClienteRepository.java
│   │   │       │   ├── ContaRepository.java
│   │   │       │   └── TransacaoRepository.java
│   │   │       │
│   │   │       └── service/
│   │   │           ├── ClienteService.java
│   │   │           ├── ContaService.java
│   │   │           └── TransacaoService.java
│   │
│   └── resources/
│       ├── application.properties
│       └── application-test.properties
│
│   └── test/
│       └── java/
│           └── org/pedro/
│
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

## 🛠️ Tecnologias Utilizadas

| Tecnologia                 | Finalidade                                           |
| -------------------------- | ---------------------------------------------------- |
| ☕ Java LTS                 | Linguagem principal do projeto                       |
| 🍃 Spring Boot 3.4.1       | Framework base da aplicação                          |
| 🌐 Spring Web              | Construção da API REST                               |
| 🔒 Spring Security         | Configuração da camada de segurança                  |
| 💾 Spring Data JPA         | Persistência e acesso aos dados                      |
| ⚙️ Hibernate               | Mapeamento Objeto-Relacional (ORM)                   |
| 🐘 PostgreSQL 16           | Banco de dados relacional principal                  |
| 🐳 Docker & Docker Compose | Containerização e gerenciamento do banco             |
| 💾 Docker Backup           | Rotina de backups periódicos compactados (`.sql.gz`) |
| 🗄️ H2 Database            | Banco em memória para testes                         |
| 🧪 JUnit 5                 | Testes automatizados                                 |
| ✅ AssertJ                  | Biblioteca de asserções para testes                  |
| 📦 Maven                   | Gerenciamento de dependências e build                |
| 📐 BigDecimal              | Precisão decimal para operações financeiras           |

---

## 🚀 Como Executar

### Pré-requisitos

Antes de executar o projeto, certifique-se de possuir:

- JDK compatível instalado e configurado no PATH.
- Maven instalado.
- Docker e Docker Compose instalados (ex.: Docker Desktop).

### 1. Clonar o repositório

```bash
git clone https://github.com/PedrorCastro/Sistema-Bancario-Java.git

cd Sistema-Bancario-Java
```

### 2. Configurar as variáveis de ambiente

Configure as variáveis necessárias para a conexão com o PostgreSQL, de acordo com as propriedades utilizadas em `application.properties`.

Exemplo:

```env
URL_BANCO=jdbc:postgresql://localhost:5432/sistema_bancario
DB_USER=seu_usuario
DB_PASSWORD=sua_senha
```

> Não compartilhe credenciais reais nem envie arquivos `.env` com informações sensíveis para o GitHub.

### 3. Subir o banco de dados

Na raiz do projeto, execute:

```bash
docker compose up -d
```

Esse comando inicia os serviços definidos no Docker Compose, incluindo o PostgreSQL e os serviços de backup configurados.

### 4. Executar os testes

Para compilar o projeto e executar a suíte de testes:

```bash
mvn clean test
```

### 5. Executar a aplicação

Para iniciar a aplicação utilizando Maven:

```bash
mvn spring-boot:run
```

---

## 🔮 Próximas Evoluções

- [x] Persistência em banco de dados relacional (PostgreSQL + H2 para testes).
- [x] Integração com Spring Data JPA e Hibernate.
- [x] Transferência bancária atômica entre contas.
- [x] Testes unitários e de integração (JUnit 5 + Spring Boot Test).
- [x] Integração com PostgreSQL via Docker.
- [x] Rotina de backup automatizado.
- [x] API REST com controladores (`@RestController`).
- [x] Padronização das respostas utilizando DTOs.
- [x] Organização da aplicação em arquitetura em camadas.
- [x] Configuração da camada de segurança com Spring Security.
- [ ] Implementação de autenticação e autorização com JWT.
- [ ] Documentação interativa da API com OpenAPI / Swagger.

---

## 👨‍💻 Autor

**Pedro Rodrigues de Castro Bezerra**

Estudante de Ciência da Computação | Desenvolvedor Backend Java / Spring Boot

- GitHub: [@PedrorCastro](https://github.com/PedrorCastro)
- Repositório: [Sistema-Bancario-Java](https://github.com/PedrorCastro/Sistema-Bancario-Java)

---

⭐ Se este projeto foi interessante, considere deixar uma estrela no repositório!
