package br.com.livrocaixa.model;

public class Funcionario {
    private int idFuncionario;
    private String nome;
    private String senha;
    private Perfil perfil;

    public Funcionario() {
    }

    public Funcionario(int idFuncionario, String nome, String senha, Perfil perfil) {
        this.idFuncionario = idFuncionario;
        this.nome = nome;
        this.senha = senha;
        this.perfil = perfil;
    }

    public int getIdFuncionario() {
        return idFuncionario;
    }

    public void setIdFuncionario(int idFuncionario) {
        this.idFuncionario = idFuncionario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }
}