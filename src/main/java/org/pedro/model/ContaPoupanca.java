package org.pedro.model;

import org.pedro.exception.SaldoInsuficienteException;
import org.pedro.exception.ValorInvalidoException;


public class ContaPoupanca extends Conta{
    public ContaPoupanca(Integer numero, Cliente titular) {
        super(numero, titular);
    }

    @Override
    public void sacar(Long valor) throws SaldoInsuficienteException, ValorInvalidoException {


        validarValor(valor);
            if (valor <= this.getSaldo()){
                this.setSaldo(this.getSaldo() - valor);
                System.out.println("Saque feito com sucesso");
                Transacao transacao = new Transacao(valor, TipoTransacao.SAQUE);
                getHistorico().add(transacao);
                System.out.println("Saldo atual de: " +  this.getSaldo());

            } else {
                throw new SaldoInsuficienteException("Saldo Insuficiente");
            }
    }

    @Override
    public String toString() {
        return "ContaPoupanca{" +
                "numero= " + getNumero() +
                ", titular= " + (getTitular() != null ? getTitular().getNome() : "") +
                ", saldo= " + getSaldo() +
                '}';
    }
}
