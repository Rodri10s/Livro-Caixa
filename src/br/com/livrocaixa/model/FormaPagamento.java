package br.com.livrocaixa.model;

import java.util.Objects;

/** Forma de pagamento aceita: Espécie, PIX, RPV (tabela FormaPagamento). */
public class FormaPagamento {

    private int idFormaPagamento;
    private String descricao;

    public FormaPagamento() {
    }

    public FormaPagamento(String descricao) {
        setDescricao(descricao);
    }

    public FormaPagamento(int idFormaPagamento, String descricao) {
        this.idFormaPagamento = idFormaPagamento;
        setDescricao(descricao);
    }

    public int getIdFormaPagamento() {
        return idFormaPagamento;
    }

    public void setIdFormaPagamento(int idFormaPagamento) {
        this.idFormaPagamento = idFormaPagamento;
    }

    public String getDescricao() {
        return descricao;
    }

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
        if (this == o)
            return true;
        if (!(o instanceof FormaPagamento outro))
            return false;
        return idFormaPagamento != 0 && idFormaPagamento == outro.idFormaPagamento;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFormaPagamento);
    }

    @Override
    public String toString() {
        return descricao;
    }
}
