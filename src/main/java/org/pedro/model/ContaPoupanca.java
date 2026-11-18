package org.pedro.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.pedro.exception.SaldoInsuficienteException;
import org.pedro.exception.ValorInvalidoException;
import java.math.BigDecimal;


@Entity
@Table(name = "tb_contas_poupancas")
public class ContaPoupanca extends Conta {

    public ContaPoupanca() {
        super();
    }

    public ContaPoupanca(Integer numero, Cliente titular) {
        super(numero, titular);
    }

    @Override
    protected void sacar(BigDecimal valor, TipoTransacao tipo, Conta contraparte)
            throws SaldoInsuficienteException, ValorInvalidoException {
        validarValor(valor);
        if (valor.compareTo(this.getSaldo()) <= 0) {
            this.setSaldo(this.getSaldo().subtract(valor));
            Transacao transacao = new Transacao(valor, tipo, this, contraparte);
            getHistorico().add(transacao);
        } else {
            throw new SaldoInsuficienteException("Saldo insuficiente: valor do saque excede o saldo da poupança");
        }
    }

    @Override
    public String toString() {
        return "ContaPoupanca{" +
                "numero=" + getNumero() +
                ", titular=" + (getTitular() != null ? getTitular().getNome() : "Sem titular") +
                ", saldo=" + getSaldo() +
                '}';
    }
}
