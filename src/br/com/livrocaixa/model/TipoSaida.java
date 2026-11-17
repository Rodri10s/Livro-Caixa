package br.com.livrocaixa.model;

import java.util.Objects;

/** Tipo de saída/gasto do escritório (tabela TipoSaida). */
public class TipoSaida {

    private int idTipoSaida;
    private String descricao;

    public TipoSaida() {
    }

    public TipoSaida(String descricao) {
        setDescricao(descricao);
    }

    public TipoSaida(int idTipoSaida, String descricao) {
        this.idTipoSaida = idTipoSaida;
        setDescricao(descricao);
    }

    public int getIdTipoSaida() { return idTipoSaida; }
    public void setIdTipoSaida(int idTipoSaida) { this.idTipoSaida = idTipoSaida; }

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
        if (!(o instanceof TipoSaida outro)) return false;
        return idTipoSaida != 0 && idTipoSaida == outro.idTipoSaida;
    }

    @Override
    public int hashCode() { return Objects.hash(idTipoSaida); }

    @Override
    public String toString() { return descricao; }
}
