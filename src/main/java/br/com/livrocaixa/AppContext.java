package br.com.livrocaixa;

import br.com.livrocaixa.controller.*;
import br.com.livrocaixa.dev.ArmazenamentoMemoria;

/** Raiz de composição: único lugar que conhece as implementações dos DAOs. */
public final class AppContext {
    private final ArmazenamentoMemoria memoria = new ArmazenamentoMemoria();

    public AppContext() {
        String modo = System.getProperty("livrocaixa.modo", "demo");
        if (!"demo".equalsIgnoreCase(modo)) {
            throw new IllegalStateException(
                    "O modo banco será habilitado quando os DAOs JDBC forem entregues. Use -Dlivrocaixa.modo=demo.");
        }
    }

    public final AutenticacaoController autenticacao = new AutenticacaoController(memoria.funcionarioDAO);
    public final PerfilController perfis = new PerfilController(memoria.perfilDAO);
    public final FuncionarioController funcionarios = new FuncionarioController(memoria.funcionarioDAO);
    public final ClienteController clientes = new ClienteController(memoria.clienteDAO);
    public final FormaPagamentoController formasPagamento = new FormaPagamentoController(memoria.formaPagamentoDAO);
    public final TipoConsultaController tiposConsulta = new TipoConsultaController(memoria.tipoConsultaDAO);
    public final TipoSaidaController tiposSaida = new TipoSaidaController(memoria.tipoSaidaDAO);
    public final ConsultaController consultas = new ConsultaController(memoria.consultaDAO, clientes, tiposConsulta);
    public final SaidaController saidas = new SaidaController(memoria.saidaDAO, tiposSaida);
    public final ParcelamentoController parcelamentos = new ParcelamentoController(
            memoria.parcelaDAO, memoria.pagamentoDAO, memoria.saidaDAO);
    public final RelatorioController relatorios = new RelatorioController(memoria.pagamentoDAO, memoria.saidaDAO);
}
