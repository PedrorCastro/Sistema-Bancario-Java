package org.pedro.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.pedro.exception.SaldoInsuficienteException;
import org.pedro.exception.ValorInvalidoException;
import org.pedro.exception.ContaNaoEncontradaException;
import org.pedro.model.Conta;
import org.pedro.model.Transacao;
import org.pedro.repository.ContaRepository;
import org.pedro.repository.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service 
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final ContaRepository contaRepository;
    private final ContaService contaService;

    public TransacaoService(
            TransacaoRepository transacaoRepository,
            ContaRepository contaRepository,
            ContaService contaService) {
        this.transacaoRepository = transacaoRepository;
        this.contaRepository = contaRepository;
        this.contaService = contaService;
    }

    @Transactional
    public void depositar(Long contaId, BigDecimal valor) {
        if (contaId == null || valor == null) {
            throw new IllegalArgumentException("ContaId e valor não podem ser nulos");
        }
        Conta conta = contaService.buscarPorIdParaAtualizacao(contaId)
                .orElseThrow(ContaNaoEncontradaException::new);

        conta.depositar(valor);
        contaRepository.save(conta);
    }

    @Transactional
    public void sacar(Long contaId, BigDecimal valor) throws SaldoInsuficienteException, ValorInvalidoException {
        if (contaId == null || valor == null) {
            throw new IllegalArgumentException("ContaId e valor não podem ser nulos");
        }
        Conta conta = contaService.buscarPorIdParaAtualizacao(contaId)
                .orElseThrow(ContaNaoEncontradaException::new);

        conta.sacar(valor);
        contaRepository.save(conta);
    }

    @Transactional
    public void transferir(Long contaIdOrigem, Long contaIdDestino, BigDecimal valor)
            throws SaldoInsuficienteException, ValorInvalidoException {
        if (contaIdOrigem == null || contaIdDestino == null || valor == null) {
            throw new IllegalArgumentException("ContaIdOrigem, ContaIdDestino e valor não podem ser nulos");
        }
        if (contaIdOrigem.equals(contaIdDestino)) {
            throw new IllegalArgumentException("A conta de origem e a conta de destino não podem ser a mesma");
        }

        contaService.verificarAcesso(contaIdOrigem);

        Long primeiroId = Math.min(contaIdOrigem, contaIdDestino);
        Long segundoId = Math.max(contaIdOrigem, contaIdDestino);
        Conta contaOrigem;
        Conta contaDestino;
        if (contaIdOrigem.equals(primeiroId)) {
            contaOrigem = contaService.buscarPorIdParaAtualizacao(contaIdOrigem)
                    .orElseThrow(ContaNaoEncontradaException::new);
            contaDestino = bloquearConta(contaIdDestino);
        } else {
            contaDestino = bloquearConta(contaIdDestino);
            contaOrigem = contaService.buscarPorIdParaAtualizacao(contaIdOrigem)
                    .orElseThrow(ContaNaoEncontradaException::new);
        }

        contaOrigem.transferirPara(contaDestino, valor);

        contaRepository.save(contaOrigem);
        contaRepository.save(contaDestino);
    }

    private Conta bloquearConta(Long contaId) {
        return contaRepository.lockRowById(contaId)
                .flatMap(contaRepository::findById)
                .orElseThrow(ContaNaoEncontradaException::new);
    }

    @Transactional(readOnly = true)
    public List<Transacao> gerarExtrato(Long contaId, LocalDateTime dataInicio, LocalDateTime dataFim) {
        if (contaId == null || dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("ContaId, dataInicio e dataFim não podem ser nulos");
        }
        if (dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("Data de início não pode ser posterior à data de fim");
        }
        contaService.buscarPorId(contaId)
                .orElseThrow(ContaNaoEncontradaException::new);
        return transacaoRepository.findByContaIdAndDataBetween(contaId, dataInicio, dataFim);
    }

    @Transactional(readOnly = true)
    public List<Transacao> findByContaId(Long contaId) {
        if (contaId == null) {
            throw new IllegalArgumentException("ContaId não pode ser nulo");
        }
        contaService.buscarPorId(contaId)
                .orElseThrow(ContaNaoEncontradaException::new);
        return transacaoRepository.findByContaId(contaId);
    }
}
