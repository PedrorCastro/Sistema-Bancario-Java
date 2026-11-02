package org.pedro.service;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import org.springframework.transaction.annotation.Transactional;

import org.pedro.model.Cliente;
import org.pedro.model.Conta;
import org.pedro.model.ContaCorrente;
import org.pedro.model.ContaPoupanca;
import org.pedro.repository.ContaRepository;
import org.springframework.stereotype.Service;
import org.pedro.dto.request.ContaRequestDTO;
import org.pedro.dto.request.TipoConta;

@Service
public class ContaService {

    private final ContaRepository contaRepository;
    private final ClienteService clienteService;

    public ContaService(
        ContaRepository contaRepository,
        ClienteService clienteService) {

        this.contaRepository = contaRepository;
        this.clienteService = clienteService;
    }

    @Transactional
    public Conta criarConta(ContaRequestDTO dto) {
        if (dto == null || dto.numero() == null) {
            throw new IllegalArgumentException("Dados da conta e número da conta não podem ser nulos");
        }

        if (contaRepository.existsByNumero(dto.numero())) {
            throw new IllegalArgumentException("Conta já cadastrada com o número: " + dto.numero());
        }

        Cliente cliente = clienteService.buscarPorId(dto.clienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com ID: " + dto.clienteId()));

        Conta conta = (dto.tipoConta() == TipoConta.CORRENTE)
                      ? new ContaCorrente()
                      : new ContaPoupanca();

        conta.setNumero(dto.numero());
        conta.setTitular(cliente);

        if (dto.saldo() != null && dto.saldo().compareTo(BigDecimal.ZERO) > 0) {
            try {
                conta.depositar(dto.saldo());
            } catch (Exception e) {
                throw new IllegalArgumentException("Erro ao realizar depósito inicial: " + e.getMessage());
            }
        }

        return contaRepository.save(conta);
    }

    @Transactional
    public boolean deletarConta(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        if (contaRepository.existsById(id)) {
            contaRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
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
