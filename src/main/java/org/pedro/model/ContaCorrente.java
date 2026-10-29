package org.pedro.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.pedro.exception.SaldoInsuficienteException;
import org.pedro.exception.ValorInvalidoException;


@Entity
@Table(name = "tb_contas_correntes")
public class ContaCorrente extends Conta {

    private Long limite = 0L;

    protected ContaCorrente() {
        super();
    }


    public ContaCorrente(Integer numero, Cliente titular, double limite) {
        super(numero, titular);
        this.limite = (long) limite;
    }

    public Long getLimite() {
        return limite;
    }

    public void setLimite(Long limite) {
        this.limite = limite != null ? limite : 0L;
    }

    @Override
    public void sacar(Long valor) throws SaldoInsuficienteException, ValorInvalidoException {
        validarValor(valor);
        long saldoDisponivel = this.getSaldo() + this.limite;

        if (saldoDisponivel >= valor) {
            this.setSaldo(this.getSaldo() - valor);
            Transacao transacao = new Transacao(valor, TipoTransacao.SAQUE, this);
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
