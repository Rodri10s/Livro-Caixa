package livrocaixa;

import livrocaixa.controller.LivroCaixaController;

import javax.swing.SwingUtilities;

/**
 * Ponto de entrada da aplicação.
 *
 * <p>Como orienta o MVC, esta classe apenas monta a camada coordenadora. A
 * criação das janelas ocorre na View e os dados demonstrativos ficam no
 * Model.</p>
 */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        // Swing exige que as janelas sejam criadas na Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> new LivroCaixaController().iniciar());
    }
}
