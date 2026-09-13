package br.com.livrocaixa.model;

public class TipoConsulta {
    private int idTipoConsulta;
    private String descricao;

    public TipoConsulta() {
    }

    public TipoConsulta(int idTipoConsulta, String descricao) {
        this.idTipoConsulta = idTipoConsulta;
        this.descricao = descricao;
    }

    public int getIdTipoConsulta() {
        return idTipoConsulta;
    }

    public void setIdTipoConsulta(int idTipoConsulta) {
        this.idTipoConsulta = idTipoConsulta;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

}