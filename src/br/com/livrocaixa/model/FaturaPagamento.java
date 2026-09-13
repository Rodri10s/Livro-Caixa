package br.com.livrocaixa.model;

import java.time.LocalDate;

public class FaturaPagamento {
    private int idFaturaPagamento;
    private Fatura fatura;
    private FormaPagamento formaPagamento;
    private double valor;
    private LocalDate data;

    public FaturaPagamento() {
    }

    public FaturaPagamento(int idFaturaPagamento, Fatura fatura, FormaPagamento formaPagamento, double valor,
            LocalDate data) {
        this.idFaturaPagamento = idFaturaPagamento;
        this.fatura = fatura;
        this.formaPagamento = formaPagamento;
        this.valor = valor;
        this.data = data;
    }

    public int getIdFaturaPagamento() {
        return idFaturaPagamento;
    }

    public void setIdFaturaPagamento(int idFaturaPagamento) {
        this.idFaturaPagamento = idFaturaPagamento;
    }

    public Fatura getFatura() {
        return fatura;
    }

    public void setFatura(Fatura fatura) {
        this.fatura = fatura;
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