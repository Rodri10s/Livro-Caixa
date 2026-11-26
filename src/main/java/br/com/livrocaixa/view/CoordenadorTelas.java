package br.com.livrocaixa.view;

import br.com.livrocaixa.AppContext;
import br.com.livrocaixa.model.Funcionario;
import br.com.livrocaixa.view.dados.LivroCaixaModel;

import java.util.Objects;

/**
 * Liga a camada de dados à interface do Livro Caixa.
 *
 * <p>Nesta etapa visual, o Controller é responsável por montar as duas
 * dependências principais e iniciar a View. Quando as regras de negócio e o
 * banco de dados forem implementados, os eventos das telas poderão ser
 * encaminhados por esta camada sem misturar Swing com o Model.</p>
 */
public final class CoordenadorTelas {
    private final AppContext context;
    private final LivroCaixaModel model;

    /** Cria o Controller com o cenário demonstrativo fornecido pelo Model. */
    public CoordenadorTelas() {
        this.context = new AppContext();
        this.model = LivroCaixaModel.load();
    }

    /** Permite fornecer outro Model, facilitando testes e futuras integrações. */
    public CoordenadorTelas(LivroCaixaModel model) {
        this.context = new AppContext();
        this.model = Objects.requireNonNull(model, "model");
    }

    /** Disponibiliza os dados demonstrativos usados durante a execução do front. */
    public LivroCaixaModel getModel() {
        return model;
    }

    /** Autentica pela camada Controller real do projeto MVC. */
    public Funcionario autenticar(String nome, String senha) {
        Funcionario funcionario = context.autenticacao.login(nome, senha);
        model.connect(context);
        return funcionario;
    }

    public void encerrarSessao() {
        context.autenticacao.logout();
    }

    public AppContext getContext() {
        return context;
    }

    /** Inicia a View e entrega a ela este Controller. */
    public void iniciar() {
        TelaLivroCaixa.start(this);
    }
}
