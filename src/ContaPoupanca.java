public class ContaPoupanca extends Conta{
    public ContaPoupanca(Integer numero, Cliente titular) {
        super(numero, titular);
    }

    @Override
    public void sacar(double valor) throws SaldoInsuficienteException {

        Transacao transacao = new Transacao(valor, TipoTransacao.SAQUE);
        if (valor > 0){
            if (valor <= this.getSaldo()){
                this.setSaldo(this.getSaldo() - valor);
                getHistorico().add(transacao);
                System.out.println("Saque feito com sucesso");
                System.out.println("Saldo atual de: " +  this.getSaldo());

            } else {
                throw new SaldoInsuficienteException("Saldo Insuficiente");
            }
        } else {
            System.out.println("Valor negativo!");
        }

    }
}
