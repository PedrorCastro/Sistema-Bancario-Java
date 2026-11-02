package org.pedro.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DepositoRequestDTO(
    @NotBlank (message = "O campo contaId não pode estar em branco")
    Long contaId,
    @NotBlank (message = "O campo valor não pode estar em branco")
    Double valor
) {

}
