package org.pedro.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

@Entity 
public class Cliente {

    private String nome;
    private String cpf;
    private String email;
    private List<Conta> conta = new ArrayList<>();

    @OneToMany(mappedBy = "cliente")
    private List<Conta> contas;

    //Contrutor
    public Cliente(String nome, String cpf, String email) {
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
    }

    //Getters e Setters


    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    private void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    private void setEmail(String email) {
        this.email = email;
    }

    public List<Conta> getContas() {
        return contas;
    }

    private void setConta(List<Conta> contas) {
        this.conta = conta;
    }

    public void adicionarConta(Conta conta){
        this.conta.add(conta);
    }
    public void depositar(Conta conta){

    }
}
