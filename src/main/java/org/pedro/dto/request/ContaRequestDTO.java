package org.pedro.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ContaRequestDTO(

    @NotNull(message = "O número da conta é obrigatório")
    @Positive(message = "O número deve ser positivo")
    Integer numero,

    @NotNull(message = "O cliente é obrigatório")
    @Positive
    Long clienteId,

    @NotNull(message = "O tipo da conta é obrigatório")
    TipoConta tipoConta,

    @PositiveOrZero
    Long limite,

    @PositiveOrZero
    Long saldo

) {}