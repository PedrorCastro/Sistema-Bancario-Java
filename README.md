# Sistema Bancario Java

API RESTful para gerenciamento de clientes, contas bancarias e transacoes financeiras. O projeto usa Java 17 e Spring Boot 3.4.1, com persistencia em PostgreSQL, seguranca stateless baseada em JWT e uma interface de linha de comando opcional.

## Visao geral

- Java 17 e Spring Boot 3.4.1
- Spring Web e Spring Security
- JWT com OAuth2 Resource Server
- Spring Data JPA e Hibernate
- PostgreSQL 16 via Docker Compose
- Flyway para migracoes de banco
- H2 para testes
- Testcontainers para testes de integracao
- JUnit 5, Mockito e AssertJ
- SpringDoc OpenAPI/Swagger UI
- Maven
- BigDecimal para valores monetarios

## Funcionalidades

- Cadastro publico de clientes com validacao de CPF, email e senha
- Login com emissao de token JWT
- Consulta dos dados do cliente autenticado
- Operacoes para clientes autenticados e administradores
- Conta Corrente com limite de credito
- Conta Poupanca sem limite de credito
- Depositos, saques e transferencias
- Transferencias atomicas entre contas
- Historico de transacoes e consulta de extrato por periodo
- Encerramento logico de contas, exigindo saldo zero
- Roles `CLIENTE` e `ADMIN`
- Senhas protegidas com BCrypt
- Respostas padronizadas para erros de dominio

## Arquitetura

```text
src/main/java/org/pedro/
|-- config/         Configuracao de seguranca, JWT e OpenAPI
|-- controller/     Endpoints REST
|-- dto/            Objetos de entrada e resposta
|-- exception/      Excecoes de dominio e tratamento global
|-- model/          Entidades e tipos de conta
|-- repository/     Repositorios Spring Data JPA
|-- security/       Usuario autenticado, roles e servicos JWT
|-- service/        Regras de negocio
|-- validation/     Validacao customizada de CPF
`-- SistemaBancarioApplication.java
```

As contas usam heranca JPA com a estrategia `JOINED`. Os valores financeiros sao armazenados com duas casas decimais e manipulados com `BigDecimal`.

## Requisitos

- JDK 17
- Maven 3.9 ou superior
- Docker e Docker Compose
- Apache Maven no `PATH`

## Configuracao

Copie o arquivo de exemplo:

```bash
cp .env.example .env
```

Configure as variaveis:

```dotenv
DB_URL=jdbc:postgresql://localhost:5432/bancodb
DB_USERNAME=postgres
DB_PASSWORD=sua-senha

JWT_SECRET=sua-chave-base64

JPA_DDL_AUTO=validate
```

`JWT_SECRET` precisa ser uma chave Base64 que represente pelo menos 32 bytes. Voce pode gerar uma chave com:

```bash
openssl rand -base64 32
```

Gere uma chave separada para a senha do banco, se necessario:

```bash
openssl rand -base64 32
```

O Maven nao carrega `.env` automaticamente. Antes de iniciar a aplicacao em um shell Unix, execute:

```bash
set -a
. ./.env
set +a
```

## Executando com Docker

Inicie PostgreSQL e o backup automatico:

```bash
docker compose up -d
```

O Compose publica o PostgreSQL apenas em `127.0.0.1`. Os dumps sao mantidos em `./backups` e o agendamento padrao ocorre diariamente.

Depois, execute a API:

```bash
mvn spring-boot:run
```

A aplicacao fica disponivel em `http://localhost:8080`.

Para encerrar os containers:

```bash
docker compose down
```

Para remover tambem o volume do PostgreSQL:

```bash
docker compose down -v
```

> Cuidado: o ultimo comando elimina os dados persistidos do banco.

## Executando os testes

Compile o projeto e execute toda a suite:

```bash
mvn clean test
```

Os testes com H2 podem ser executados sem o Docker. Os testes de integracao que usam Testcontainers exigem Docker disponivel no ambiente.

## Interface de linha de comando

A CLI esta incluida em `MenuConsole`, mas usa o profile `console` e nao inicia no modo REST padrao.

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=console
```

A CLI permite cadastrar clientes e contas, realizar depositos, saques, transferencias e consultar extratos.

## Documentacao da API

Com a aplicacao iniciada, acesse:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

No Swagger UI, use o botao **Authorize** e informe o token no formato:

```text
Bearer <token>
```

## Autenticacao

### Cadastrar cliente

```http
POST /clientes
Content-Type: application/json

{
  "nome": "Joao Silva",
  "cpf": "45566045839",
  "email": "joao@email.com",
  "senha": "Senha123!"
}
```

O cadastro retorna `201 Created`.

### Login

```http
POST /auth/login
Content-Type: application/json

