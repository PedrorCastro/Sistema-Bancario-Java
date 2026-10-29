package org.pedro.exception;

/**
 * [ALTERADO]
 * Mudança: Alterado para estender RuntimeException (exceção não-checada).
 * Motivo:
 * 1. Padronização com ValorInvalidoException.
 * 2. Integração com o Spring @Transactional: por padrão, o Spring só realiza rollback
 *    automático para exceções que herdam de RuntimeException.
 * 3. Evita poluição de 'throws' desnecessários em métodos de serviço e modelos.
 */
public class SaldoInsuficienteException extends RuntimeException {
    public SaldoInsuficienteException(String mensagem) {
        super(mensagem);
    }
}
