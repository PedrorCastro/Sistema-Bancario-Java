package org.pedro.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;


@Entity 
public class Transacao {

    @Id 
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    private Long valor;
    private TipoTransacao tipo;
    private LocalDateTime data = LocalDateTime.now();
    


    public Transacao(Long valor, TipoTransacao tipo) {
        this.valor = valor;
        this.tipo = tipo;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getValor() {
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
