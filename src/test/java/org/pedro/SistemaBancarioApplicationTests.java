package org.pedro;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SistemaBancarioApplicationTests {

    @Test
    @DisplayName("Contexto do Spring Boot e mapeamentos JPA devem carregar com sucesso")
    void contextLoads() {
    }
}
