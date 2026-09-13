package br.com.livrocaixa.model;

import java.time.LocalDate;

public class Consulta {
    private int idConsulta;
    private String observacao;
    private LocalDate data;
    private Fatura fatura;
    private Cliente cliente;
    private TipoConsulta tipoConsulta;
    private Funcionario funcionario;

    public Consulta() {
    }

    public Consulta(int idConsulta, String observacao, LocalDate data, Fatura fatura, Cliente cliente,
            TipoConsulta tipoConsulta, Funcionario funcionario) {
        this.idConsulta = idConsulta;
        this.observacao = observacao;
        this.data = data;
        this.fatura = fatura;
        this.cliente = cliente;
        this.tipoConsulta = tipoConsulta;
        this.funcionario = funcionario;
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
        this.observacao = observacao;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public Fatura getFatura() {
        return fatura;
    }

    public void setFatura(Fatura fatura) {
        this.fatura = fatura;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public TipoConsulta getTipoConsulta() {
        return tipoConsulta;
    }

    public void setTipoConsulta(TipoConsulta tipoConsulta) {
        this.tipoConsulta = tipoConsulta;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
    }

}