package br.com.livrocaixa.dev;

import br.com.livrocaixa.dao.*;
import br.com.livrocaixa.model.*;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DESENVOLVIMENTO — substituir pelos DAOs reais.
 * Armazenamento compartilhado usado somente para navegar e testar as Views antes do MySQL.
 */
public final class ArmazenamentoMemoria {
    private final List<Perfil> perfis = new ArrayList<>();
    private final List<Funcionario> funcionarios = new ArrayList<>();
    private final List<Cliente> clientes = new ArrayList<>();
    private final List<TipoConsulta> tiposConsulta = new ArrayList<>();
    private final List<TipoSaida> tiposSaida = new ArrayList<>();
    private final List<FormaPagamento> formasPagamento = new ArrayList<>();
    private final List<Consulta> consultas = new ArrayList<>();
    private final List<Parcela> parcelas = new ArrayList<>();
    private final List<Saida> saidas = new ArrayList<>();
    private final AtomicInteger proximoId = new AtomicInteger(100);

    public final PerfilDAO perfilDAO = new DominioDAO<>(perfis);
    public final TipoConsultaDAO tipoConsultaDAO = new TipoConsultaMemoria();
    public final TipoSaidaDAO tipoSaidaDAO = new TipoSaidaMemoria();
    public final FormaPagamentoDAO formaPagamentoDAO = new FormaPagamentoMemoria();
    public final FuncionarioDAO funcionarioDAO = new FuncionarioMemoria();
    public final ClienteDAO clienteDAO = new ClienteMemoria();
    public final ConsultaDAO consultaDAO = new ConsultaMemoria();
    public final ParcelaDAO parcelaDAO = new ParcelaMemoria();
    public final PagamentoDAO pagamentoDAO = new PagamentoMemoria();
    public final SaidaDAO saidaDAO = new SaidaMemoria();

    public ArmazenamentoMemoria() {
        carregarAmostraTemporaria();
    }

    private void carregarAmostraTemporaria() {
        Perfil admin = new Perfil(1, "Administrador");
        Perfil recep = new Perfil(2, "Recepcionista");
        perfis.addAll(List.of(admin, recep));

        funcionarios.add(funcionario(1, "Ana Souza", "123456", admin));
        funcionarios.add(funcionario(2, "Bruno Reis", "123456", recep));
        clientes.add(new Cliente(1, "Carlos Mendes", "529.982.247-25"));
        clientes.add(new Cliente(2, "Mariana Silva", "111.444.777-35"));
        tiposConsulta.addAll(List.of(
                new TipoConsulta(1, "Consulta de rotina"),
                new TipoConsulta(2, "Retorno"),
                new TipoConsulta(3, "Avaliação")));
        tiposSaida.addAll(List.of(
                new TipoSaida(1, "Aluguel"),
                new TipoSaida(2, "Material de escritório"),
                new TipoSaida(3, "Energia")));
        formasPagamento.addAll(List.of(
                new FormaPagamento(1, "Espécie"),
                new FormaPagamento(2, "PIX"),
                new FormaPagamento(3, "RPV")));
    }

    private Funcionario funcionario(int id, String nome, String senha, Perfil perfil) {
        Funcionario f = new Funcionario();
        f.setId(id); f.setNome(nome); f.setSenha(senha); f.setPerfil(perfil);
        return f;
    }

    private int novoId() { return proximoId.getAndIncrement(); }

    private abstract class CadastroDAO<T extends DominioSimples> {
        final List<T> dados;
        CadastroDAO(List<T> dados) { this.dados = dados; }
        Optional<T> buscar(Integer id) { return dados.stream().filter(v -> Objects.equals(v.getId(), id)).findFirst(); }
        List<T> todos() { return List.copyOf(dados); }
        void inserirItem(T item) { item.setId(novoId()); dados.add(item); }
        void atualizarItem(T item) {
            int indice = indicePorId(dados, item.getId());
            if (indice >= 0) dados.set(indice, item);
        }
        void excluirItem(Integer id) { dados.removeIf(v -> Objects.equals(v.getId(), id)); }
    }

    private final class DominioDAO<T extends DominioSimples> extends CadastroDAO<T> implements PerfilDAO {
        DominioDAO(List<T> dados) { super(dados); }
        @SuppressWarnings("unchecked") public Optional<Perfil> buscarPorId(Integer id) { return (Optional<Perfil>) (Optional<?>) buscar(id); }
        @SuppressWarnings("unchecked") public List<Perfil> listar() { return (List<Perfil>) (List<?>) todos(); }
        @SuppressWarnings("unchecked") public void inserir(Perfil item) { inserirItem((T) item); }
        @SuppressWarnings("unchecked") public void atualizar(Perfil item) { atualizarItem((T) item); }
        public void excluir(Integer id) { excluirItem(id); }
    }

    private final class TipoConsultaMemoria extends CadastroDAO<TipoConsulta> implements TipoConsultaDAO {
        TipoConsultaMemoria() { super(tiposConsulta); }
        public Optional<TipoConsulta> buscarPorId(Integer id) { return buscar(id); }
        public List<TipoConsulta> listar() { return todos(); }
        public void inserir(TipoConsulta item) { inserirItem(item); }
        public void atualizar(TipoConsulta item) { atualizarItem(item); }
        public void excluir(Integer id) { excluirItem(id); }
    }

    private final class TipoSaidaMemoria extends CadastroDAO<TipoSaida> implements TipoSaidaDAO {
        TipoSaidaMemoria() { super(tiposSaida); }
        public Optional<TipoSaida> buscarPorId(Integer id) { return buscar(id); }
        public List<TipoSaida> listar() { return todos(); }
        public void inserir(TipoSaida item) { inserirItem(item); }
        public void atualizar(TipoSaida item) { atualizarItem(item); }
        public void excluir(Integer id) { excluirItem(id); }
    }

