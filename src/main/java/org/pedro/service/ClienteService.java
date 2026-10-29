package org.pedro.service;

import java.util.List;
import java.util.Optional;

import org.pedro.model.Cliente;
import org.pedro.repository.ContaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.pedro.repository.*;

@Service 
public class ClienteService {

    @Autowired 
    private ContaRepository contaRepository;
    @Autowired 
    private ClienteRepository clienteRepository;

    public void cadastrarCliente(Cliente cliente){
        if(clienteRepository.existsByCpf(cliente.getCpf())){
            throw new IllegalArgumentException("Cliente já cadastrado");
        } else{
            clienteRepository.save(cliente);
        }

    }

    public Cliente buscarporId(Long Id){
        if(Id == null){
            throw new IllegalArgumentException("Id não pode ser nulo");
        } else{
            return clienteRepository.findById(Id).orElse(null);
        }

    }

    public void buscarporCPF(String cpf){
        if(cpf == null || cpf.isEmpty()){
            throw new IllegalArgumentException("CPF não pode ser nulo ou vazio");
        } else{
            clienteRepository.findByCpf(cpf);
        }

    }

    public List<Cliente> listarTodos(){
        return clienteRepository.findAll();
    }

}
