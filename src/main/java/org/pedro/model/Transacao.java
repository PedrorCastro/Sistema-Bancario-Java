package org.pedro.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "tb_transacoes")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 19, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    private TipoTransacao tipo;

    private LocalDateTime data = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "conta_id")
    private Conta conta;

    @ManyToOne
    @JoinColumn(name = "conta_contraparte_id")
    private Conta contaContraparte;

    protected Transacao() {
    }

    public Transacao(BigDecimal valor, TipoTransacao tipo) {
        this.valor = valor;
        this.tipo = tipo;
        this.data = LocalDateTime.now();
    }

    public Transacao(BigDecimal valor, TipoTransacao tipo, Conta conta) {
        this.valor = valor;
        this.tipo = tipo;
        this.conta = conta;
        this.data = LocalDateTime.now();
    }

    public Transacao(BigDecimal valor, TipoTransacao tipo, Conta conta, Conta contaContraparte) {
        this(valor, tipo, conta);
        this.contaContraparte = contaContraparte;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public TipoTransacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoTransacao tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public Conta getConta() {
        return conta;
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }

    public Conta getContaContraparte() {
        return contaContraparte;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transacao that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Transacao.class.hashCode();
    }

    @Override
    public String toString() {
        return "Transacao{" +
                "id=" + id +
                ", valor=" + valor +
                ", tipo=" + tipo +
                ", data=" + data +
                '}';
    }
}
