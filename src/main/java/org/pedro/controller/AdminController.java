 package org.pedro.controller;

import java.util.List;
import java.util.ArrayList;

import org.pedro.dto.response.ClienteResponseDTO;
import org.pedro.model.Cliente;
import org.pedro.service.ClienteService;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ClienteService clienteService;

    public AdminController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping("/clientes")
    public ResponseEntity<List<ClienteResponseDTO>> listarTodos() {

        List<Cliente> clientes = clienteService.listarTodos();

        List<ClienteResponseDTO> clientesDTO = new ArrayList<>();

        for (Cliente cliente : clientes) {
            clientesDTO.add(
                new ClienteResponseDTO(
                    cliente.getNome(),
                    cliente.getCpf(),
                    cliente.getEmail()
                )
            );
        }

        return ResponseEntity.ok(clientesDTO);
    }
}

