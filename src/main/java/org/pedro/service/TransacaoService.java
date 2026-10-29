package org.pedro.service;

import java.time.LocalDateTime;
import java.util.List;

import org.pedro.exception.SaldoInsuficienteException;
import org.pedro.exception.ValorInvalidoException;
import org.pedro.model.Conta;
import org.pedro.model.TipoTransacao;
import org.pedro.model.Transacao;
import org.pedro.repository.ContaRepository;
import org.pedro.repository.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service 
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final ContaRepository contaRepository;

    public TransacaoService(TransacaoRepository transacaoRepository, ContaRepository contaRepository) {
        this.transacaoRepository = transacaoRepository;
        this.contaRepository = contaRepository;
    }

    @Transactional
    public void depositar(Long contaId, Long valor) {
        if (contaId == null || valor == null) {
            throw new IllegalArgumentException("ContaId e valor não podem ser nulos");
        }
        Conta conta = contaRepository.findById(contaId)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada com o ID: " + contaId));

        conta.depositar(valor);
        contaRepository.save(conta);
    }

    @Transactional
    public void sacar(Long contaId, Long valor) throws SaldoInsuficienteException, ValorInvalidoException {
        if (contaId == null || valor == null) {
            throw new IllegalArgumentException("ContaId e valor não podem ser nulos");
        }
        Conta conta = contaRepository.findById(contaId)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada com o ID: " + contaId));

        conta.sacar(valor);
        contaRepository.save(conta);
    }

    @Transactional
    public void transferir(Long contaIdOrigem, Long contaIdDestino, Long valor)
            throws SaldoInsuficienteException, ValorInvalidoException {
        if (contaIdOrigem == null || contaIdDestino == null || valor == null) {
            throw new IllegalArgumentException("ContaIdOrigem, ContaIdDestino e valor não podem ser nulos");
        }
        if (contaIdOrigem.equals(contaIdDestino)) {
            throw new IllegalArgumentException("A conta de origem e a conta de destino não podem ser a mesma");
        }

        Conta contaOrigem = contaRepository.findById(contaIdOrigem)
                .orElseThrow(() -> new IllegalArgumentException("Conta de origem não encontrada com ID: " + contaIdOrigem));
        Conta contaDestino = contaRepository.findById(contaIdDestino)
                .orElseThrow(() -> new IllegalArgumentException("Conta de destino não encontrada com ID: " + contaIdDestino));

        // Executa o débito com validação de saldo/limite
        contaOrigem.sacar(valor);
        // Atualiza a transação gerada para o tipo TRANSFERENCIA
        if (!contaOrigem.getHistorico().isEmpty()) {
            contaOrigem.getHistorico().get(contaOrigem.getHistorico().size() - 1).setTipo(TipoTransacao.TRANSFERENCIA);
        }

        // Executa o crédito
        contaDestino.depositar(valor);
        // Atualiza a transação gerada para o tipo TRANSFERENCIA
        if (!contaDestino.getHistorico().isEmpty()) {
            contaDestino.getHistorico().get(contaDestino.getHistorico().size() - 1).setTipo(TipoTransacao.TRANSFERENCIA);
        }

        contaRepository.save(contaOrigem);
        contaRepository.save(contaDestino);
    }

    @Transactional(readOnly = true)
    public List<Transacao> gerarExtrato(Long contaId, LocalDateTime dataInicio, LocalDateTime dataFim) {
        if (contaId == null || dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("ContaId, dataInicio e dataFim não podem ser nulos");
        }
        if (dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("Data de início não pode ser posterior à data de fim");
        }
        if (!contaRepository.existsById(contaId)) {
            throw new IllegalArgumentException("Conta não encontrada com o ID: " + contaId);
        }
        return transacaoRepository.findByContaIdAndDataBetween(contaId, dataInicio, dataFim);
    }
}
