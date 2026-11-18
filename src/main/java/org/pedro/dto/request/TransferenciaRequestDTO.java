package org.pedro.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransferenciaRequestDTO(
    @NotNull(message = "O campo contaOrigemId é obrigatório")
    @Positive(message = "O campo contaOrigemId deve ser positivo")
    Long contaOrigemId,
    @NotNull(message = "O campo contaDestinoId é obrigatório")
    @Positive(message = "O campo contaDestinoId deve ser positivo")
    Long contaDestinoId,
    @NotNull(message = "O campo valor é obrigatório")
    @Positive(message = "O campo valor deve ser positivo")
    BigDecimal valor


) {

}
