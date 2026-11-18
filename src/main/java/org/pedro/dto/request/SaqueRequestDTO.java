package org.pedro.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SaqueRequestDTO(
    @NotNull(message = "O campo contaId é obrigatório")
    @Positive(message = "O campo contaId deve ser positivo")
    Long contaId,
    @NotNull(message = "O campo valor é obrigatório")
    @Positive(message = "O campo valor deve ser positivo")
    BigDecimal valor
) {

}
