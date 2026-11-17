package br.com.livrocaixa.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Parcela de uma fatura (RF06, RF08, RF09, RF12, RF13). */
public class Parcela {

    private int idParcela;
    private BigDecimal valor;
    private LocalDate dataVencimento;
    private boolean situacaoPago;      // TINYINT: 0 = em aberto, 1 = paga
    private int numParcelas;           // total de parcelas da fatura
    private int posicaoParcela;        // posição (1..numParcelas)
    private BigDecimal desconto;       // opcional
    private Fatura fatura;
    private TipoParcela tipoParcela;
    private final List<Pagamento> pagamentos = new ArrayList<>();

    public Parcela() {
    }

    public Parcela(Fatura fatura, TipoParcela tipoParcela, BigDecimal valor,
                   LocalDate dataVencimento, int numParcelas, int posicaoParcela) {
        setFatura(fatura);
        setTipoParcela(tipoParcela);
        setValor(valor);
        setDataVencimento(dataVencimento);
        if (numParcelas < 1 || posicaoParcela < 1 || posicaoParcela > numParcelas) {
            throw new IllegalArgumentException("Posição/quantidade de parcelas inválida.");
        }
        this.numParcelas = numParcelas;
        this.posicaoParcela = posicaoParcela;
    }

    // ---------------- Getters / Setters ----------------

    public int getIdParcela() { return idParcela; }
    public void setIdParcela(int idParcela) { this.idParcela = idParcela; }

    public BigDecimal getValor() { return valor; }

    public void setValor(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("O valor da parcela deve ser maior que zero.");
        }
        this.valor = valor;
    }

    public LocalDate getDataVencimento() { return dataVencimento; }

    public void setDataVencimento(LocalDate dataVencimento) {
        if (dataVencimento == null) throw new IllegalArgumentException("O vencimento é obrigatório.");
        this.dataVencimento = dataVencimento;
    }

    public boolean isSituacaoPago() { return situacaoPago; }
    public void setSituacaoPago(boolean situacaoPago) { this.situacaoPago = situacaoPago; }

    public int getNumParcelas() { return numParcelas; }
    public void setNumParcelas(int numParcelas) { this.numParcelas = numParcelas; }

    public int getPosicaoParcela() { return posicaoParcela; }
    public void setPosicaoParcela(int posicaoParcela) { this.posicaoParcela = posicaoParcela; }

    public BigDecimal getDesconto() { return desconto; }

    public void setDesconto(BigDecimal desconto) {
        if (desconto != null && (desconto.signum() < 0 || (valor != null && desconto.compareTo(valor) > 0))) {
            throw new IllegalArgumentException("Desconto inválido.");
        }
        this.desconto = desconto;
    }

    public Fatura getFatura() { return fatura; }

    public void setFatura(Fatura fatura) {
        if (fatura == null) throw new IllegalArgumentException("A fatura é obrigatória.");
        this.fatura = fatura;
    }

    public TipoParcela getTipoParcela() { return tipoParcela; }

    public void setTipoParcela(TipoParcela tipoParcela) {
        if (tipoParcela == null) throw new IllegalArgumentException("O tipo de parcela é obrigatório.");
        this.tipoParcela = tipoParcela;
    }

    public List<Pagamento> getPagamentos() { return Collections.unmodifiableList(pagamentos); }

    /** Uso interno/DAO: adiciona pagamento carregado do banco. */
    public void adicionarPagamento(Pagamento pagamento) {
        pagamento.setParcela(this);
        pagamentos.add(pagamento);
    }

    // ---------------- Regras de negócio ----------------

    /** Valor efetivamente devido (valor - desconto). */
    public BigDecimal getValorLiquido() {
        return desconto == null ? valor : valor.subtract(desconto);
    }

    /** RF09: parcela vencida e não quitada. */
    public boolean isAtrasada() {
        return !situacaoPago && dataVencimento.isBefore(LocalDate.now());
    }

    /** AlterarParcela() - RF06: o Administrador pode editar os valores antes de quitar. */
    public void alterarParcela(BigDecimal novoValor, LocalDate novoVencimento, BigDecimal novoDesconto) {
        if (situacaoPago) {
            throw new IllegalStateException("Não é possível alterar uma parcela já paga.");
        }
        setValor(novoValor);
        setDataVencimento(novoVencimento);
        setDesconto(novoDesconto);
    }

    /**
     * BaixarParcela() - RF12 / UC007: registra o recebimento, cria o
     * Pagamento correspondente e marca a parcela como paga.
     */
    public Pagamento baixarParcela(FormaPagamento formaPagamento, LocalDate dataPagamento) {
        if (situacaoPago) {
            throw new IllegalStateException("Esta parcela já foi paga.");
        }
        Pagamento pagamento = new Pagamento(this, formaPagamento, getValorLiquido(), dataPagamento);
        pagamentos.add(pagamento);
        situacaoPago = true;
        return pagamento;
    }

    /**
     * DesfazerBaixa() - RF13: volta a parcela para pendente e remove a entrada do
     * fluxo de caixa. Os registros removidos ficam em {@code getPagamentos()} vazio;
     * o DAO deve excluir os Pagamento correspondentes do banco.
     */
    public void desfazerBaixa() {
        if (!situacaoPago) {
            throw new IllegalStateException("Esta parcela não está paga.");
        }
        pagamentos.clear();
        situacaoPago = false;
    }

    /**
     * CancelarParcela() - RF13: cancela definitivamente uma parcela lançada
     * indevidamente. Parcela paga deve ter a baixa desfeita antes.
     * O DAO deve excluir a parcela do banco.
     */
    public void cancelarParcela() {
        if (situacaoPago) {
            throw new IllegalStateException("Desfaça a baixa antes de cancelar a parcela.");
        }
        if (fatura != null) fatura.removerParcela(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Parcela outro)) return false;
        return idParcela != 0 && idParcela == outro.idParcela;
    }

    @Override
    public int hashCode() { return Objects.hash(idParcela); }
}
