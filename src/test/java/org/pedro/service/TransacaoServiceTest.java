package org.pedro.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pedro.exception.ContaNaoEncontradaException;
import org.pedro.model.ContaPoupanca;
import org.pedro.model.TipoTransacao;
import org.pedro.repository.ContaRepository;
import org.pedro.repository.TransacaoRepository;

import java.math.BigDecimal;
import java.util.Optional;

import org.mockito.InOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransacaoServiceTest {

    private ContaRepository contaRepository;
    private ContaService contaService;
    private TransacaoService transacaoService;

    @BeforeEach
    void setUp() {
        contaRepository = mock(ContaRepository.class);
        TransacaoRepository transacaoRepository = mock(TransacaoRepository.class);
        contaService = mock(ContaService.class);
        transacaoService = new TransacaoService(
                transacaoRepository, contaRepository, contaService);
    }

    @Test
    void depositoUsaContaAutorizadaBloqueada() {
        ContaPoupanca conta = new ContaPoupanca(1, null);
        when(contaService.buscarPorIdParaAtualizacao(1L)).thenReturn(Optional.of(conta));

        transacaoService.depositar(1L, new BigDecimal("20.00"));

        assertEquals(new BigDecimal("20.00"), conta.getSaldo());
        verify(contaService).buscarPorIdParaAtualizacao(1L);
        verify(contaRepository).save(conta);
    }

    @Test
    void depositoDeContaSemAcessoNaoAlteraSaldoNemAdquireLock() {
        when(contaService.buscarPorIdParaAtualizacao(1L)).thenReturn(Optional.empty());

        assertThrows(ContaNaoEncontradaException.class,
                () -> transacaoService.depositar(1L, new BigDecimal("20.00")));

        verify(contaRepository, never()).lockRowById(1L);
        verify(contaRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void transferenciaBloqueiaEmOrdemDeIdEPermiteDestinoDeOutroTitular() {
        ContaPoupanca origem = new ContaPoupanca(1, null);
        origem.setId(1L);
        origem.depositar(new BigDecimal("100.00"));
        ContaPoupanca destino = new ContaPoupanca(2, null);
        destino.setId(2L);

        when(contaService.buscarPorIdParaAtualizacao(1L)).thenReturn(Optional.of(origem));
        when(contaRepository.lockRowById(2L)).thenReturn(Optional.of(2L));
        when(contaRepository.findById(2L)).thenReturn(Optional.of(destino));

        transacaoService.transferir(1L, 2L, new BigDecimal("25.00"));

        assertEquals(new BigDecimal("75.00"), origem.getSaldo());
        assertEquals(new BigDecimal("25.00"), destino.getSaldo());
        assertEquals(TipoTransacao.TRANSFERENCIA,
                origem.getHistorico().get(1).getTipo());
        assertEquals(destino,
                origem.getHistorico().get(1).getContaContraparte());
        assertEquals(TipoTransacao.TRANSFERENCIA,
                destino.getHistorico().get(0).getTipo());
        assertEquals(origem,
                destino.getHistorico().get(0).getContaContraparte());
        InOrder locks = inOrder(contaService, contaRepository);
        locks.verify(contaService).verificarAcesso(1L);
        locks.verify(contaService).buscarPorIdParaAtualizacao(1L);
        locks.verify(contaRepository).lockRowById(2L);
        locks.verify(contaRepository).findById(2L);
    }
}
