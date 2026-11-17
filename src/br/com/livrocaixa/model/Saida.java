package br.com.livrocaixa.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/** Saída (gasto) do escritório registrada por um funcionário (UC004, RF05). */
public class Saida {

    private int idSaida;
    private String observacao;
    private BigDecimal valor;
    private LocalDate data;
    private Funcionario funcionario;
    private TipoSaida tipoSaida;

    public Saida() {
    }

    public Saida(String observacao, BigDecimal valor, LocalDate data,
                 Funcionario funcionario, TipoSaida tipoSaida) {
        setObservacao(observacao);
        setValor(valor);
        setData(data);
        setFuncionario(funcionario);
        setTipoSaida(tipoSaida);
    }

    public int getIdSaida() { return idSaida; }
    public void setIdSaida(int idSaida) { this.idSaida = idSaida; }

    public String getObservacao() { return observacao; }

    public void setObservacao(String observacao) {
        if (observacao != null && observacao.length() > 45) {
            throw new IllegalArgumentException("A observação deve ter no máximo 45 caracteres.");
        }
        this.observacao = observacao;
    }

    public BigDecimal getValor() { return valor; }

    public void setValor(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("O valor da saída deve ser maior que zero.");
        }
        this.valor = valor;
    }

    public LocalDate getData() { return data; }

    public void setData(LocalDate data) {
        if (data == null) throw new IllegalArgumentException("A data da saída é obrigatória.");
        this.data = data;
    }

    public Funcionario getFuncionario() { return funcionario; }

    public void setFuncionario(Funcionario funcionario) {
        if (funcionario == null) throw new IllegalArgumentException("O funcionário é obrigatório.");
        this.funcionario = funcionario;
    }

    public TipoSaida getTipoSaida() { return tipoSaida; }

    public void setTipoSaida(TipoSaida tipoSaida) {
        if (tipoSaida == null) throw new IllegalArgumentException("O tipo de saída é obrigatório.");
        this.tipoSaida = tipoSaida;
    }

    /** Valor com sinal negativo, como contabilizado no livro caixa (pós-condição do UC004). */
    public BigDecimal getValorContabil() { return valor.negate(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Saida outro)) return false;
        return idSaida != 0 && idSaida == outro.idSaida;
    }

    @Override
    public int hashCode() { return Objects.hash(idSaida); }
}
