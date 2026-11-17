package br.com.livrocaixa.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Fatura de uma consulta; agrega as parcelas (RF04, RF06, RF07). */
public class Fatura {

    private int idFatura;
    private BigDecimal valorTotal;
    private LocalDate dataInicial;
    private Consulta consulta;
    private final List<Parcela> parcelas = new ArrayList<>();

    public Fatura() {
    }

    public Fatura(BigDecimal valorTotal, LocalDate dataInicial, Consulta consulta) {
        setValorTotal(valorTotal);
        setDataInicial(dataInicial);
        setConsulta(consulta);
    }

    public int getIdFatura() {
        return idFatura;
    }

    public void setIdFatura(int idFatura) {
        this.idFatura = idFatura;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        if (valorTotal == null || valorTotal.signum() <= 0) {
            throw new IllegalArgumentException("O valor total da fatura deve ser maior que zero.");
        }
        this.valorTotal = valorTotal.setScale(2, RoundingMode.HALF_UP);
    }

    public LocalDate getDataInicial() {
        return dataInicial;
    }

    public void setDataInicial(LocalDate dataInicial) {
        if (dataInicial == null)
            throw new IllegalArgumentException("A data inicial é obrigatória.");
        this.dataInicial = dataInicial;
    }

    public Consulta getConsulta() {
        return consulta;
    }

    public void setConsulta(Consulta consulta) {
        if (consulta == null)
            throw new IllegalArgumentException("A consulta é obrigatória.");
        this.consulta = consulta;
    }

    public List<Parcela> getParcelas() {
        return Collections.unmodifiableList(parcelas);
    }

    /** Uso interno/DAO: adiciona parcela já existente (ex.: carregada do banco). */
    public void adicionarParcela(Parcela parcela) {
        parcela.setFatura(this);
        parcelas.add(parcela);
    }

    /** Usado por Parcela.cancelarParcela(). */
    void removerParcela(Parcela parcela) {
        parcelas.remove(parcela);
    }

    // ---------------- GerarParcelas() ----------------

    /** Pagamento à vista: uma única parcela vencendo na data informada. */
    public List<Parcela> gerarParcelasAVista(TipoParcela tipoAVista, LocalDate vencimento) {
        return gerarParcelas(tipoAVista, 1, vencimento);
    }

    /**
     * Pagamento a prazo: divide o valor total em n parcelas mensais (RF06).
     * A última parcela absorve a diferença de arredondamento, de modo que a
     * soma das parcelas é sempre igual ao valor total da fatura.
     */
    public List<Parcela> gerarParcelasAPrazo(TipoParcela tipoAPrazo, int numParcelas,
            LocalDate primeiroVencimento) {
        return gerarParcelas(tipoAPrazo, numParcelas, primeiroVencimento);
    }

    private List<Parcela> gerarParcelas(TipoParcela tipo, int n, LocalDate primeiroVencimento) {
        if (n < 1)
            throw new IllegalArgumentException("O número de parcelas deve ser ao menos 1.");
        if (tipo == null)
            throw new IllegalArgumentException("O tipo de parcela é obrigatório.");
        if (primeiroVencimento == null)
            throw new IllegalArgumentException("O vencimento é obrigatório.");
        if (!parcelas.isEmpty() && temParcelaPaga()) {
            throw new IllegalStateException("Não é possível regerar parcelas: há parcelas já pagas.");
        }

        parcelas.clear();
        BigDecimal valorBase = valorTotal.divide(BigDecimal.valueOf(n), 2, RoundingMode.DOWN);
        BigDecimal acumulado = BigDecimal.ZERO;

        for (int i = 1; i <= n; i++) {
            BigDecimal valor = (i == n) ? valorTotal.subtract(acumulado) : valorBase;
            acumulado = acumulado.add(valor);
            parcelas.add(new Parcela(this, tipo, valor,
                    primeiroVencimento.plusMonths(i - 1L), n, i));
        }
        return getParcelas();
    }

    private boolean temParcelaPaga() {
        return parcelas.stream().anyMatch(Parcela::isSituacaoPago);
    }

    // ---------------- Consultas auxiliares ----------------

    /**
     * Soma dos valores das parcelas (útil após o Administrador editar valores -
     * RF06).
     */
    public BigDecimal getSomaParcelas() {
        return parcelas.stream().map(Parcela::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Confere se a soma das parcelas fecha com o valor total antes de salvar. */
    public boolean validarSomaParcelas() {
        return getSomaParcelas().compareTo(valorTotal) == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Fatura outro))
            return false;
        return idFatura != 0 && idFatura == outro.idFatura;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFatura);
    }
}
