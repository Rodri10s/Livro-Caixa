package br.com.livrocaixa.model;

import java.util.Objects;

/** Perfil de acesso do funcionário (Administrador ou Recepcionista) - RNF002. */
public class Perfil {

    public static final String ADMINISTRADOR = "Administrador";
    public static final String RECEPCIONISTA = "Recepcionista";

    private int idPerfil;
    private String descricao;

    public Perfil() {
    }

    public Perfil(String descricao) {
        setDescricao(descricao);
    }

    public Perfil(int idPerfil, String descricao) {
        this.idPerfil = idPerfil;
        setDescricao(descricao);
    }

    public int getIdPerfil() { return idPerfil; }
    public void setIdPerfil(int idPerfil) { this.idPerfil = idPerfil; }

    public String getDescricao() { return descricao; }

    public void setDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("A descrição do perfil é obrigatória.");
        }
        if (descricao.length() > 45) {
            throw new IllegalArgumentException("A descrição deve ter no máximo 45 caracteres.");
        }
        this.descricao = descricao.trim();
    }

    public boolean isAdministrador() {
        return ADMINISTRADOR.equalsIgnoreCase(descricao);
    }

    public boolean isRecepcionista() {
        return RECEPCIONISTA.equalsIgnoreCase(descricao);
    }

    // ---- ValidarPermissoes() do diagrama de classes (Seção 3.1 / RNF002) ----

    /** Administrador e Recepcionista podem registrar saídas (UC004). */
    public boolean podeRegistrarSaida() {
        return isAdministrador() || isRecepcionista();
    }

    /** Somente o Administrador registra consultas/faturas, dá baixa em parcelas e gerencia cadastros. */
    public boolean podeRegistrarEntrada() { return isAdministrador(); }
    public boolean podeManterCadastros()  { return isAdministrador(); }
    public boolean podeBaixarParcela()    { return isAdministrador(); }
    public boolean podeConsultarHistorico() { return isAdministrador(); }
    public boolean podeGerarRelatorios()  { return isAdministrador(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Perfil outro)) return false;
        return idPerfil != 0 && idPerfil == outro.idPerfil;
    }

    @Override
    public int hashCode() { return Objects.hash(idPerfil); }

    @Override
    public String toString() { return descricao; }
}