    private final class FormaPagamentoMemoria extends CadastroDAO<FormaPagamento> implements FormaPagamentoDAO {
        FormaPagamentoMemoria() { super(formasPagamento); }
        public Optional<FormaPagamento> buscarPorId(Integer id) { return buscar(id); }
        public List<FormaPagamento> listar() { return todos(); }
        public void inserir(FormaPagamento item) { inserirItem(item); }
        public void atualizar(FormaPagamento item) { atualizarItem(item); }
        public void excluir(Integer id) { excluirItem(id); }
    }

    private final class ClienteMemoria implements ClienteDAO {
        public Optional<Cliente> buscarPorId(Integer id) { return clientes.stream().filter(v -> Objects.equals(v.getId(), id)).findFirst(); }
        public Optional<Cliente> buscarPorCpf(String cpf) { return clientes.stream().filter(v -> Objects.equals(v.getCpf(), cpf)).findFirst(); }
        public List<Cliente> listar() { return List.copyOf(clientes); }
        public void inserir(Cliente item) { item.setId(novoId()); clientes.add(item); }
        public void atualizar(Cliente item) { int i = indicePorId(clientes, item.getId()); if (i >= 0) clientes.set(i, item); }
        public void excluir(Integer id) { clientes.removeIf(v -> Objects.equals(v.getId(), id)); }
    }

    private final class FuncionarioMemoria implements FuncionarioDAO {
        public Optional<Funcionario> buscarPorNome(String nome) { return funcionarios.stream().filter(v -> v.getNome().equalsIgnoreCase(nome)).findFirst(); }
        public Optional<Funcionario> buscarPorId(Integer id) { return funcionarios.stream().filter(v -> Objects.equals(v.getId(), id)).findFirst(); }
        public List<Funcionario> listar() { return List.copyOf(funcionarios); }
        public void inserir(Funcionario item) { item.setId(novoId()); funcionarios.add(item); }
        public void atualizar(Funcionario item) { int i = indicePorId(funcionarios, item.getId()); if (i >= 0) funcionarios.set(i, item); }
        public void excluir(Integer id) { funcionarios.removeIf(v -> Objects.equals(v.getId(), id)); }
    }

    private final class ConsultaMemoria implements ConsultaDAO {
        public void inserirComFatura(Consulta consulta) {
            consulta.setId(novoId());
            consulta.getFatura().setId(novoId());
            consulta.getFatura().getParcelas().forEach(p -> p.setId(novoId()));
            consultas.add(consulta);
            parcelas.addAll(consulta.getFatura().getParcelas());
        }
        public Optional<Consulta> buscarPorId(Integer id) { return consultas.stream().filter(v -> Objects.equals(v.getId(), id)).findFirst(); }
    }

    private final class ParcelaMemoria implements ParcelaDAO {
        public List<Parcela> listarPorCliente(Integer idCliente) {
            return parcelas.stream().filter(p -> p.getFatura() != null && p.getFatura().getConsulta() != null
                    && p.getFatura().getConsulta().getCliente() != null
                    && Objects.equals(p.getFatura().getConsulta().getCliente().getId(), idCliente)).toList();
        }
        public List<Parcela> listarAtrasadas(LocalDate data, Integer idCliente) {
            return parcelas.stream().filter(p -> p.isAtrasada(data))
                    .filter(p -> idCliente == null || (p.getFatura() != null && p.getFatura().getConsulta() != null
                            && Objects.equals(p.getFatura().getConsulta().getCliente().getId(), idCliente))).toList();
        }
        public Optional<Parcela> buscarPorId(Integer id) { return parcelas.stream().filter(v -> Objects.equals(v.getId(), id)).findFirst(); }
        public void atualizarValor(Parcela parcela) { }
        public void registrarBaixa(Parcela parcela, Pagamento pagamento) { pagamento.setId(novoId()); }
        public void desfazerBaixa(Parcela parcela, Pagamento pagamento) { }
        public void cancelar(Parcela parcela, List<Pagamento> pagamentos) { }
    }

    private final class PagamentoMemoria implements PagamentoDAO {
        private List<Pagamento> todos() { return parcelas.stream().flatMap(p -> p.getPagamentos().stream()).toList(); }
        public List<Pagamento> listarPorPeriodo(LocalDate inicio, LocalDate fim) {
            return todos().stream().filter(p -> !p.getData().isBefore(inicio) && !p.getData().isAfter(fim)).toList();
        }
        public List<Pagamento> listarPorFuncionario(Integer id) {
            return todos().stream().filter(p -> p.getFuncionario() != null && Objects.equals(p.getFuncionario().getId(), id)).toList();
        }
    }

    private final class SaidaMemoria implements SaidaDAO {
        public void inserir(Saida item) { item.setId(novoId()); saidas.add(item); }
        public List<Saida> listarPorPeriodo(LocalDate inicio, LocalDate fim) {
            return saidas.stream().filter(s -> !s.getData().isBefore(inicio) && !s.getData().isAfter(fim)).toList();
        }
        public List<Saida> listarPorFuncionario(Integer id) {
            return saidas.stream().filter(s -> s.getFuncionario() != null && Objects.equals(s.getFuncionario().getId(), id)).toList();
        }
    }

    private static <T> int indicePorId(List<T> itens, Integer id) {
        for (int i = 0; i < itens.size(); i++) {
            Object item = itens.get(i);
            Integer atual = item instanceof Cliente v ? v.getId()
                    : item instanceof Funcionario v ? v.getId()
                    : item instanceof DominioSimples v ? v.getId() : null;
            if (Objects.equals(atual, id)) return i;
        }
        return -1;
    }
}
