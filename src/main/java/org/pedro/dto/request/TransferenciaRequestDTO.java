package org.pedro.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;

public record TransferenciaRequestDTO(
    @NotBlank(message = "O campo contaOrigemId não pode estar em branco")
    Long contaOrigemId,
    @NotBlank(message = "O campo contaDestinoId não pode estar em branco")
    Long contaDestinoId,
    @NotBlank(message = "O campo valor não pode estar em branco")
    BigDecimal valor


) {

}
