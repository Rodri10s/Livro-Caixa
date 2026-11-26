package br.com.livrocaixa;

import br.com.livrocaixa.view.CoordenadorTelas;

import javax.swing.SwingUtilities;

/** Ponto de entrada que preserva o front visual aprovado durante a integração MVC. */
public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CoordenadorTelas().iniciar());
    }
}
