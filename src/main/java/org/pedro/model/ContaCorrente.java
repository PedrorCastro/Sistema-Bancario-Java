package org.pedro.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import java.math.BigDecimal;

import org.pedro.exception.SaldoInsuficienteException;
import org.pedro.exception.ValorInvalidoException;


@Entity
@Table(name = "tb_contas_correntes")
public class ContaCorrente extends Conta {

    @Column(precision = 19, scale = 2)
    private BigDecimal limite = new BigDecimal("5000.00");

    public ContaCorrente() {
        super();
    }


    public ContaCorrente(Integer numero, Cliente titular) {
        super(numero, titular);
        this.limite = new BigDecimal("5000.00");
    }

    public BigDecimal getLimite() {
        return limite;
    }

    public void setLimite(BigDecimal limite) {
        this.limite = limite != null ? limite : BigDecimal.ZERO;
    }

    @Override
    protected void sacar(BigDecimal valor, TipoTransacao tipo, Conta contraparte)
            throws SaldoInsuficienteException, ValorInvalidoException {
        validarValor(valor);
        BigDecimal saldoDisponivel = this.getSaldo().add(this.limite);

        if (saldoDisponivel.compareTo(valor) >= 0) {
            this.setSaldo(this.getSaldo().subtract(valor));
            Transacao transacao = new Transacao(valor, tipo, this, contraparte);
            getHistorico().add(transacao);
        } else {
            throw new SaldoInsuficienteException("Saldo insuficiente: valor do saque excede o saldo disponível com limite");
        }
    }

    @Override
    public String toString() {
        return "ContaCorrente{" +
                "numero=" + getNumero() +
                ", titular=" + (getTitular() != null ? getTitular().getNome() : "Sem titular") +
                ", saldo=" + getSaldo() +
                ", limite=" + limite +
                '}';
    }


    
}
