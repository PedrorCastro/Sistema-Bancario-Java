package org.pedro;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pedro.dto.request.ClienteRequestDTO;
import org.pedro.dto.request.ContaRequestDTO;
import org.pedro.dto.request.DepositoRequestDTO;
import org.pedro.dto.request.SaqueRequestDTO;
import org.pedro.dto.request.TipoConta;
import org.pedro.dto.request.TransferenciaRequestDTO;
import org.pedro.model.Cliente;
import org.pedro.model.ContaPoupanca;
import org.pedro.repository.ClienteRepository;
import org.pedro.repository.ContaRepository;
import org.pedro.repository.TransacaoRepository;
import org.pedro.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Percorre a API com os três tipos de usuário (anônimo, CLIENTE e ADMIN)
 * e provoca cada código de erro HTTP que a aplicação pode devolver.
 * O 500 fica em {@link ErroInternoTests}, porque exige simular uma falha.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CenariosHttpPorPerfilTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    private static final String CLIENTE_A_EMAIL = "cliente-a@example.test";
    private static final String CLIENTE_A_SENHA = "SenhaCorreta123";
    private static final String CLIENTE_A_CPF = "52998224725";
    private static final String CLIENTE_B_EMAIL = "cliente-b@example.test";
    private static final String ADMIN_EMAIL = "admin@example.test";
    private static final long ID_INEXISTENTE = 999_999L;

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
    private ClienteService clienteService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RequestPostProcessor clienteA = user(CLIENTE_A_EMAIL).roles("CLIENTE");
    private final RequestPostProcessor clienteB = user(CLIENTE_B_EMAIL).roles("CLIENTE");
    private final RequestPostProcessor admin = user(ADMIN_EMAIL).roles("ADMIN");

    private Long clienteBId;
    private Long contaAId;
    private Long contaBId;

    @BeforeEach
    void prepararDados() {
        transacaoRepository.deleteAll();
        contaRepository.deleteAll();
        clienteRepository.deleteAll();

        // Cadastrado pelo service para a senha ficar com BCrypt e permitir login real.
        Cliente a = clienteService.cadastrarCliente(
                new Cliente("Cliente A", CLIENTE_A_CPF, CLIENTE_A_EMAIL, CLIENTE_A_SENHA));
        Cliente b = clienteRepository.save(
                new Cliente("Cliente B", "22222222222", CLIENTE_B_EMAIL, "senha-codificada"));
        Cliente administrador = clienteRepository.save(
                new Cliente("Administrador", "33333333333", ADMIN_EMAIL, "senha-codificada"));
        // Não existe endpoint para promover usuário; em produção isso também seria feito no banco.
        jdbcTemplate.update("UPDATE tb_clientes SET role = 'ADMIN' WHERE id = ?", administrador.getId());

        clienteBId = b.getId();

        ContaPoupanca contaA = new ContaPoupanca(2001, a);
        contaA.depositar(new BigDecimal("20.00"));
        contaAId = contaRepository.saveAndFlush(contaA).getId();

        ContaPoupanca contaB = new ContaPoupanca(2002, b);
        contaB.depositar(new BigDecimal("20.00"));
        contaBId = contaRepository.saveAndFlush(contaB).getId();
    }

    // =====================================================================
    // 2xx - o que cada perfil pode fazer
    // =====================================================================

    @Test
    @DisplayName("[201/200] Anônimo se cadastra, faz login e usa o token JWT real")
    void anonimoSeCadastraELoga() throws Exception {
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ClienteRequestDTO(
                                "Cliente Novo", "11144477735", "novo@example.test", "SenhaForte123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("novo@example.test"));

        String token = login("novo@example.test", "SenhaForte123");

        mockMvc.perform(get("/clientes/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Cliente Novo"));
    }

    @Test
    @DisplayName("[200] CLIENTE consulta e movimenta as próprias contas")
    void clienteUsaAsPropriasContas() throws Exception {
        mockMvc.perform(get("/clientes/me").with(clienteA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(CLIENTE_A_EMAIL));

        mockMvc.perform(get("/contas/minhas").with(clienteA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(post("/transacoes/depositar").with(clienteA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new DepositoRequestDTO(contaAId, new BigDecimal("5.00")))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/contas/{id}", contaAId).with(clienteA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(25.00));

        mockMvc.perform(get("/transacoes/conta/{contaId}", contaAId).with(clienteA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("[200] ADMIN acessa a área administrativa e contas de qualquer cliente")
    void adminAcessaAreaAdministrativa() throws Exception {
        mockMvc.perform(get("/admin/clientes").with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        mockMvc.perform(get("/contas").with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/clientes/{id}", clienteBId).with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(CLIENTE_B_EMAIL));

        mockMvc.perform(get("/contas/{id}", contaBId).with(admin))
                .andExpect(status().isOk());
    }

    // =====================================================================
    // 400 Bad Request - dados inválidos ou regra de negócio violada
    // =====================================================================

    @Test
    @DisplayName("[400] Cadastro com CPF, e-mail e senha inválidos lista o erro de cada campo")
    void cadastroComCamposInvalidos() throws Exception {
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ClienteRequestDTO(
                                "Cliente", "12345678900", "nao-e-email", "curta"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.cpf").exists())
                .andExpect(jsonPath("$.campos.email").exists())
                .andExpect(jsonPath("$.campos.senha").exists());
    }

    @Test
    @DisplayName("[400] CPF já cadastrado")
    void cpfDuplicado() throws Exception {
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ClienteRequestDTO(
                                "Outro", CLIENTE_A_CPF, "outro@example.test", "SenhaForte123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cliente já cadastrado com o CPF: " + CLIENTE_A_CPF));
    }

    @Test
    @DisplayName("[400] JSON mal formatado")
    void jsonMalFormatado() throws Exception {
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"sem fechar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Corpo da requisição inválido ou mal formatado."));
    }

    @Test
    @DisplayName("[400] Tipo de conta inexistente no enum")
    void tipoDeContaInvalido() throws Exception {
        mockMvc.perform(post("/contas").with(clienteA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numero\": 3001, \"clienteId\": 1, \"tipoConta\": \"INVESTIMENTO\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[400] Número de conta já existente")
    void numeroDeContaDuplicado() throws Exception {
        mockMvc.perform(post("/contas").with(clienteA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ContaRequestDTO(2001, 1L, TipoConta.CORRENTE, null, null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Conta já cadastrada com o número: 2001"));
    }

    @Test
    @DisplayName("[400] ID de conta que não é número na URL")
    void idDeContaComTipoErrado() throws Exception {
        mockMvc.perform(get("/contas/abc").with(clienteA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Valor inválido para o parâmetro 'id'."));
    }

    @Test
    @DisplayName("[400] Depósito com valor negativo")
    void depositoComValorNegativo() throws Exception {
        mockMvc.perform(post("/transacoes/depositar").with(clienteA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new DepositoRequestDTO(contaAId, new BigDecimal("-5.00")))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.valor").exists());
    }

    @Test
    @DisplayName("[400] Saque maior que o saldo da poupança")
    void saqueSemSaldo() throws Exception {
        mockMvc.perform(post("/transacoes/sacar").with(clienteA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new SaqueRequestDTO(contaAId, new BigDecimal("1000.00")))))
                .andExpect(status().isBadRequest());

        assertThat(contaRepository.findById(contaAId).orElseThrow().getSaldo())
                .isEqualByComparingTo("20.00");
    }

    @Test
    @DisplayName("[400] Transferência para a própria conta")
    void transferenciaParaMesmaConta() throws Exception {
        mockMvc.perform(post("/transacoes/transferir").with(clienteA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new TransferenciaRequestDTO(contaAId, contaAId, BigDecimal.ONE))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[400] Extrato sem parâmetros obrigatórios ou com período invertido")
    void extratoComParametrosInvalidos() throws Exception {
        mockMvc.perform(get("/transacoes/extrato").with(clienteA)
                        .param("contaId", contaAId.toString()))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/transacoes/extrato").with(clienteA)
                        .param("contaId", contaAId.toString())
                        .param("dataInicio", "2027-01-01T00:00:00")
                        .param("dataFim", "2026-01-01T00:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Data de início não pode ser posterior à data de fim"));
    }

    @Test
    @DisplayName("[400] Login com e-mail inválido e senha em branco")
    void loginComCorpoInvalido() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "nao-e-email", "senha", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.email").exists())
                .andExpect(jsonPath("$.campos.senha").exists());
    }

    // =====================================================================
    // 401 Unauthorized - sem autenticação válida
    // =====================================================================

    @Test
    @DisplayName("[401] Anônimo tenta acessar rotas protegidas")
    void anonimoEmRotaProtegida() throws Exception {
        mockMvc.perform(get("/contas/minhas"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/admin/clientes"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/transacoes/depositar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new DepositoRequestDTO(contaAId, BigDecimal.ONE))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("[401] Token JWT inválido")
    void tokenInvalido() throws Exception {
        mockMvc.perform(get("/contas/minhas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("[401] Login com senha errada ou e-mail inexistente")
    void loginComCredenciaisErradas() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", CLIENTE_A_EMAIL, "senha", "SenhaErrada999"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("E-mail ou senha inválidos."));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "ninguem@example.test", "senha", "Qualquer123"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("E-mail ou senha inválidos."));
    }

    // =====================================================================
    // 403 Forbidden - autenticado, mas sem permissão
    // =====================================================================

    @Test
    @DisplayName("[403] CLIENTE tenta usar rotas exclusivas de ADMIN")
    void clienteEmRotaDeAdmin() throws Exception {
        mockMvc.perform(get("/admin/clientes").with(clienteA))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/contas").with(clienteA))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/clientes/{id}", clienteBId).with(clienteA))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/clientes/{id}", clienteBId).with(clienteA))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("[403] ADMIN em /clientes/me (regra atual: rota liberada só para CLIENTE)")
    void adminEmClientesMe() throws Exception {
        mockMvc.perform(get("/clientes/me").with(admin))
                .andExpect(status().isForbidden());
    }

    // =====================================================================
    // 404 Not Found - recurso inexistente ou de outro cliente
    // =====================================================================

    @Test
    @DisplayName("[404] CLIENTE consulta conta de outro cliente (não revela que ela existe)")
    void clienteConsultaContaAlheia() throws Exception {
        mockMvc.perform(get("/contas/{id}", contaBId).with(clienteA))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/transacoes/conta/{contaId}", contaBId).with(clienteA))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("[404] ADMIN busca conta e cliente inexistentes")
    void adminBuscaRecursosInexistentes() throws Exception {
        mockMvc.perform(get("/contas/{id}", ID_INEXISTENTE).with(admin))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/clientes/{id}", ID_INEXISTENTE).with(admin))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/clientes/{id}", ID_INEXISTENTE).with(admin))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("[404] Rota que não existe")
    void rotaInexistente() throws Exception {
        mockMvc.perform(get("/rota-que-nao-existe").with(clienteA))
                .andExpect(status().isNotFound());
    }

    // =====================================================================
    // 405 Method Not Allowed e 415 Unsupported Media Type
    // =====================================================================

    @Test
    @DisplayName("[405] Método HTTP não suportado pela rota")
    void metodoNaoPermitido() throws Exception {
        mockMvc.perform(put("/contas").with(clienteA))
                .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(get("/transacoes/depositar").with(clienteA))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("[415] Corpo enviado como texto em vez de JSON")
    void tipoDeConteudoNaoSuportado() throws Exception {
        mockMvc.perform(post("/transacoes/depositar").with(clienteA)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("contaId=1&valor=10"))
                .andExpect(status().isUnsupportedMediaType());
    }

    // =====================================================================
    // 409 Conflict - operação incompatível com o estado da conta
    // =====================================================================

    @Test
    @DisplayName("[400] Encerrar conta que ainda tem saldo")
    void encerrarContaComSaldo() throws Exception {
        mockMvc.perform(delete("/contas/{id}", contaAId).with(clienteA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Só é possível encerrar uma conta com saldo zero"));
    }

    @Test
    @DisplayName("[409] Movimentar conta encerrada, como origem ou como destino")
    void movimentarContaEncerrada() throws Exception {
        // Regra de negócio: a conta precisa estar zerada para ser encerrada.
        mockMvc.perform(post("/transacoes/sacar").with(clienteA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new SaqueRequestDTO(contaAId, new BigDecimal("20.00")))))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/contas/{id}", contaAId).with(clienteA))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/transacoes/sacar").with(clienteA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new SaqueRequestDTO(contaAId, BigDecimal.ONE))))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/transacoes/transferir").with(clienteB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new TransferenciaRequestDTO(contaBId, contaAId, BigDecimal.ONE))))
                .andExpect(status().isConflict());

        assertThat(contaRepository.findById(contaBId).orElseThrow().getSaldo())
                .isEqualByComparingTo("20.00");
    }

    // =====================================================================
    // Auxiliares
    // =====================================================================

    private String login(String email, String senha) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "senha", senha))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(resultado.getResponse().getContentAsString())
                .get("token")
                .asText();
    }

    private byte[] json(Object corpo) throws Exception {
        return objectMapper.writeValueAsBytes(corpo);
    }
}