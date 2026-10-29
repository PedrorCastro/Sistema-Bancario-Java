package org.pedro.service;

import jakarta.transaction.Transactional;
import org.pedro.exception.SaldoInsuficienteException;
import org.pedro.exception.ValorInvalidoException;
import org.pedro.model.Conta;
import org.pedro.repository.ContaRepository;
import org.pedro.repository.TransacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.pedro.model.Transacao;

import java.time.LocalDateTime;
import java.util.List;

import org.pedro.model.TipoTransacao;

@Service 
public class TransacaoService {
    @Autowired 
    private TransacaoRepository transacaoRepository;
    @Autowired 
    private ContaRepository contaRepository;

    @Transactional
    public void depositar (Long contaId, Long valor){
        if (contaId == null || valor == null) {
            throw new IllegalArgumentException("ContaId e valor não podem ser nulos");
        } else {
            Conta conta = contaRepository.findById(contaId).orElseThrow(() -> new IllegalArgumentException("Conta não encontrada com o ID: " + contaId));

            conta.depositar(valor);

            contaRepository.save(conta);
            transacaoRepository.save(new Transacao(valor, TipoTransacao.DEPOSITO));
        }
    }
    @Transactional
    public void sacar (Long contaId, Long valor) throws SaldoInsuficienteException, ValorInvalidoException {
        if (contaId == null || valor == null) {
            throw new IllegalArgumentException("ContaId e valor não podem ser nulos");
        } else {
            Conta conta = contaRepository.findById(contaId).orElseThrow(() -> new IllegalArgumentException("Conta não encontrada com o ID: " + contaId));

            conta.sacar(valor);

            contaRepository.save(conta);
            transacaoRepository.save(new Transacao(valor, TipoTransacao.SAQUE));
        }
    }
    @Transactional
    public void transferir (Long contaIdOrigem, Long contaIdDestino, Long valor) throws SaldoInsuficienteException, ValorInvalidoException {
        if(contaIdOrigem == null || contaIdDestino == null || valor == null){
            throw new IllegalArgumentException("ContaIdOrigem, ContaIdDestino e valor não podem ser nulos");
        } else {
            Conta contaOrigem = contaRepository.findById(contaIdOrigem).orElseThrow(() -> new IllegalArgumentException("Conta de origem não encontrada"));
            Conta contaDestino = contaRepository.findById(contaIdDestino).orElseThrow(() -> new IllegalArgumentException("Conta de origem não encontrada"));

            contaOrigem.sacar(valor);
            contaDestino.depositar(valor);


            transacaoRepository.save(new Transacao(valor, TipoTransacao.TRANSFERENCIA));
        }
    }

    public List<Transacao> gerarExtrato(Long contaId, LocalDateTime dataInicio, LocalDateTime dataFim){
        if(contaId == null || dataInicio == null || dataFim == null){
            throw new IllegalArgumentException("ContaId, dataInicio e dataFim não podem ser nulos");
        } if (!contaRepository.existsById(contaId)) {
            throw new IllegalArgumentException("Conta não encontrada com o ID: " + contaId);

        }
        return transacaoRepository.findByContaIdAndDataBetween(contaId, dataInicio, dataFim);
    }
}
