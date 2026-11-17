package br.com.livrocaixa.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Classe associativa entre Parcela e FormaPagamento (tabela Pagamento).
 * Representa a entrada financeira efetiva no livro caixa. No diagrama de
 * classes aparece como "ParcelaPagamento".
 */
public class Pagamento {

    private int idParcelaPagamento;
    private Parcela parcela;
    private FormaPagamento formaPagamento;
    private BigDecimal valor;
    private LocalDate data;

    public Pagamento() {
    }

    public Pagamento(Parcela parcela, FormaPagamento formaPagamento,
                            BigDecimal valor, LocalDate data) {
        setParcela(parcela);
        setFormaPagamento(formaPagamento);
        setValor(valor);
        setData(data);
    }

    public int getIdParcelaPagamento() { return idParcelaPagamento; }
    public void setIdParcelaPagamento(int idParcelaPagamento) { this.idParcelaPagamento = idParcelaPagamento; }

    public Parcela getParcela() { return parcela; }

    public void setParcela(Parcela parcela) {
        if (parcela == null) throw new IllegalArgumentException("A parcela é obrigatória.");
        this.parcela = parcela;
    }

    public FormaPagamento getFormaPagamento() { return formaPagamento; }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        if (formaPagamento == null) throw new IllegalArgumentException("A forma de pagamento é obrigatória.");
        this.formaPagamento = formaPagamento;
    }

    public BigDecimal getValor() { return valor; }

    public void setValor(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("O valor do pagamento deve ser maior que zero.");
        }
        this.valor = valor;
    }

    public LocalDate getData() { return data; }

    public void setData(LocalDate data) {
        if (data == null) throw new IllegalArgumentException("A data do pagamento é obrigatória.");
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pagamento outro)) return false;
        return idParcelaPagamento != 0 && idParcelaPagamento == outro.idParcelaPagamento;
    }

    @Override
    public int hashCode() { return Objects.hash(idParcelaPagamento); }
}
