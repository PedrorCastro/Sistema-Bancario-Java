package org.pedro.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.pedro.exception.SaldoInsuficienteException;
import org.pedro.exception.ValorInvalidoException;


@Entity
@Table(name = "tb_contas_poupancas")
public class ContaPoupanca extends Conta {

    protected ContaPoupanca() {
        super();
    }

    public ContaPoupanca(Integer numero, Cliente titular) {
        super(numero, titular);
    }

    @Override
    public void sacar(Long valor) throws SaldoInsuficienteException, ValorInvalidoException {
        validarValor(valor);
        if (valor <= this.getSaldo()) {
            this.setSaldo(this.getSaldo() - valor);
            Transacao transacao = new Transacao(valor, TipoTransacao.SAQUE, this);
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
