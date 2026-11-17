package br.com.livrocaixa.model;

import java.util.Objects;

/** Tipo de consulta realizada pelo escritório (tabela TipoConsulta). */
public class TipoConsulta {

    private int idTipoConsulta;
    private String descricao;

    public TipoConsulta() {
    }

    public TipoConsulta(String descricao) {
        setDescricao(descricao);
    }

    public TipoConsulta(int idTipoConsulta, String descricao) {
        this.idTipoConsulta = idTipoConsulta;
        setDescricao(descricao);
    }

    public int getIdTipoConsulta() { return idTipoConsulta; }
    public void setIdTipoConsulta(int idTipoConsulta) { this.idTipoConsulta = idTipoConsulta; }

    public String getDescricao() { return descricao; }

    public void setDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("A descrição é obrigatória.");
        }
        if (descricao.length() > 45) {
            throw new IllegalArgumentException("A descrição deve ter no máximo 45 caracteres.");
        }
        this.descricao = descricao.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TipoConsulta outro)) return false;
        return idTipoConsulta != 0 && idTipoConsulta == outro.idTipoConsulta;
    }

    @Override
    public int hashCode() { return Objects.hash(idTipoConsulta); }

    @Override
    public String toString() { return descricao; }
}
