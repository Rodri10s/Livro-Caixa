package br.com.livrocaixa.model;

import java.util.Objects;

/** Usuário do sistema (tabela Funcionario). */
public class Funcionario {

    private int idFuncionario;
    private String nome;
    private String senha;
    private Perfil perfil;

    public Funcionario() {
    }

    public Funcionario(String nome, String senha, Perfil perfil) {
        setNome(nome);
        setSenha(senha);
        setPerfil(perfil);
    }

    public Funcionario(int idFuncionario, String nome, String senha, Perfil perfil) {
        this(nome, senha, perfil);
        this.idFuncionario = idFuncionario;
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
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do funcionário é obrigatório.");
        }
        if (nome.length() > 45) {
            throw new IllegalArgumentException("O nome deve ter no máximo 45 caracteres.");
        }
        this.nome = nome.trim();
    }

    public String getSenha() {
        return senha;
    }

    /**
     * A coluna senha é VARCHAR(45). Se for armazenar hash (recomendado), use um
     * algoritmo cujo resultado caiba em 45 caracteres ou aumente o tamanho da
     * coluna.
     */
    public void setSenha(String senha) {
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("A senha é obrigatória.");
        }
        if (senha.length() > 45) {
            throw new IllegalArgumentException("A senha deve ter no máximo 45 caracteres.");
        }
        this.senha = senha;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        if (perfil == null) {
            throw new IllegalArgumentException("O perfil do funcionário é obrigatório.");
        }
        this.perfil = perfil;
    }

    /** ValidarLogin() do diagrama de classes (UC001, RF01). */
    public boolean validarLogin(String senhaInformada) {
        return senha != null && senha.equals(senhaInformada);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Funcionario outro))
            return false;
        return idFuncionario != 0 && idFuncionario == outro.idFuncionario;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFuncionario);
    }

    @Override
    public String toString() {
        return nome;
    }
}
