package org.pedro.controller;

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
import org.pedro.dto.request.TipoConta;
import org.pedro.dto.response.ContaResponseDTO;
import org.pedro.model.Cliente;
import org.pedro.model.Conta;
import org.pedro.model.ContaCorrente;
import org.pedro.model.ContaPoupanca;



@RestController
@RequestMapping("/contas")
public class ContaController {

    private final ContaService contaService;

    public ContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    @PostMapping
public ResponseEntity<ContaResponseDTO> criarConta(
        @RequestBody ContaRequestDTO contaRequestDTO) {

    Conta createdConta = contaService.criarConta(contaRequestDTO);

    ContaResponseDTO responseDTO = new ContaResponseDTO(
        createdConta.getId(),
        createdConta.getClass().getSimpleName(),
        createdConta.getSaldo(),
        createdConta.getTitular().getId()
    );

    return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
}
    
    @GetMapping("/{id}")
    public ResponseEntity<Conta> buscarPorId(@PathVariable Long id){
        Conta conta =  contaService.buscarPorId(id).orElse(null);
        if (conta != null) {
            return new ResponseEntity<>(conta, HttpStatus.OK);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarConta(@PathVariable Long id){
        if (contaService.deletarConta(id)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


}
