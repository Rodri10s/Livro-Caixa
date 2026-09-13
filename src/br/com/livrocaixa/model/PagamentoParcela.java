package br.com.livrocaixa.model;

import java.time.LocalDate;

public class PagamentoParcela {
    private int idParcelaPagamento;
    private Parcela parcela;
    private FormaPagamento formaPagamento;
    private double valor;
    private LocalDate data;

    public PagamentoParcela() {
    }

    public PagamentoParcela(int idParcelaPagamento, Parcela parcela, FormaPagamento formaPagamento, double valor,
            LocalDate data) {
        this.idParcelaPagamento = idParcelaPagamento;
        this.parcela = parcela;
        this.formaPagamento = formaPagamento;
        this.valor = valor;
        this.data = data;
    }

    public int getIdParcelaPagamento() {
        return idParcelaPagamento;
    }

    public void setIdParcelaPagamento(int idParcelaPagamento) {
        this.idParcelaPagamento = idParcelaPagamento;
    }

    public Parcela getParcela() {
        return parcela;
    }

    public void setParcela(Parcela parcela) {
        this.parcela = parcela;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

}