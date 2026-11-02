package org.pedro.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.pedro.service.ContaService;
import org.pedro.service.ClienteService;
import org.pedro.dto.request.ContaRequestDTO;
import org.pedro.dto.request.TipoConta;
import org.pedro.dto.response.ContaResponseDTO;
import org.pedro.model.Cliente;
import org.pedro.model.Conta;
import org.pedro.model.ContaCorrente;
import org.pedro.model.ContaPoupanca;

import java.util.List;

@RestController
@RequestMapping("/contas")
public class ContaController {

    private final ContaService contaService;
    private final ClienteService clienteService;

    public ContaController(ContaService contaService, ClienteService clienteService) {
        this.contaService = contaService;
        this.clienteService = clienteService;
    }

    @PostMapping("api/contas")
    public ResponseEntity<ContaResponseDTO> criarConta(@RequestBody @Valid ContaRequestDTO dto) {

        Cliente cliente = clienteService.buscarPorId(dto.clienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com ID: " + dto.clienteId()));

        Conta conta;
        if (dto.tipoConta() == TipoConta.CORRENTE) {
            ContaCorrente cc = new ContaCorrente();
            conta = cc;
        } else {
            conta = new ContaPoupanca();
        }

        conta.setNumero(dto.numero());
        conta.setTitular(cliente);

        if (dto.saldo() != null && dto.saldo() > 0) {
            conta.depositar(dto.saldo());
        }


        Conta createdConta = contaService.criarConta(conta);

        ContaResponseDTO responseDTO = new ContaResponseDTO(
                createdConta.getId(),
                createdConta.getClass().getSimpleName(),
                (long) createdConta.getSaldo(),
                createdConta.getTitular().getId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContaResponseDTO> buscarPorId(@PathVariable Long id){
        return contaService.buscarPorId(id)
                .map(conta -> ResponseEntity.ok(new ContaResponseDTO(
                        conta.getId(),
                        conta.getClass().getSimpleName(),
                        (long) conta.getSaldo(),
                        conta.getTitular().getId()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarConta(@PathVariable Long id){
        if (contaService.deletarConta(id)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ContaResponseDTO>> listarContas() {
        List<ContaResponseDTO> contas = contaService.listarTodos().stream()
                .map(conta -> new ContaResponseDTO(
                        conta.getId(),
                        conta.getClass().getSimpleName(),
                        (long) conta.getSaldo(),
                        conta.getTitular().getId()
                ))
                .toList();
        return ResponseEntity.ok(contas);
    }
}