import java.time.LocalDateTime;

public class Transacao {
    private double valor;
    private TipoTransacao tipo;
    private LocalDateTime data = LocalDateTime.now();


    public Transacao(double valor, TipoTransacao tipo) {
        this.valor = valor;
        this.tipo = tipo;
    }


    public double getValor() {
        return valor;
    }



    public LocalDateTime getData() {
        return data;
    }


    public TipoTransacao getTipo() {
        return tipo;
    }

    @Override
    public String toString() {
        return "Transacao{" +
                "valor=" + valor +
                ", tipo=" + tipo +
                ", data=" + data +
                '}';
    }
}
