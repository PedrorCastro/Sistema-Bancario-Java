package org.pedro.model;

import org.junit.jupiter.api.Test;
import org.pedro.exception.ContaEncerradaException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContaTransferenciaTest {

    @Test
    void transferenciaCriaLancamentosComTipoCorretoNasDuasContas() {
        ContaPoupanca origem = new ContaPoupanca(1, null);
        ContaPoupanca destino = new ContaPoupanca(2, null);
        origem.depositar(new BigDecimal("100.00"));

        origem.transferirPara(destino, new BigDecimal("25.00"));

        assertEquals(new BigDecimal("75.00"), origem.getSaldo());
        assertEquals(new BigDecimal("25.00"), destino.getSaldo());
        assertEquals(TipoTransacao.TRANSFERENCIA,
                origem.getHistorico().get(1).getTipo());
        assertEquals(destino, origem.getHistorico().get(1).getContaContraparte());
        assertEquals(TipoTransacao.TRANSFERENCIA,
                destino.getHistorico().get(0).getTipo());
        assertEquals(origem, destino.getHistorico().get(0).getContaContraparte());
    }

    @Test
    void contasTransientComMesmoNumeroNaoSaoIguaisEAIdentidadeNaoMudaComNumero() {
        ContaPoupanca primeira = new ContaPoupanca(10, null);
        ContaPoupanca segunda = new ContaPoupanca(10, null);

        assertNotEquals(primeira, segunda);
        primeira.setId(1L);
        segunda.setId(1L);
        int hashCode = primeira.hashCode();

        primeira.setNumero(20);

        assertEquals(primeira, segunda);
        assertEquals(hashCode, primeira.hashCode());
    }

    @Test
    void encerramentoPreservaHistoricoEImpedeNovasMovimentacoes() {
        ContaPoupanca conta = new ContaPoupanca(3, null);
        conta.depositar(new BigDecimal("10.00"));
        conta.sacar(new BigDecimal("10.00"));

        conta.encerrar();

        assertEquals(StatusConta.ENCERRADA, conta.getStatus());
        assertEquals(2, conta.getHistorico().size());
        assertThrows(ContaEncerradaException.class,
                () -> conta.depositar(BigDecimal.ONE));
        assertThrows(ContaEncerradaException.class,
                () -> conta.sacar(BigDecimal.ONE));
    }

    @Test
    void naoEncerraContaComSaldoDiferenteDeZero() {
        ContaPoupanca conta = new ContaPoupanca(4, null);
        conta.depositar(new BigDecimal("1.00"));

        assertThrows(IllegalArgumentException.class, conta::encerrar);
        assertEquals(StatusConta.ATIVA, conta.getStatus());
    }
}
