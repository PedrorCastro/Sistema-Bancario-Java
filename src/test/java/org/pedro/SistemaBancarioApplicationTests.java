package org.pedro;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * [NOVO]
 * Teste de integração do Spring Boot.
 * Valida se o contexto da aplicação, todas as entidades JPA, dialeto do banco H2,
 * mapeamentos relacionais e repositórios inicializam com sucesso e sem falhas de mapeamento.
 */
@SpringBootTest
@ActiveProfiles("test")
class SistemaBancarioApplicationTests {

    @Test
    @DisplayName("Contexto do Spring Boot e mapeamentos JPA devem carregar com sucesso")
    void contextLoads() {
    }
}
