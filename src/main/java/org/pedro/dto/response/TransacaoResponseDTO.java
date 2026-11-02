package org.pedro.dto.response;

import java.time.LocalDateTime;

public record TransacaoResponseDTO(
    Long id,
    String tipoTransacao,
    Double valor,
    LocalDateTime dataHora,
    Long contaId
) {

}
