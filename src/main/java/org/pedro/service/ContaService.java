package org.pedro.service;

import org.pedro.repository.ContaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import org.pedro.model.Conta;
import org.pedro.model.Cliente;

@Service 
public class ContaService {

    @Autowired 
    private ContaRepository contaRepository;

    public void criarConta(Conta conta){
        if(contaRepository.existsByNumero(conta.getNumero())){
            throw new IllegalArgumentException("Conta já cadastrada");
        } else{
            contaRepository.save(conta);
        }
    }

    public void buscarporId(Long Id){
        if(Id == null){
            throw new IllegalArgumentException("Id não pode ser nulo");
        } else{
            contaRepository.findById(Id);
        }

    }

    public void buscarporNumero(Integer numero){
        if (numero == null) {
            throw new IllegalArgumentException("Número não pode ser nulo");
        } else {
            contaRepository.findByNumero(numero);
        }
    }


    public void listarPorCliente (Cliente cliente){
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente não pode ser nulo");
        } else {
            contaRepository.findByTitular(cliente);
        }
    }

    public List<Conta> listarTodos(){
        return contaRepository.findAll();
    }
}
