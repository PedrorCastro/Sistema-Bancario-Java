package org.pedro.service;

import java.util.List;
import java.util.Optional;

import org.pedro.model.Cliente;
import org.pedro.repository.ClienteRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.persistence.Enumerated;
import org.pedro.model.Role;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

@Service 
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public ClienteService(ClienteRepository clienteRepository, PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Cliente cadastrarCliente(Cliente cliente) {

        if (cliente == null || cliente.getCpf() == null
                || cliente.getCpf().trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Dados do cliente ou CPF não podem ser nulos/vazios"
            );
        }

        if (clienteRepository.existsByCpf(cliente.getCpf())) {
            throw new IllegalArgumentException(
                "Cliente já cadastrado com o CPF: " + cliente.getCpf()
            );
        }

        if (cliente.getEmail() == null || cliente.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email não pode ser vazio");
        }

        String emailNormalizado = cliente.getEmail().trim().toLowerCase();

        if (clienteRepository.existsByEmail(emailNormalizado)) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        if (cliente.getSenha() == null || cliente.getSenha().isBlank()) {
            throw new IllegalArgumentException("Senha não pode ser vazia");
        }

        cliente.setEmail(emailNormalizado);
        cliente.setSenha(passwordEncoder.encode(cliente.getSenha()));

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

    public Optional<Cliente> buscarPorEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email não pode ser nulo ou vazio");
        }
        return clienteRepository.findByEmail(email.trim().toLowerCase());
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public boolean deletarCliente(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        if (clienteRepository.existsById(id)) {
            clienteRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    public Optional<Cliente> buscarClienteLogado() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        String email = authentication.getName();

        return clienteRepository.findByEmail(email);
    }
}
