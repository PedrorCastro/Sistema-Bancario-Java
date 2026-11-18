package org.pedro.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.pedro.exception.SaldoInsuficienteException;
import org.pedro.exception.ValorInvalidoException;
import org.pedro.exception.ContaEncerradaException;

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

    @Column(precision = 19, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusConta status = StatusConta.ATIVA;

    @OneToMany(mappedBy = "conta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transacao> historico = new ArrayList<>();

    protected Conta() {
    }

    public Conta(Integer numero, Cliente titular) {
        this.numero = numero;
        this.titular = titular;
    }

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

    public BigDecimal getSaldo() {
        return saldo;
    }

    public StatusConta getStatus() {
        return status;
    }

    public boolean isAtiva() {
        return status == StatusConta.ATIVA;
    }

    public void encerrar() {
        if (!isAtiva()) {
            throw new ContaEncerradaException("A conta já está encerrada");
        }
        if (saldo.compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException("Só é possível encerrar uma conta com saldo zero");
        }
        status = StatusConta.ENCERRADA;
    }

    protected void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public List<Transacao> getHistorico() {
        return historico;
    }

    public void setHistorico(List<Transacao> historico) {
        this.historico = historico != null ? historico : new ArrayList<>();
    }

    public void sacar(BigDecimal valor) throws SaldoInsuficienteException, ValorInvalidoException {
        validarContaAtiva();
        sacar(valor, TipoTransacao.SAQUE, null);
    }

    protected abstract void sacar(BigDecimal valor, TipoTransacao tipo, Conta contraparte)
            throws SaldoInsuficienteException, ValorInvalidoException;

    public void depositar(BigDecimal valor) throws ValorInvalidoException {
        validarContaAtiva();
        depositar(valor, TipoTransacao.DEPOSITO, null);
    }

    private void depositar(BigDecimal valor, TipoTransacao tipo, Conta contraparte)
            throws ValorInvalidoException {
        validarValor(valor);
        this.saldo = this.saldo.add(valor);
        Transacao transacao = new Transacao(valor, tipo, this, contraparte);
        historico.add(transacao);
    }

    public void transferirPara(Conta destino, BigDecimal valor)
            throws SaldoInsuficienteException, ValorInvalidoException {
        validarContaAtiva();
        if (destino == null || destino == this
                || (id != null && id.equals(destino.id))) {
            throw new IllegalArgumentException("A conta de destino deve ser diferente da conta de origem");
        }
        destino.validarContaAtiva();
        validarValor(valor);
        sacar(valor, TipoTransacao.TRANSFERENCIA, destino);
        destino.depositar(valor, TipoTransacao.TRANSFERENCIA, this);
    }

    protected void validarValor(BigDecimal valor) throws ValorInvalidoException {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValorInvalidoException("Transação inválida: o valor deve ser maior que zero");
        }
    }

    private void validarContaAtiva() {
        if (!isAtiva()) {
            throw new ContaEncerradaException("Não é possível movimentar uma conta encerrada");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Conta conta)) return false;
        return id != null && id.equals(conta.id);
    }

    @Override
    public int hashCode() {
        return Conta.class.hashCode();
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
