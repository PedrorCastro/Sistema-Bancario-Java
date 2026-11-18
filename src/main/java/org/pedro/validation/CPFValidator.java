
package org.pedro.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CPFValidator implements ConstraintValidator<ValidCPF, String> {

    @Override
    public boolean isValid(String cpf, ConstraintValidatorContext context) {

        // 1. CPF nulo ou vazio será tratado por @NotBlank
        if (cpf == null || cpf.isBlank()) {
            return true;
        }

        // 2. Remove pontos e traço
        cpf = cpf.replaceAll("[^0-9]", "");

        // 3. Verifica se possui exatamente 11 dígitos
        if (cpf.length() != 11) {
            return false;
        }

        // 4. Rejeita sequências repetidas
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        // 5. Valida o primeiro dígito verificador
        int primeiroDigito = calcularDigito(cpf, 9);

        if (primeiroDigito != Character.getNumericValue(cpf.charAt(9))) {
            return false;
        }

        // 6. Valida o segundo dígito verificador
        int segundoDigito = calcularDigito(cpf, 10);

        return segundoDigito == Character.getNumericValue(cpf.charAt(10));
    }

    private int calcularDigito(String cpf, int quantidadeDigitos) {

        int soma = 0;

        for (int i = 0; i < quantidadeDigitos; i++) {
            int digito = Character.getNumericValue(cpf.charAt(i));
            soma += digito * (quantidadeDigitos + 1 - i);
        }

        int resto = soma % 11;

        return resto < 2 ? 0 : 11 - resto;
    }
}
