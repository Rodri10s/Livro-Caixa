package br.com.livrocaixa.model;

import java.time.LocalDate;

public class Parcela {
    private int idParcela;
    private double valor;
    private LocalDate dataVencimento;
    private int situacaoPago; // Utilizado int para mapear o TINYINT do banco (ex: 0 para falso, 1 para
                              // verdadeiro)
    private Fatura fatura;

    public Parcela() {
    }

    public Parcela(int idParcela, double valor, LocalDate dataVencimento, int situacaoPago, Fatura fatura) {
        this.idParcela = idParcela;
        this.valor = valor;
        this.dataVencimento = dataVencimento;
        this.situacaoPago = situacaoPago;
        this.fatura = fatura;
    }

    public int getIdParcela() {
        return idParcela;
    }

    public void setIdParcela(int idParcela) {
        this.idParcela = idParcela;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(LocalDate dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public int getSituacaoPago() {
        return situacaoPago;
    }

    public void setSituacaoPago(int situacaoPago) {
        this.situacaoPago = situacaoPago;
    }

    public Fatura getFatura() {
        return fatura;
    }

    public void setFatura(Fatura fatura) {
        this.fatura = fatura;
    }

}