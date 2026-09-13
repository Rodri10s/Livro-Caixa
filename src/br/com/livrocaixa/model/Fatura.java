package br.com.livrocaixa.model;

import java.time.LocalDate;

public class Fatura {
    private int idFatura;
    private double valorTotal;
    private int numParcelas;
    private double valorParcela;
    private LocalDate dataInicial;

    public Fatura() {
    }

    public Fatura(int idFatura, double valorTotal, int numParcelas, double valorParcela, LocalDate dataInicial) {
        this.idFatura = idFatura;
        this.valorTotal = valorTotal;
        this.numParcelas = numParcelas;
        this.valorParcela = valorParcela;
        this.dataInicial = dataInicial;
    }

    public int getIdFatura() {
        return idFatura;
    }

    public void setIdFatura(int idFatura) {
        this.idFatura = idFatura;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public int getNumParcelas() {
        return numParcelas;
    }

    public void setNumParcelas(int numParcelas) {
        this.numParcelas = numParcelas;
    }

    public double getValorParcela() {
        return valorParcela;
    }

    public void setValorParcela(double valorParcela) {
        this.valorParcela = valorParcela;
    }

    public LocalDate getDataInicial() {
        return dataInicial;
    }

    public void setDataInicial(LocalDate dataInicial) {
        this.dataInicial = dataInicial;
    }

}