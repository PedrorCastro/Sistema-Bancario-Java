package org.pedro.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.pedro.exception.SaldoInsuficienteException;
import org.pedro.exception.ValorInvalidoException;

@Entity 
@Table(name = "tb_contas")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Conta {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    private Integer numero;

    @ManyToOne
    @JoinColumn(name = "titular_id")
    private Cliente titular;

    private Long saldo = 0L;

    @OneToMany(mappedBy = "conta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transacao> historico = new ArrayList<>();

    // Construtor sem argumentos exigido pela JPA
    protected Conta() {
    }

    // Construtor
    public Conta(Integer numero, Cliente titular) {
        this.numero = numero;
        this.titular = titular;
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Cliente getTitular() {
        return titular;
    }

    public void setTitular(Cliente titular) {
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

    public void setHistorico(List<Transacao> historico) {
        this.historico = historico != null ? historico : new ArrayList<>();
    }

    public abstract void sacar(Long valor) throws SaldoInsuficienteException, ValorInvalidoException;


    public void depositar(Long valor) throws ValorInvalidoException {
        validarValor(valor);
        this.saldo += valor;
        Transacao transacao = new Transacao(valor, TipoTransacao.DEPOSITO, this);
        historico.add(transacao);
    }


    protected void validarValor(Long valor) throws ValorInvalidoException {
        if (valor == null || valor <= 0) {
            throw new ValorInvalidoException("Transação inválida: o valor deve ser maior que zero");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Conta conta)) return false;
        return Objects.equals(numero, conta.numero);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numero);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +
                ", numero=" + numero +
                ", titular=" + (titular != null ? titular.getNome() : "Sem titular") +
                ", saldo=" + saldo +
                '}';
    }
}
