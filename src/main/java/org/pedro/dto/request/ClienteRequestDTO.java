package org.pedro.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.pedro.validation.ValidCPF;

public record ClienteRequestDTO(

    @NotBlank(message = "O nome não pode estar em branco")
    String nome,

    @NotBlank(message = "O CPF não pode estar em branco")
    @ValidCPF
    String cpf,

    @NotBlank(message = "O email não pode estar em branco")
    @Email(message = "O email deve ser válido")
    String email,

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
    String senha

) {}