package org.pedro.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthRequestDTO(
    @NotBlank(message = "O email nao pode ser em branco")
    @Email(message = "Informe um email valido")
    String email,

    @NotBlank(message = "A senha nao pode ser em branco")
    String senha
){

}