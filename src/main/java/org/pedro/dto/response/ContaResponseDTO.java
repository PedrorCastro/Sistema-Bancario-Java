package org.pedro.dto.response;

import java.math.BigDecimal;

public record ContaResponseDTO(
    Long id,
    String tipoConta,
    Long saldo,
    Long clienteId
) {

}
