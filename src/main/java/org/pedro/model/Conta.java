package org.pedro.model;

import java.util.ArrayList;
import java.util.List;
import org.pedro.exception.SaldoInsuficienteException;
import org.pedro.exception.ValorInvalidoException;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity 
public abstract class Conta {

    private Integer numero;
    private Cliente titular;
    private Long saldo = Long.valueOf(0);
    private List<Transacao> historico = new ArrayList<>();

    @Id 
    @GeneratedValue 
    private Long id;

    @ManyToOne 
    private Cliente cliente;

    //Construtor
    public Conta(Integer numero, Cliente titular) {
        this.numero = numero;
        this.titular = titular;
    }

    //Getter e Setters


    public Integer getNumero() {
        return numero;
    }

    private void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Cliente getTitular() {
        return titular;
    }

    private void setTitular(Cliente titular) {
        this.titular = titular;
    }

    public Long getSaldo() {
        return saldo;
    }

    protected void setSaldo(Long saldo) {
        this.saldo = saldo;
    }

    public List<Transacao> getHistorico() {
        return historico;
    }

    private void setHistorico(List<Transacao> historico) {
        this.historico = historico;
    }



    public abstract void sacar(Long valor) throws SaldoInsuficienteException, ValorInvalidoException;

    public void depositar(Long valor) throws ValorInvalidoException {
        validarValor(valor);
        this.saldo += valor;
        Transacao transacao = new Transacao(valor, TipoTransacao.DEPOSITO);
        historico.add(transacao);

        System.out.println("Saldo atual de: " +  this.getSaldo());
    }

    protected void validarValor(Long valor) throws ValorInvalidoException{
        if(valor > 0){
            System.out.println("Transação valida");
        }else {
            throw new ValorInvalidoException("Transação invalido");
        }
    }
}

