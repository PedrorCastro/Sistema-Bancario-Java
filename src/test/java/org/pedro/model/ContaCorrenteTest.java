package org.pedro.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.pedro.exception.SaldoInsuficienteException;
import org.pedro.exception.ValorInvalidoException;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;


public class ContaCorrenteTest {

    private Cliente cliente;
    private ContaCorrente conta;

    @BeforeEach
    void setUp() {
        cliente = new Cliente("Pedro", "12345678900", "pedro@teste.com", "123456789");
        conta = new ContaCorrente(1001, cliente, 1000L); // Limite de 1000
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar depositar valor negativo ou zero")
    void deveLancarExcecaoAoDepositarValorNegativo() {
        ValorInvalidoException exNegativo = assertThrows(ValorInvalidoException.class, () -> conta.depositar(BigDecimal.valueOf(-1000)));
        assertTrue(exNegativo.getMessage().contains("Transação inválida"));

        ValorInvalidoException exZero = assertThrows(ValorInvalidoException.class, () -> conta.depositar(BigDecimal.ZERO));
        assertTrue(exZero.getMessage().contains("Transação inválida"));
    }

    @Test
    @DisplayName("Deve realizar depósito com sucesso e atualizar saldo e histórico")
    void deveRealizarDepositoComSucesso() {
        conta.depositar(BigDecimal.valueOf(500));

        assertEquals(BigDecimal.valueOf(500), conta.getSaldo());
        assertEquals(1, conta.getHistorico().size());
        assertEquals(TipoTransacao.DEPOSITO, conta.getHistorico().get(0).getTipo());
        assertEquals(BigDecimal.valueOf(500), conta.getHistorico().get(0).getValor());
    }

    @Test
    @DisplayName("Deve permitir saque dentro do saldo próprio")
    void devePermitirSaqueDentroDoSaldo() {
        conta.depositar(BigDecimal.valueOf(500));
        conta.sacar(BigDecimal.valueOf(200));

        assertEquals(BigDecimal.valueOf(300), conta.getSaldo());
        assertEquals(2, conta.getHistorico().size());
    }

    @Test
    @DisplayName("Deve permitir saque utilizando o limite de crédito")
    void devePermitirSaqueUsandoLimite() {
        conta.depositar(BigDecimal.valueOf(100));
        // Saldo = 100, Limite = 1000 -> Disponível = 1100. Saque de 500 deixa saldo em -400.
        conta.sacar(BigDecimal.valueOf(500));

        assertEquals(BigDecimal.valueOf(-400), conta.getSaldo());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar sacar valor que excede saldo + limite")
    void deveLancarExcecaoAoExcederSaldoELimite() {
        conta.depositar(BigDecimal.valueOf(100));
        // Disponível = 1100. Saque de 1200 deve falhar.
        assertThrows(SaldoInsuficienteException.class, () -> conta.sacar(BigDecimal.valueOf(1200)));
        assertEquals(BigDecimal.valueOf(100), conta.getSaldo()); // Saldo intacto
    }
}
