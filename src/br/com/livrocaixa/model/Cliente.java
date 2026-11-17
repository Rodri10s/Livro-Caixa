package br.com.livrocaixa.model;

import java.util.Objects;

/** Cliente do escritório - somente pessoa física (tabela Cliente). */
public class Cliente {

    private int idCliente;
    private String nome;
    private String cpf; // armazenado no formato 000.000.000-00 (VARCHAR(14))

    public Cliente() {
    }

    public Cliente(String nome, String cpf) {
        setNome(nome);
        setCpf(cpf);
    }

    public Cliente(int idCliente, String nome, String cpf) {
        this(nome, cpf);
        this.idCliente = idCliente;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do cliente é obrigatório.");
        }
        if (nome.length() > 45) {
            throw new IllegalArgumentException("O nome deve ter no máximo 45 caracteres.");
        }
        this.nome = nome.trim();
    }

    public String getCpf() {
        return cpf;
    }

    /** Valida formato e integridade do CPF (RNF003) e o guarda formatado. */
    public void setCpf(String cpf) {
        if (!validarCPF(cpf)) {
            throw new IllegalArgumentException("CPF inválido.");
        }
        String d = somenteDigitos(cpf);
        this.cpf = d.substring(0, 3) + "." + d.substring(3, 6) + "."
                + d.substring(6, 9) + "-" + d.substring(9);
    }

    /** ValidarCPF() do diagrama de classes (RNF003). Aceita com ou sem máscara. */
    public static boolean validarCPF(String cpf) {
        if (cpf == null)
            return false;
        String d = somenteDigitos(cpf);
        if (d.length() != 11)
            return false;
        if (d.chars().distinct().count() == 1)
            return false; // 111.111.111-11 etc.
        return digitoVerificador(d, 9) == d.charAt(9) - '0'
                && digitoVerificador(d, 10) == d.charAt(10) - '0';
    }

    /** Valida o CPF atualmente armazenado neste cliente. */
    public boolean validarCPF() {
        return validarCPF(cpf);
    }

    private static int digitoVerificador(String d, int tamanho) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++) {
            soma += (d.charAt(i) - '0') * (tamanho + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

    private static String somenteDigitos(String s) {
        return s.replaceAll("\\D", "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Cliente outro))
            return false;
        return Objects.equals(cpf, outro.cpf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cpf);
    }

    @Override
    public String toString() {
        return nome + " (" + cpf + ")";
    }
}
