package org.pedro.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteRequestDTO(

    @NotBlank(message = "O nome não pode estar em branco")
    String nome,

    @NotBlank(message = "O CPF não pode estar em branco")
    @Pattern(
        regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}",
        message = "O CPF deve estar no formato XXX.XXX.XXX-XX"
    )
    String cpf,

    @NotBlank(message = "O email não pode estar em branco")
    @Email(message = "O email deve ser válido")
    String email,

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
    String senha

) {}