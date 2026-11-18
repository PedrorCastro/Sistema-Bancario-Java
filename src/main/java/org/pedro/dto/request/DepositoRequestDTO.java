package org.pedro.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record DepositoRequestDTO(
    @NotNull(message = "O campo contaId é obrigatório")
    @Positive(message = "O campo contaId deve ser positivo")
    Long contaId,
    @NotNull(message = "O campo valor é obrigatório")
    @Positive(message = "O campo valor deve ser positivo")
    BigDecimal valor
) {

}
