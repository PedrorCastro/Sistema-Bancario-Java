package org.pedro.dto.response;


public record AuthResponseDTO(
    String token,
    String tipo,
    Long expiresIn
){

}