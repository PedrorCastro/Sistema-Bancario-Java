package org.pedro;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pedro.dto.request.DepositoRequestDTO;
import org.pedro.service.TransacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Simula uma falha inesperada (por exemplo, o banco caindo no meio de uma operação)
 * para verificar que a API responde 500 sem expor detalhes internos ao cliente.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ErroInternoTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransacaoService transacaoService;

    @Test
    @DisplayName("[500] Falha inesperada devolve mensagem genérica, sem detalhes internos")
    void falhaInesperadaNaoVazaDetalhes() throws Exception {
        doThrow(new IllegalStateException("falha SQL: conexão perdida com tb_contas"))
                .when(transacaoService).depositar(any(), any());

        mockMvc.perform(post("/transacoes/depositar")
                        .with(user("cliente@example.test").roles("CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(
                                new DepositoRequestDTO(1L, BigDecimal.TEN))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Erro interno no servidor. Tente novamente mais tarde."))
                .andExpect(content().string(not(containsString("SQL"))))
                .andExpect(content().string(not(containsString("tb_contas"))));
    }
}
