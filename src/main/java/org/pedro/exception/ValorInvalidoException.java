package org.pedro.exception;

/**
 * [MANTIDO / DOCUMENTADO]
 * Estende RuntimeException para representar valores inválidos em operações bancárias (ex: valores <= 0 ou nulos).
 * Permite rollback automático em transações gerenciadas pelo Spring.
 */
public class ValorInvalidoException extends RuntimeException {
    public ValorInvalidoException(String message) {
        super(message);
    }
}
