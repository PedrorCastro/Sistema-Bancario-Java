package org.pedro.controller;

import jakarta.validation.Valid;
import org.pedro.dto.request.DepositoRequestDTO;
import org.pedro.dto.request.SaqueRequestDTO;
import org.pedro.dto.request.TransferenciaRequestDTO;
import org.pedro.dto.response.TransacaoResponseDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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
    public void depositar(@RequestBody @Valid DepositoRequestDTO request) {
        transacaoService.depositar(request.contaId(), request.valor());
    }

    @PostMapping("/transacoes/sacar")
    public void sacar(@RequestBody @Valid SaqueRequestDTO request) {
        transacaoService.sacar(request.contaId(), request.valor());
    }

    @PostMapping("/transacoes/transferir")
    public void transferir(@RequestBody @Valid TransferenciaRequestDTO request) {
        transacaoService.transferir(
                request.contaOrigemId(),
                request.contaDestinoId(),
                request.valor());
    }

    @GetMapping("/transacoes/extrato")
    public ResponseEntity<List<TransacaoResponseDTO>> consultarTransacoes(
            @RequestParam Long contaId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dataInicio,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dataFim) {

        return ResponseEntity.ok(transacaoService.gerarExtrato(contaId, dataInicio, dataFim)
                .stream()
                .map(this::mapToResponseDTO)
                .toList());
    }

    @GetMapping("/transacoes/conta/{contaId}")
    public ResponseEntity<List<TransacaoResponseDTO>> consultarTransacoesPorConta(@PathVariable Long contaId) {
        return ResponseEntity.ok(transacaoService.findByContaId(contaId)
                .stream()
                .map(this::mapToResponseDTO)
                .toList());
    }

    private TransacaoResponseDTO mapToResponseDTO(Transacao transacao) {
        return new TransacaoResponseDTO(
                transacao.getId(),
                transacao.getTipo().name(),
                transacao.getValor(),
                transacao.getData(),
                transacao.getConta().getId(),
                transacao.getContaContraparte() != null
                        ? transacao.getContaContraparte().getId()
                        : null);
    }
}
