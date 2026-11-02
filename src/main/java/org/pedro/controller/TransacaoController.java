package org.pedro.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.pedro.service.TransacaoService;
import org.pedro.model.Transacao;

import java.time.LocalDateTime;
import java.util.List;

@RestController 
public class TransacaoController {

    private final TransacaoService transacaoService;

    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @PostMapping("/transacoes/depositar") 
    public void depositar(Long contaId, Long valor) {
        transacaoService.depositar(contaId, valor);

    }

    @PostMapping("/transacoes/sacar")
    public void sacar(Long contaId, Long valor) {
        transacaoService.sacar(contaId, valor);
    }

    @PostMapping("/transacoes/transferir")
    public void transferir(Long contaOrigemId, Long contaDestinoId, Long valor) {
        transacaoService.transferir(contaOrigemId, contaDestinoId, valor);
    }

    @GetMapping("/transacoes/extrato")
    public ResponseEntity<List<Transacao>> consultarTransacoes(
            @RequestParam Long contaId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dataInicio,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dataFim) {

        return ResponseEntity.ok(transacaoService.gerarExtrato(contaId, dataInicio, dataFim));
    }

    @GetMapping("/transacoes/conta/{contaId}")
    public ResponseEntity<List<Transacao>> consultarTransacoesPorConta(@PathVariable Long contaId) {
        return ResponseEntity.ok(transacaoService.findByContaId(contaId));
    }
}
