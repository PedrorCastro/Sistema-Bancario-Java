package org.pedro.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransacaoResponseDTO(
    Long id,
    String tipoTransacao,
    BigDecimal valor,
    LocalDateTime dataHora,
    Long contaId
) {

}
