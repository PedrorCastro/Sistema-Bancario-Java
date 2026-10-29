package org.pedro.service;

import java.util.List;
import java.util.Optional;

import org.pedro.model.Cliente;
import org.pedro.model.Conta;
import org.pedro.repository.ContaRepository;
import org.springframework.stereotype.Service;

/**
 * [ALTERADO]
 * Mudanças:
 * 1. Injeção de dependência via construtor com 'final'.
 * 2. Métodos buscarPorId, buscarPorNumero e listarPorCliente corrigidos de 'void' para retornar
 *    Optional<Conta> e List<Conta> (anteriormente descartavam os retornos).
 * 3. Nomenclatura ajustada para camelCase.
 * 4. Adicionados retornos úteis nos métodos de criação e consulta.
 */
@Service 
public class ContaService {

    private final ContaRepository contaRepository;

    public ContaService(ContaRepository contaRepository) {
        this.contaRepository = contaRepository;
    }

    public Conta criarConta(Conta conta) {
        if (conta == null || conta.getNumero() == null) {
            throw new IllegalArgumentException("Conta e número da conta não podem ser nulos");
        }
        if (contaRepository.existsByNumero(conta.getNumero())) {
            throw new IllegalArgumentException("Conta já cadastrada com o número: " + conta.getNumero());
        }
        return contaRepository.save(conta);
    }

    public Optional<Conta> buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return contaRepository.findById(id);
    }

    public Optional<Conta> buscarPorNumero(Integer numero) {
        if (numero == null) {
            throw new IllegalArgumentException("Número não pode ser nulo");
        }
        return contaRepository.findByNumero(numero);
    }

    public List<Conta> listarPorCliente(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente não pode ser nulo");
        }
        return contaRepository.findByTitular(cliente);
    }

    public List<Conta> listarTodos() {
        return contaRepository.findAll();
    }
}
