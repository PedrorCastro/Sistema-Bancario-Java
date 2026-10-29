package org.pedro.service;

import java.util.List;
import java.util.Optional;

import org.pedro.model.Cliente;
import org.pedro.repository.ClienteRepository;
import org.springframework.stereotype.Service;


@Service 
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public Cliente cadastrarCliente(Cliente cliente) {
        if (cliente == null || cliente.getCpf() == null || cliente.getCpf().trim().isEmpty()) {
            throw new IllegalArgumentException("Dados do cliente ou CPF não podem ser nulos/vazios");
        }
        if (clienteRepository.existsByCpf(cliente.getCpf())) {
            throw new IllegalArgumentException("Cliente já cadastrado com o CPF: " + cliente.getCpf());
        }
        return clienteRepository.save(cliente);
    }

    public Optional<Cliente> buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return clienteRepository.findById(id);
    }

    public Optional<Cliente> buscarPorCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new IllegalArgumentException("CPF não pode ser nulo ou vazio");
        }
        return clienteRepository.findByCpf(cpf);
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }
}