{
  "email": "joao@email.com",
  "senha": "Senha123!"
}
```

Resposta:

```json
{
  "token": "<jwt>",
  "tipo": "Bearer",
  "expiresIn": 3600000
}
```

Envie o token nas requisicoes protegidas:

```http
Authorization: Bearer <jwt>
```

## Endpoints principais

| Metodo | Endpoint | Acesso | Descricao |
|---|---|---|---|
| POST | `/clientes` | Publico | Cadastra um cliente |
| POST | `/auth/login` | Publico | Autentica e gera JWT |
| GET | `/clientes/me` | `CLIENTE` | Retorna os dados do cliente autenticado |
| GET | `/clientes/{id}` | `ADMIN` | Busca um cliente por ID |
| DELETE | `/clientes/{id}` | `ADMIN` | Remove um cliente |
| GET | `/admin/clientes` | Autenticado | Lista todos os clientes |
| POST | `/contas` | `CLIENTE` ou `ADMIN` | Abre uma conta |
| GET | `/contas` | `ADMIN` | Lista todas as contas |
| GET | `/contas/minhas` | `CLIENTE` ou `ADMIN` | Lista as contas do usuario autenticado |
| GET | `/contas/{id}` | `CLIENTE` ou `ADMIN` | Consulta uma conta |
| DELETE | `/contas/{id}` | `CLIENTE` ou `ADMIN` | Encerra uma conta com saldo zero |
| POST | `/transacoes/depositar` | Autenticado | Realiza um deposito |
| POST | `/transacoes/sacar` | Autenticado | Realiza um saque |
| POST | `/transacoes/transferir` | Autenticado | Transfere valor entre contas |
| GET | `/transacoes/extrato` | Autenticado | Consulta transacoes por periodo |
| GET | `/transacoes/conta/{contaId}` | Autenticado | Lista transacoes de uma conta |

## Criando uma conta

```http
POST /contas
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "numero": 12345,
  "clienteId": 1,
  "tipoConta": "CORRENTE",
  "limite": 5000.00,
  "saldo": 100.00
}
```

Tipos aceitos:

- `CORRENTE`
- `POUPANCA`

A resposta usa `ContaResponseDTO` e informa ID, tipo, saldo, ID do titular e status (`ATIVA` ou `ENCERRADA`).

## Transacoes

### Deposito

```http
POST /transacoes/depositar
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "contaId": 1,
  "valor": 25.50
}
```

### Saque

```http
POST /transacoes/sacar
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "contaId": 1,
  "valor": 10.00
}
```

### Transferencia

```http
POST /transacoes/transferir
Authorization: Bearer <jwt>
Content-Type: application/json

{
  "contaOrigemId": 1,
  "contaDestinoId": 2,
  "valor": 5.00
}
```

### Extrato

```http
GET /transacoes/extrato?contaId=1&dataInicio=2026-01-01T00:00:00&dataFim=2027-01-01T00:00:00
Authorization: Bearer <jwt>
```

Os endpoints bancarios validam valores positivos, disponibilidade de saldo e permissoes do usuario autenticado.

## Banco de dados e migracoes

O esquema relacional esta em `src/main/resources/db/migration`:

- `V1__initial_schema.sql`: cria clientes, contas, tipos de conta e transacoes
- `V2__account_lifecycle_and_transfer_counterparty.sql`: adiciona status da conta e contraparte das transferencias

Por padrao, `spring.jpa.hibernate.ddl-auto` esta configurado como `validate`. As alteracoes de esquema devem ser feitas por novas migracoes Flyway.

Para bancos existentes e nao vazios, o Flyway usa baseline na versao 1. Antes da primeira migracao, recomenda-se fazer backup.

## Variaveis de ambiente

| Variavel | Padrao | Descricao |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/bancodb` | URL JDBC do PostgreSQL |
| `DB_USERNAME` | `postgres` | Usuario do banco |
| `DB_PASSWORD` | Obrigatoria | Senha do banco |
| `JPA_DDL_AUTO` | `validate` | Estrategia do Hibernate |
| `JWT_SECRET` | Obrigatoria | Chave Base64 com pelo menos 32 bytes |

## Seguranca

- Senhas armazenadas com BCrypt
- Autenticacao sem estado com JWT
- Expiracao configurada em 1 hora
- Proibicao de CSRF porque o fluxo usa API stateless
- Cabeçalhos de proxy reverso habilitados
- Swagger UI e recursos OpenAPI publicos

## Arquivos auxiliares

- `.env.example`: modelo das variaveis de ambiente
- `docker-compose.yml`: PostgreSQL e backup local
- `testes-api.http`: exemplos de requisicoes para clientes HTTP e VS Code REST Client
- `backups/`: destino dos arquivos de backup compactados
