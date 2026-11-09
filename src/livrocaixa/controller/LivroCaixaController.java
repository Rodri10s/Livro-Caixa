package livrocaixa.controller;

import livrocaixa.model.LivroCaixaModel;
import livrocaixa.view.LivroCaixaView;

import java.util.Objects;

/**
 * Liga a camada de dados à interface do Livro Caixa.
 *
 * <p>Nesta etapa visual, o Controller é responsável por montar as duas
 * dependências principais e iniciar a View. Quando as regras de negócio e o
 * banco de dados forem implementados, os eventos das telas poderão ser
 * encaminhados por esta camada sem misturar Swing com o Model.</p>
 */
public final class LivroCaixaController {
    private final LivroCaixaModel model;

    /** Cria o Controller com o cenário demonstrativo fornecido pelo Model. */
    public LivroCaixaController() {
        this(LivroCaixaModel.load());
    }

    /** Permite fornecer outro Model, facilitando testes e futuras integrações. */
    public LivroCaixaController(LivroCaixaModel model) {
        this.model = Objects.requireNonNull(model, "model");
    }

    /** Disponibiliza os dados demonstrativos usados durante a execução do front. */
    public LivroCaixaModel getModel() {
        return model;
    }

    /** Inicia a View e entrega a ela este Controller. */
    public void iniciar() {
        LivroCaixaView.start(this);
    }
}
