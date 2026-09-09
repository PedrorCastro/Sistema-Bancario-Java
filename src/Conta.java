import java.util.ArrayList;
import java.util.List;

public abstract class Conta {

    private Integer numero;
    private Cliente titular;
    private double saldo = 0;
    private List<Transacao> historico = new ArrayList<>();

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

    public double getSaldo() {
        return saldo;
    }

    protected void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public List<Transacao> getHistorico() {
        return historico;
    }

    private void setHistorico(List<Transacao> historico) {
        this.historico = historico;
    }



    public abstract void sacar(double valor) throws SaldoInsuficienteException, ValorInvalidoException;

    public void depositar(double valor) throws ValorInvalidoException {
        validarValor(valor);
        this.saldo += valor;
        Transacao transacao = new Transacao(valor, TipoTransacao.DEPOSITO);
        historico.add(transacao);

        System.out.println("Saldo atual de: " +  this.getSaldo());
    }

    protected void validarValor(double valor) throws ValorInvalidoException{
        if(valor > 0){
            System.out.println("Transação valida");
        }else {
            throw new ValorInvalidoException("Transação invalido");
        }
    }
}

