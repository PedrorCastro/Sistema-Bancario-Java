package org.pedro.dto.response;

public record ContaResponseDTO(
    Long id,
    String tipoConta,
    Long saldo,
    Long clienteId
) {

}
