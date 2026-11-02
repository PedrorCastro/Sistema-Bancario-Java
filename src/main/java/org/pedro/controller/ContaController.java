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
import org.pedro.dto.request.ContaRequestDTO;
import org.pedro.dto.response.ContaResponseDTO;
import org.pedro.model.Conta;

import java.util.List;

@RestController
@RequestMapping("/contas")
public class ContaController {

    private final ContaService contaService;

    public ContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    @PostMapping("/api/contas")
    public ResponseEntity<ContaResponseDTO> criarConta(@RequestBody @Valid ContaRequestDTO dto) {
        Conta createdConta = contaService.criarConta(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponseDTO(createdConta));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContaResponseDTO> buscarPorId(@PathVariable Long id){
        return contaService.buscarPorId(id)
                .map(conta -> ResponseEntity.ok(mapToResponseDTO(conta)))
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
                .map(this::mapToResponseDTO)
                .toList();
        return ResponseEntity.ok(contas);
    }

    private ContaResponseDTO mapToResponseDTO(Conta conta) {
        return new ContaResponseDTO(
                conta.getId(),
                conta.getClass().getSimpleName(),
                conta.getSaldo(),
                conta.getTitular().getId()
        );
    }
}
