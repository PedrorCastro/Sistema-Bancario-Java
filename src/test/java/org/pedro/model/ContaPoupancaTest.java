package org.pedro.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pedro.exception.SaldoInsuficienteException;
import org.pedro.exception.ValorInvalidoException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * [ALTERADO]
 * Mudanças:
 * 1. Declarado pacote 'org.pedro.model'.
 * 2. Adicionados testes de depósito com sucesso, saque com saldo suficiente e saque sem saldo.
 */
public class ContaPoupancaTest {

    private Cliente cliente;
    private ContaPoupanca conta;

    @BeforeEach
    void setUp() {
        cliente = new Cliente("Pedro", "12345678900", "pedro@teste.com");
        conta = new ContaPoupanca(2001, cliente);
    }

    @Test
    @DisplayName("Deve lançar exceção ao depositar valor negativo ou zero")
    void deveLancarExcecaoAoDepositarValorNegativo() {
        assertThrows(ValorInvalidoException.class, () -> conta.depositar(-1000L));
        assertThrows(ValorInvalidoException.class, () -> conta.depositar(0L));
    }

    @Test
    @DisplayName("Deve lançar exceção ao sacar valor negativo ou zero")
    void deveLancarExcecaoAoSacarValorNegativo() {
        assertThrows(ValorInvalidoException.class, () -> conta.sacar(-1000L));
        assertThrows(ValorInvalidoException.class, () -> conta.sacar(0L));
    }

    @Test
    @DisplayName("Deve realizar depósito com sucesso")
    void deveRealizarDepositoComSucesso() {
        conta.depositar(300L);
        assertEquals(300L, conta.getSaldo());
        assertEquals(1, conta.getHistorico().size());
        assertEquals(TipoTransacao.DEPOSITO, conta.getHistorico().get(0).getTipo());
    }

    @Test
    @DisplayName("Deve permitir saque quando houver saldo suficiente")
    void devePermitirSaqueComSaldoSuficiente() {
        conta.depositar(500L);
        conta.sacar(250L);

        assertEquals(250L, conta.getSaldo());
        assertEquals(2, conta.getHistorico().size());
        assertEquals(TipoTransacao.SAQUE, conta.getHistorico().get(1).getTipo());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar sacar sem saldo suficiente (Poupança não possui limite)")
    void deveLancarExcecaoSemSaldoSuficiente() {
        conta.depositar(100L);
        assertThrows(SaldoInsuficienteException.class, () -> conta.sacar(150L));
        assertEquals(100L, conta.getSaldo());
    }
}
