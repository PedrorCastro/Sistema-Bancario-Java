public class ContaCorrente extends Conta{
    private double limite;

    public ContaCorrente(Integer numero, Cliente titular, double limite) {
        super(numero, titular);
        this.limite = limite;
    }


    public double getLimite() {
        return limite;
    }

    @Override
    public void sacar(double valor) throws SaldoInsuficienteException, ValorInvalidoException {
        validarValor(valor);
        double saldoDisponivel = this.getSaldo() + this.getLimite();

            if (saldoDisponivel >= valor) {
                this.setSaldo(this.getSaldo() - valor);
                Transacao transacao = new Transacao(valor, TipoTransacao.SAQUE);
                getHistorico().add(transacao);
                System.out.println("Saque realizado com sucesso!");
            } else {
                throw new SaldoInsuficienteException("Saque indisponivel! Valor de saque maior do que disponivel");
            }

        System.out.println("Saldo atual de: " +  this.getSaldo());
    }

}