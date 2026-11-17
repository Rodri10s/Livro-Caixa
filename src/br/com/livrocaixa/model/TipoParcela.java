package br.com.livrocaixa.model;

import java.util.Objects;

/** Tipo de parcela: à vista ou a prazo (tabela TipoParcela). */
public class TipoParcela {

    private int idTipoParcela;
    private String descricao;

    public TipoParcela() {
    }

    public TipoParcela(String descricao) {
        setDescricao(descricao);
    }

    public TipoParcela(int idTipoParcela, String descricao) {
        this.idTipoParcela = idTipoParcela;
        setDescricao(descricao);
    }

    public int getIdTipoParcela() { return idTipoParcela; }
    public void setIdTipoParcela(int idTipoParcela) { this.idTipoParcela = idTipoParcela; }

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
        if (!(o instanceof TipoParcela outro)) return false;
        return idTipoParcela != 0 && idTipoParcela == outro.idTipoParcela;
    }

    @Override
    public int hashCode() { return Objects.hash(idTipoParcela); }

    @Override
    public String toString() { return descricao; }
}
