package br.com.livrocaixa.model;

public class Saida {
    private int idSaida;
    private String observacao;
    private double valor;
    private Funcionario funcionario;
    private TipoSaida tipoSaida;

    public Saida() {
    }

    public Saida(int idSaida, String observacao, double valor, Funcionario funcionario, TipoSaida tipoSaida) {
        this.idSaida = idSaida;
        this.observacao = observacao;
        this.valor = valor;
        this.funcionario = funcionario;
        this.tipoSaida = tipoSaida;
    }

    public int getIdSaida() {
        return idSaida;
    }

    public void setIdSaida(int idSaida) {
        this.idSaida = idSaida;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
    }

    public TipoSaida getTipoSaida() {
        return tipoSaida;
    }

    public void setTipoSaida(TipoSaida tipoSaida) {
        this.tipoSaida = tipoSaida;
    }

}