package org.pedro.controller;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.pedro.service.ClienteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.pedro.model.Cliente;
import org.pedro.dto.request.ClienteRequestDTO;
import org.pedro.dto.response.ClienteResponseDTO;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final PasswordEncoder passwordEncoder;

    public ClienteController(ClienteService clienteService, PasswordEncoder passwordEncoder) {
        this.clienteService = clienteService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> createdEntity(@Valid @RequestBody ClienteRequestDTO clienteRequestDTO) {
        Cliente cliente = new Cliente();
        cliente.setNome(clienteRequestDTO.nome());
        cliente.setEmail(clienteRequestDTO.email());
        cliente.setCpf(clienteRequestDTO.cpf());
        cliente.setSenha(passwordEncoder.encode(clienteRequestDTO.senha()));

        Cliente clienteSalvo = clienteService.cadastrarCliente(cliente);
        ClienteResponseDTO clienteResponseDTO = new ClienteResponseDTO(
            clienteSalvo.getNome(),
            clienteSalvo.getCpf(),
            clienteSalvo.getEmail()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(clienteResponseDTO);


    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> getClienteById(@PathVariable Long id) {
        return clienteService.buscarPorId(id)
            .map(cliente -> ResponseEntity.ok(
                    new ClienteResponseDTO(
                            cliente.getNome(),
                            cliente.getCpf(),
                            cliente.getEmail()
                    )
            ))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCliente(@PathVariable Long id) {
        if (clienteService.deletarCliente(id)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
