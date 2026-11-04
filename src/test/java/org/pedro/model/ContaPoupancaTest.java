package org.pedro.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pedro.exception.SaldoInsuficienteException;
import org.pedro.exception.ValorInvalidoException;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;


public class ContaPoupancaTest {

    private Cliente cliente;
    private ContaPoupanca conta;

    @BeforeEach
    void setUp() {
        cliente = new Cliente("Pedro", "12345678900", "pedro@teste.com", "123456789");
        conta = new ContaPoupanca(2001, cliente);
    }

    @Test
    @DisplayName("Deve lançar exceção ao depositar valor negativo ou zero")
    void deveLancarExcecaoAoDepositarValorNegativo() {
        assertThrows(ValorInvalidoException.class, () -> conta.depositar(BigDecimal.valueOf(-1000)));
        assertThrows(ValorInvalidoException.class, () -> conta.depositar(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Deve lançar exceção ao sacar valor negativo ou zero")
    void deveLancarExcecaoAoSacarValorNegativo() {
        assertThrows(ValorInvalidoException.class, () -> conta.sacar(BigDecimal.valueOf(-1000)));
        assertThrows(ValorInvalidoException.class, () -> conta.sacar(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Deve realizar depósito com sucesso")
    void deveRealizarDepositoComSucesso() {
        conta.depositar(BigDecimal.valueOf(300));
        assertEquals(BigDecimal.valueOf(300), conta.getSaldo());
        assertEquals(1, conta.getHistorico().size());
        assertEquals(TipoTransacao.DEPOSITO, conta.getHistorico().get(0).getTipo());
    }

    @Test
    @DisplayName("Deve permitir saque quando houver saldo suficiente")
    void devePermitirSaqueComSaldoSuficiente() {
        conta.depositar(BigDecimal.valueOf(500));
        conta.sacar(BigDecimal.valueOf(250));

        assertEquals(BigDecimal.valueOf(250), conta.getSaldo());
        assertEquals(2, conta.getHistorico().size());
        assertEquals(TipoTransacao.SAQUE, conta.getHistorico().get(1).getTipo());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar sacar sem saldo suficiente (Poupança não possui limite)")
    void deveLancarExcecaoSemSaldoSuficiente() {
        conta.depositar(BigDecimal.valueOf(100));

        assertThrows(SaldoInsuficienteException.class, () -> conta.sacar(BigDecimal.valueOf(150)));
        assertEquals(BigDecimal.valueOf(100), conta.getSaldo());
    }
}
