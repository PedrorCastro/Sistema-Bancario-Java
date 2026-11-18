
package org.pedro.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.pedro.dto.request.ContaRequestDTO;
import org.pedro.dto.request.TipoConta;
import org.pedro.model.Cliente;
import org.pedro.model.Conta;
import org.pedro.model.ContaCorrente;
import org.pedro.model.ContaPoupanca;
import org.pedro.model.Role;
import org.pedro.exception.ContaNaoEncontradaException;
import org.pedro.repository.ContaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private Cliente buscarClienteAutenticado() {
        return clienteService.buscarClienteLogado()
                .orElseThrow(() ->
                        new IllegalStateException("Usuário autenticado não encontrado"));
    }

    private boolean isAdmin(Cliente cliente) {
        return cliente.getRole() == Role.ADMIN;
    }

    @Transactional
    public Conta criarConta(ContaRequestDTO dto) {

        if (dto == null || dto.numero() == null || dto.tipoConta() == null) {
            throw new IllegalArgumentException("Dados obrigatórios da conta não informados");
        }

        if (contaRepository.existsByNumero(dto.numero())) {
            throw new IllegalArgumentException(
                    "Conta já cadastrada com o número: " + dto.numero());
        }

        Cliente autenticado = buscarClienteAutenticado();

        Cliente titular;

        if (isAdmin(autenticado)) {
            titular = clienteService.buscarPorId(dto.clienteId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Cliente não encontrado"));
        } else {
            // CLIENTE só pode criar conta para si mesmo.
            titular = autenticado;
        }

        Conta conta = (dto.tipoConta() == TipoConta.CORRENTE)
                ? new ContaCorrente()
                : new ContaPoupanca();

        conta.setNumero(dto.numero());
        conta.setTitular(titular);

        if (dto.saldo() != null && dto.saldo().compareTo(BigDecimal.ZERO) > 0) {
            conta.depositar(dto.saldo());
        }

        return contaRepository.save(conta);
    }

    @Transactional
    public boolean deletarConta(Long id) {

        Optional<Conta> conta = buscarPorIdParaAtualizacao(id);
        if (conta.isEmpty()) {
            return false;
        }

        if (!conta.get().isAtiva()) {
            return false;
        }

        conta.get().encerrar();
        contaRepository.save(conta.get());
        return true;
    }

    @Transactional
    public Optional<Conta> buscarPorIdParaAtualizacao(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }

        Cliente autenticado = buscarClienteAutenticado();
        Optional<Long> lockedId = isAdmin(autenticado)
                ? contaRepository.lockRowById(id)
                : contaRepository.lockRowByIdAndTitularEmail(id, autenticado.getEmail());

        return lockedId.flatMap(contaRepository::findById);
    }

    public void verificarAcesso(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }

        Cliente autenticado = buscarClienteAutenticado();
        boolean acessivel = isAdmin(autenticado)
                ? contaRepository.existsById(id)
                : contaRepository.existsByIdAndTitular_Email(id, autenticado.getEmail());
        if (!acessivel) {
            throw new ContaNaoEncontradaException();
        }
    }

    public Optional<Conta> buscarPorId(Long id) {

        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }

        Cliente autenticado = buscarClienteAutenticado();

        if (isAdmin(autenticado)) {
            return contaRepository.findById(id);
        }

        return contaRepository.findByIdAndTitular_Email(
                id, autenticado.getEmail());
    }

    public Optional<Conta> buscarPorNumero(Integer numero) {

        if (numero == null) {
            throw new IllegalArgumentException("Número não pode ser nulo");
        }

        Cliente autenticado = buscarClienteAutenticado();

        if (isAdmin(autenticado)) {
            return contaRepository.findByNumero(numero);
        }

        return contaRepository.findByNumeroAndTitular_Email(
                numero, autenticado.getEmail());
    }

    public List<Conta> listarPorCliente(Cliente cliente) {

        Cliente autenticado = buscarClienteAutenticado();

        if (isAdmin(autenticado)) {
            if (cliente == null) {
                throw new IllegalArgumentException("Cliente não pode ser nulo");
            }

            return contaRepository.findByTitular(cliente);
        }

        return contaRepository.findByTitular_Email(autenticado.getEmail());
    }

    public List<Conta> listarMinhasContas() {
        Cliente autenticado = buscarClienteAutenticado();

        return contaRepository.findByTitular_Email(autenticado.getEmail());
    }

    public List<Conta> listarTodos() {

        Cliente autenticado = buscarClienteAutenticado();

        if (!isAdmin(autenticado)) {
            throw new IllegalStateException(
                    "Apenas administradores podem listar todas as contas");
        }

        return contaRepository.findAll();
    }
}
