package br.com.livrocaixa.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Consulta realizada para um cliente; gera a fatura (entrada) - UC003, RF04.
 */
public class Consulta {

    private int idConsulta;
    private String observacao;
    private LocalDate data;
    private Cliente cliente;
    private TipoConsulta tipoConsulta;
    private Funcionario funcionario;
    private Fatura fatura; // composição 1..1

    public Consulta() {
    }

    public Consulta(String observacao, LocalDate data, Cliente cliente,
            TipoConsulta tipoConsulta, Funcionario funcionario) {
        setObservacao(observacao);
        setData(data);
        setCliente(cliente);
        setTipoConsulta(tipoConsulta);
        setFuncionario(funcionario);
    }

    public int getIdConsulta() {
        return idConsulta;
    }

    public void setIdConsulta(int idConsulta) {
        this.idConsulta = idConsulta;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        if (observacao != null && observacao.length() > 45) {
            throw new IllegalArgumentException("A observação deve ter no máximo 45 caracteres.");
        }
        this.observacao = observacao;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        if (data == null)
            throw new IllegalArgumentException("A data da consulta é obrigatória.");
        this.data = data;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        if (cliente == null)
            throw new IllegalArgumentException("O cliente é obrigatório.");
        this.cliente = cliente;
    }

    public TipoConsulta getTipoConsulta() {
        return tipoConsulta;
    }

    public void setTipoConsulta(TipoConsulta tipoConsulta) {
        if (tipoConsulta == null)
            throw new IllegalArgumentException("O tipo de consulta é obrigatório.");
        this.tipoConsulta = tipoConsulta;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        if (funcionario == null)
            throw new IllegalArgumentException("O funcionário é obrigatório.");
        this.funcionario = funcionario;
    }

    public Fatura getFatura() {
        return fatura;
    }

    /** Uso interno/DAO: associa a fatura carregada do banco. */
    public void setFatura(Fatura fatura) {
        this.fatura = fatura;
    }

    /**
     * GerarFatura() do diagrama de classes: cada consulta possui uma única fatura.
     */
    public Fatura gerarFatura(BigDecimal valorTotal, LocalDate dataInicial) {
        if (fatura != null) {
            throw new IllegalStateException("Esta consulta já possui fatura.");
        }
        this.fatura = new Fatura(valorTotal, dataInicial, this);
        return fatura;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Consulta outro))
            return false;
        return idConsulta != 0 && idConsulta == outro.idConsulta;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idConsulta);
    }
}
