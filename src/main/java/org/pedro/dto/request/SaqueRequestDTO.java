package org.pedro.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;

public record SaqueRequestDTO(
    @NotBlank(message = "O campo contaId não pode estar em branco")
    Long contaId,
    @NotBlank(message = "O campo valor não pode estar em branco")
    BigDecimal valor
) {

}
