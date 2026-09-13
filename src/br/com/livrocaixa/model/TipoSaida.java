package br.com.livrocaixa.model;

public class TipoSaida {
    private int idTipoSaida;
    private String descricao;

    public TipoSaida() {
    }

    public TipoSaida(int idTipoSaida, String descricao) {
        this.idTipoSaida = idTipoSaida;
        this.descricao = descricao;
    }

    public int getIdTipoSaida() {
        return idTipoSaida;
    }

    public void setIdTipoSaida(int idTipoSaida) {
        this.idTipoSaida = idTipoSaida;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

}