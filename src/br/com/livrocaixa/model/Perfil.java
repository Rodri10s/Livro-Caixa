package br.com.livrocaixa.model;

public class Perfil {
    private int idPerfil;
    private String descricao;

    public Perfil() {
    }

    public Perfil(int idPerfil, String descricao) {
        this.idPerfil = idPerfil;
        this.descricao = descricao;
    }

    public int getIdPerfil() {
        return idPerfil;
    }

    public void setIdPerfil(int idPerfil) {
        this.idPerfil = idPerfil;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}