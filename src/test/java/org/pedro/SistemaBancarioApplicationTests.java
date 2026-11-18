
package org.pedro;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pedro.dto.request.DepositoRequestDTO;
import org.pedro.dto.request.SaqueRequestDTO;
import org.pedro.dto.request.TransferenciaRequestDTO;
import org.pedro.exception.SaldoInsuficienteException;
import org.pedro.model.Cliente;
import org.pedro.model.ContaPoupanca;
import org.pedro.model.TipoTransacao;
import org.pedro.repository.ClienteRepository;
import org.pedro.repository.ContaRepository;
import org.pedro.repository.TransacaoRepository;
import org.pedro.service.TransacaoService;
import org.pedro.service.ClienteService;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SistemaBancarioApplicationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    private static final String CLIENTE_A_EMAIL = "cliente-a@example.test";
    private static final String CLIENTE_B_EMAIL = "cliente-b@example.test";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private TransacaoService transacaoService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void limparBanco() {
        transacaoRepository.deleteAll();
        contaRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    @Test
    @DisplayName("Contexto do Spring Boot e mapeamentos JPA devem carregar com sucesso")
    void contextLoads() {
    }

    @Test
    @DisplayName("Flyway atualiza um banco existente sem apagar os dados")
    void migracaoFazBaselineEAtualizaBancoExistente() {
        String schema = "flyway_upgrade_test";
        jdbcTemplate.execute("CREATE SCHEMA " + schema);

        try {
            Flyway.configure()
                    .dataSource(dataSource)
                    .schemas(schema)
                    .defaultSchema(schema)
                    .locations("classpath:db/migration")
                    .target(MigrationVersion.fromVersion("1"))
                    .load()
                    .migrate();

            Long clienteId = jdbcTemplate.queryForObject(
                    "INSERT INTO " + schema
                            + ".tb_clientes (nome, cpf, email, senha, role) "
                            + "VALUES (?, ?, ?, ?, ?) RETURNING id",
                    Long.class,
                    "Cliente legado",
                    "33333333333",
                    "legado@example.test",
                    "encoded-test-password",
                    "CLIENTE");
            Long contaId = jdbcTemplate.queryForObject(
                    "INSERT INTO " + schema
                            + ".tb_contas (numero, titular_id, saldo) VALUES (?, ?, ?) RETURNING id",
                    Long.class,
                    3001,
                    clienteId,
                    new BigDecimal("12.34"));
            jdbcTemplate.update(
                    "INSERT INTO " + schema + ".tb_contas_poupancas (id) VALUES (?)",
                    contaId);
            jdbcTemplate.update(
                    "INSERT INTO " + schema
                            + ".tb_transacoes (valor, tipo, data, conta_id) "
                            + "VALUES (?, ?, CURRENT_TIMESTAMP, ?)",
                    new BigDecimal("12.34"),
                    "DEPOSITO",
                    contaId);

            jdbcTemplate.execute("DROP TABLE " + schema + ".flyway_schema_history");

            Flyway.configure()
                    .dataSource(dataSource)
                    .schemas(schema)
                    .defaultSchema(schema)
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .baselineVersion(MigrationVersion.fromVersion("1"))
                    .load()
                    .migrate();

            assertEquals("ATIVA", jdbcTemplate.queryForObject(
                    "SELECT status FROM " + schema + ".tb_contas WHERE id = ?",
                    String.class,
                    contaId));
            assertEquals(new BigDecimal("12.34"), jdbcTemplate.queryForObject(
                    "SELECT saldo FROM " + schema + ".tb_contas WHERE id = ?",
                    BigDecimal.class,
                    contaId));
            assertEquals(1, jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM " + schema + ".tb_transacoes WHERE conta_id = ?",
                    Integer.class,
                    contaId));
            assertEquals(1, jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns "
                            + "WHERE table_schema = ? AND table_name = 'tb_transacoes' "
                            + "AND column_name = 'conta_contraparte_id'",
                    Integer.class,
                    schema));
        } finally {
            jdbcTemplate.execute("DROP SCHEMA IF EXISTS " + schema + " CASCADE");
        }
    }

    @Test
    @DisplayName("Cliente não pode sacar, depositar, transferir ou consultar conta alheia")
    void clienteNaoAcessaOperacoesDaContaDeOutroCliente() throws Exception {
        Fixture fixture = criarContas();
        RequestPostProcessor clienteA = user(CLIENTE_A_EMAIL).roles("CLIENTE");

        mockMvc.perform(post("/transacoes/sacar")
                        .with(clienteA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(
                                new SaqueRequestDTO(
                                        fixture.contaBId(), new BigDecimal("1.00")))))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/transacoes/depositar")
                        .with(clienteA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(
                                new DepositoRequestDTO(
                                        fixture.contaBId(), new BigDecimal("1.00")))))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/transacoes/transferir")
                        .with(clienteA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(
                                new TransferenciaRequestDTO(
                                        fixture.contaBId(), fixture.contaAId(),
                                        new BigDecimal("1.00")))))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/transacoes/extrato")
                        .with(clienteA)
                        .param("contaId", fixture.contaBId().toString())
                        .param("dataInicio", "2026-01-01T00:00:00")
                        .param("dataFim", "2027-01-01T00:00:00"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/transacoes/conta/{contaId}", fixture.contaBId())
                        .with(clienteA))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Cliente pode transferir da própria conta para conta de outro titular")
    void clientePodeTransferirParaOutroTitular() throws Exception {
        Fixture fixture = criarContas();
        RequestPostProcessor clienteA = user(CLIENTE_A_EMAIL).roles("CLIENTE");

        mockMvc.perform(post("/transacoes/transferir")
                        .with(clienteA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(
                                new TransferenciaRequestDTO(
                                        fixture.contaAId(), fixture.contaBId(),
                                        new BigDecimal("5.00")))))
                .andExpect(status().isOk());

        assertEquals(new BigDecimal("15.00"),
                contaRepository.findById(fixture.contaAId()).orElseThrow().getSaldo());
        assertEquals(new BigDecimal("25.00"),
                contaRepository.findById(fixture.contaBId()).orElseThrow().getSaldo());
        assertEquals(fixture.contaBId(),
                transacaoRepository.findByContaId(fixture.contaAId()).stream()
                        .filter(transacao -> transacao.getTipo() == TipoTransacao.TRANSFERENCIA)
                        .findFirst()
                        .orElseThrow()
                        .getContaContraparte()
                        .getId());
    }

    @Test
    @DisplayName("Encerramento preserva o extrato e bloqueia novas movimentações")
    void encerramentoLogicoPreservaHistorico() throws Exception {
        Fixture fixture = criarContas();
        RequestPostProcessor clienteA = user(CLIENTE_A_EMAIL).roles("CLIENTE");

        mockMvc.perform(post("/transacoes/sacar")
                        .with(clienteA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(
                                new SaqueRequestDTO(
                                        fixture.contaAId(), new BigDecimal("20.00")))))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/contas/{id}", fixture.contaAId()).with(clienteA))
                .andExpect(status().isNoContent());

        assertEquals(org.pedro.model.StatusConta.ENCERRADA,
                contaRepository.findById(fixture.contaAId()).orElseThrow().getStatus());
        assertEquals(2, transacaoRepository.findByContaId(fixture.contaAId()).size());

        mockMvc.perform(post("/transacoes/depositar")
                        .with(clienteA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(
                                new DepositoRequestDTO(
                                        fixture.contaAId(), new BigDecimal("1.00")))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Excluir cliente com contas não apaga o histórico financeiro")
    void naoExcluiClienteComContas() {
        Fixture fixture = criarContas();
        Long clienteId = contaRepository.findById(fixture.contaBId())
                .orElseThrow()
                .getTitular()
                .getId();

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> clienteService.deletarCliente(clienteId));

        assertTrue(contaRepository.existsById(fixture.contaBId()));
        assertEquals(1, transacaoRepository.findByContaId(fixture.contaBId()).size());
    }

    @Test
    @DisplayName("Saques concorrentes no PostgreSQL não causam saldo negativo nem perda de atualização")
    void saquesConcorrentesPreservamSaldoEHistorico() throws Exception {
        Cliente cliente = salvarCliente("Concorrência", "12345678901", CLIENTE_A_EMAIL);
        ContaPoupanca conta = new ContaPoupanca(1001, cliente);
        conta.depositar(new BigDecimal("10.00"));
        conta = contaRepository.saveAndFlush(conta);
        Long contaId = conta.getId();

        int workers = 8;
        int tentativas = 40;
        ExecutorService executor = Executors.newFixedThreadPool(workers);
        CountDownLatch prontos = new CountDownLatch(workers);
        CountDownLatch iniciar = new CountDownLatch(1);
        List<Future<Boolean>> resultados = new ArrayList<>();

        try {
            for (int i = 0; i < tentativas; i++) {
                resultados.add(executor.submit(() -> {
                    prontos.countDown();
                    if (!iniciar.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Timeout aguardando início dos saques concorrentes");
                    }

                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                            CLIENTE_A_EMAIL,
                            "test",
                            List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))));
                    SecurityContextHolder.setContext(context);
                    try {
                        transacaoService.sacar(contaId, BigDecimal.ONE);
                        return true;
                    } catch (SaldoInsuficienteException ex) {
                        return false;
                    } finally {
                        SecurityContextHolder.clearContext();
                    }
                }));
            }

            assertTrue(
                    prontos.await(10, TimeUnit.SECONDS),
                    "Os workers não ficaram prontos a tempo");
            iniciar.countDown();

            long saquesConcluidos = 0;
            for (Future<Boolean> resultado : resultados) {
                if (resultado.get(30, TimeUnit.SECONDS)) {
                    saquesConcluidos++;
                }
            }

            assertEquals(10, saquesConcluidos);
            ContaPoupanca contaAtualizada = (ContaPoupanca) contaRepository.findById(contaId)
                    .orElseThrow();
            assertEquals(
                    BigDecimal.ZERO.setScale(2), contaAtualizada.getSaldo());
            assertEquals(10,
                    transacaoRepository.findByContaId(contaId).stream()
                            .filter(transacao -> transacao.getTipo() == TipoTransacao.SAQUE)
                            .count());
        } finally {
            iniciar.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    private Fixture criarContas() {
        Cliente clienteA = salvarCliente("Cliente A", "11111111111", CLIENTE_A_EMAIL);
        Cliente clienteB = salvarCliente("Cliente B", "22222222222", CLIENTE_B_EMAIL);

        ContaPoupanca contaA = new ContaPoupanca(2001, clienteA);
        contaA.depositar(new BigDecimal("20.00"));
        contaA = contaRepository.saveAndFlush(contaA);

        ContaPoupanca contaB = new ContaPoupanca(2002, clienteB);
        contaB.depositar(new BigDecimal("20.00"));
        contaB = contaRepository.saveAndFlush(contaB);

        return new Fixture(contaA.getId(), contaB.getId());
    }

    private Cliente salvarCliente(String nome, String cpf, String email) {
        return clienteRepository.save(new Cliente(nome, cpf, email, "encoded-test-password"));
    }

    private record Fixture(Long contaAId, Long contaBId) {
    }
}
