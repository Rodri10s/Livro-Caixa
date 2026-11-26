package br.com.livrocaixa.view.dados;

import br.com.livrocaixa.AppContext;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Camada Model do protótipo.
 *
 * <p>Guarda os dados demonstrativos e oferece os cálculos usados pela interface.
 * Não conhece janelas, botões ou qualquer classe de {@code javax.swing}. Nesta etapa
 * não existe persistência: {@link #save()} é intencionalmente vazio e {@link #load()}
 * sempre devolve uma amostra estável para apresentação.</p>
 */
public final class LivroCaixaModel implements Serializable {
    public static final LocalDate DEMO_DATE = LocalDate.of(2026, 11, 5);
    private static final Map<Integer, Integer> LEGACY_INVOICE_APPOINTMENTS = Map.of(
            491, 103,
            498, 104,
            502, 105
    );

    public int nextClient = 6;
    public int nextAppointment = 106;
    public int nextInstallment = 511;
    public int nextPayment = 4;
    public int nextExpense = 8;

    public final List<Client> clients = new ArrayList<>();
    public final List<Appointment> appointments = new ArrayList<>();
    public final List<Installment> installments = new ArrayList<>();
    public final List<Payment> payments = new ArrayList<>();
    public final List<Expense> expenses = new ArrayList<>();

    public final List<String> appointmentTypes = new ArrayList<>(
            List.of("Consulta de rotina", "Retorno", "Avaliação"));
    public final List<String> expenseTypes = new ArrayList<>(
            List.of("Aluguel", "Material de escritório", "Energia"));
    public final List<String> paymentMethods = new ArrayList<>(
            List.of("Dinheiro", "PIX", "Cartão de crédito", "Cartão de débito"));

    private transient AppContext context;
    private final transient Map<Integer, br.com.livrocaixa.model.Cliente> realClients = new HashMap<>();
    private final transient Map<Integer, br.com.livrocaixa.model.Parcela> realInstallments = new HashMap<>();
    private final transient Map<Integer, br.com.livrocaixa.model.Pagamento> realPayments = new HashMap<>();

    /**
     * Substitui a amostra visual pelos dados fornecidos pelos Controllers reais.
     * A View continua usando suas projeções leves, sem acessar DAOs ou SQL.
     */
    public void connect(AppContext context) {
        this.context = context;
        clients.clear(); appointments.clear(); installments.clear(); payments.clear(); expenses.clear();
        appointmentTypes.clear(); expenseTypes.clear(); paymentMethods.clear();
        realClients.clear(); realInstallments.clear(); realPayments.clear();
        context.clientes.listar().forEach(item -> {
            realClients.put(item.getId(), item);
            clients.add(new Client(item.getId(), item.getNome(), item.getCpf(), "", "", ""));
        });
        context.tiposConsulta.listar().forEach(item -> appointmentTypes.add(item.getDescricao()));
        context.tiposSaida.listar().forEach(item -> expenseTypes.add(item.getDescricao()));
        context.formasPagamento.listar().forEach(item -> paymentMethods.add(item.getDescricao()));
        nextClient = clients.stream().mapToInt(item -> item.id).max().orElse(0) + 1;
    }

    /** Carrega um cenário novo em memória para o front-end demonstrativo. */
    public static LivroCaixaModel load() {
        return sample();
    }

    /** Sem persistência nesta entrega; será substituído pela integração futura. */
    public void save() {
        // Intencionalmente vazio.
    }

    /** Monta os mesmos registros apresentados nas telas e nos testes do protótipo. */
    public static LivroCaixaModel sample() {
        LivroCaixaModel model = new LivroCaixaModel();
        LocalDate ref = DEMO_DATE;

        model.clients.add(new Client(1, "Carlos Mendes", "123.456.789-00",
                "(85) 99999-1001", "carlos@email.com", "Rua das Flores, 10"));
        model.clients.add(new Client(2, "Mariana Silva", "987.654.321-00",
                "(85) 99999-1002", "mariana@email.com", "Av. Central, 200"));
        model.clients.add(new Client(3, "Roberto Lima", "456.789.123-00",
                "(85) 99999-1003", "roberto@email.com", "Rua Norte, 45"));
        model.clients.add(new Client(4, "Fernanda Costa", "741.852.963-00",
                "(85) 99999-1004", "fernanda@email.com", "Rua Sul, 18"));
        model.clients.add(new Client(5, "Lucas Almeida", "852.963.741-00",
                "(85) 99999-1005", "lucas@email.com", "Av. Leste, 90"));

        model.appointments.add(new Appointment(105, "Carlos Mendes", "Consulta de rotina", ref,
                "Ana Souza", new BigDecimal("1400"), "Retorno em 30 dias", false));
        model.appointments.add(new Appointment(104, "Mariana Silva", "Retorno", ref.minusDays(1),
                "Bruno Reis", new BigDecimal("1260"), "", false));
        model.appointments.add(new Appointment(103, "Roberto Lima", "Avaliação", ref.minusDays(2),
                "Ana Souza", new BigDecimal("1400"), "", false));

        model.installments.add(new Installment(501, 502, "Carlos Mendes", "1/4", ref,
                new BigDecimal("350"), new BigDecimal("100"), false));
        model.installments.add(new Installment(502, 498, "Mariana Silva", "1/3", ref.plusDays(3),
                new BigDecimal("420"), BigDecimal.ZERO, false));
        model.installments.add(new Installment(503, 491, "Roberto Lima", "3/5", ref.minusDays(4),
                new BigDecimal("280"), BigDecimal.ZERO, false));
        model.installments.add(new Installment(504, 502, "Carlos Mendes", "2/4", ref.plusMonths(1),
                new BigDecimal("350"), BigDecimal.ZERO, false));
        model.installments.add(new Installment(505, 502, "Carlos Mendes", "3/4", ref.plusMonths(2),
                new BigDecimal("350"), BigDecimal.ZERO, false));
        model.installments.add(new Installment(506, 498, "Mariana Silva", "2/3", ref.plusMonths(1),
                new BigDecimal("420"), BigDecimal.ZERO, false));
        model.installments.add(new Installment(507, 498, "Mariana Silva", "3/3", ref.plusMonths(2),
                new BigDecimal("420"), BigDecimal.ZERO, false));
        model.installments.add(new Installment(508, 491, "Roberto Lima", "4/5", ref.plusMonths(1),
                new BigDecimal("280"), BigDecimal.ZERO, false));
        model.installments.add(new Installment(509, 491, "Roberto Lima", "5/5", ref.plusMonths(2),
                new BigDecimal("410"), BigDecimal.ZERO, false));
        model.installments.add(new Installment(510, 487, "Fernanda Costa", "1/2", ref.plusDays(7),
                new BigDecimal("600"), new BigDecimal("600"), false));

        model.payments.add(new Payment(1, 502, "Carlos Mendes", "1/4", ref.minusDays(2),
                new BigDecimal("100"), "PIX", BigDecimal.ZERO, false));
        model.payments.add(new Payment(2, 487, "Fernanda Costa", "1/2", ref.minusDays(1),
                new BigDecimal("600"), "Cartão de crédito", BigDecimal.ZERO, false));
        model.payments.add(new Payment(3, 0, "Recebimentos anteriores", "À vista", ref.minusDays(1),
                new BigDecimal("11980"), "PIX", BigDecimal.ZERO, false));

        model.expenses.add(new Expense(1, "Aluguel", "Aluguel", new BigDecimal("1200"),
                LocalDate.of(2026, 11, 1), "Ana Souza", "Pagamento do aluguel do consultório."));
        model.expenses.add(new Expense(2, "Material de escritório", "Material de escritório",
                new BigDecimal("480"), LocalDate.of(2026, 11, 3), "Bruno Reis", ""));
        model.expenses.add(new Expense(3, "Energia", "Energia", new BigDecimal("620"),
                LocalDate.of(2026, 11, 4), "Ana Souza", ""));
        model.expenses.add(new Expense(4, "Manutenção", "Manutenção", new BigDecimal("760"),
                LocalDate.of(2026, 11, 7), "Bruno Reis", ""));
        model.expenses.add(new Expense(5, "Internet", "Internet", new BigDecimal("200"),
                LocalDate.of(2026, 11, 12), "Ana Souza", ""));
        model.expenses.add(new Expense(6, "Limpeza", "Limpeza", new BigDecimal("500"),
                LocalDate.of(2026, 11, 15), "Bruno Reis", ""));
        model.expenses.add(new Expense(7, "Água", "Água", new BigDecimal("500"),
                LocalDate.of(2026, 11, 20), "Ana Souza", ""));
        return model;
    }

    public List<Installment> installmentsForInvoice(int invoiceId) {
        return installments.stream()
                .filter(item -> item.appointmentId == invoiceId)
                .sorted(Comparator.comparing((Installment item) -> item.due).thenComparingInt(item -> item.id))
                .toList();
    }

    public List<Payment> paymentsForInvoice(int invoiceId) {
        return payments.stream()
                .filter(payment -> payment.appointmentId == invoiceId)
                .sorted(Comparator.comparing((Payment payment) -> payment.date).thenComparingInt(payment -> payment.id))
                .toList();
    }

    /** Localiza a consulta vinculada à fatura, inclusive nos dados demonstrativos antigos. */
    public Appointment appointmentForInvoice(int invoiceId) {
        int appointmentId = LEGACY_INVOICE_APPOINTMENTS.getOrDefault(invoiceId, invoiceId);
        return appointments.stream()
                .filter(appointment -> appointment.id == appointmentId)
                .findFirst()
                .orElse(null);
    }

    public BigDecimal invoiceTotal(int invoiceId) {
        Appointment exactAppointment = appointments.stream()
                .filter(appointment -> appointment.id == invoiceId)
                .findFirst()
                .orElse(null);
        if (exactAppointment != null) return exactAppointment.value;

        BigDecimal scheduled = installmentsForInvoice(invoiceId).stream()
                .map(item -> item.value)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal entry = paymentsForInvoice(invoiceId).stream()
                .filter(payment -> !payment.reversed && "Entrada".equalsIgnoreCase(payment.label))
                .map(payment -> payment.value.add(payment.discount))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (scheduled.signum() > 0) return scheduled.add(entry);
        return paymentsForInvoice(invoiceId).stream()
                .filter(payment -> !payment.reversed)
                .map(payment -> payment.value.add(payment.discount))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal invoiceReceived(int invoiceId) {
        return paymentsForInvoice(invoiceId).stream()
                .filter(payment -> !payment.reversed)
                .map(payment -> payment.value)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal invoiceDiscounts(int invoiceId) {
        return paymentsForInvoice(invoiceId).stream()
                .filter(payment -> !payment.reversed)
                .map(payment -> payment.discount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal invoiceBalance(int invoiceId) {
        return invoiceTotal(invoiceId).subtract(invoiceReceived(invoiceId))
                .subtract(invoiceDiscounts(invoiceId)).max(BigDecimal.ZERO);
    }

    /**
     * Registra uma fatura somente depois de validar todo o conjunto. A operação
     * continua em memória nesta versão visual e não introduz banco ou backend.
     */
    public void recordInvoice(Appointment appointment, List<Payment> invoicePayments,
                              List<Installment> invoiceInstallments) {
        if (appointment == null || appointments.stream().noneMatch(item -> item.id == appointment.id))
            throw new IllegalArgumentException("Consulta não encontrada.");
        if (appointment.billed) throw new IllegalArgumentException("Esta consulta já foi faturada.");
        boolean invalidPayment = invoicePayments.stream().anyMatch(payment -> payment.appointmentId != appointment.id
                || payment.value.signum() <= 0 || payment.discount.signum() < 0 || payment.reversed);
        boolean invalidInstallment = invoiceInstallments.stream().anyMatch(installment -> installment.appointmentId != appointment.id
                || installment.value.signum() <= 0 || installment.paid.signum() < 0 || installment.cancelled);
        invalidInstallment = invalidInstallment || invoiceInstallments.stream()
                .anyMatch(installment -> installment.paid.compareTo(installment.value) > 0);
        if (invalidPayment || invalidInstallment) throw new IllegalArgumentException("Dados da fatura inválidos.");

        BigDecimal allocated = invoicePayments.stream()
                .map(payment -> payment.value.add(payment.discount)).reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(invoiceInstallments.stream().map(item -> item.value).reduce(BigDecimal.ZERO, BigDecimal::add));
        if (allocated.compareTo(appointment.value) != 0)
            throw new IllegalArgumentException("Pagamentos e parcelas devem totalizar o valor da consulta.");

        if (context != null) {
            br.com.livrocaixa.model.Cliente realClient = context.clientes.listar().stream()
                    .filter(item -> item.getNome().equals(appointment.client)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado."));
            br.com.livrocaixa.model.TipoConsulta realType = context.tiposConsulta.listar().stream()
                    .filter(item -> item.getDescricao().equals(appointment.type)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Tipo de consulta não encontrado."));
            List<Installment> regular = invoiceInstallments.stream()
                    .filter(item -> !"Entrada".equalsIgnoreCase(item.label)).toList();
            boolean cash = invoiceInstallments.isEmpty();
            BigDecimal entry = invoiceInstallments.stream()
                    .filter(item -> "Entrada".equalsIgnoreCase(item.label)).map(item -> item.value)
                    .findFirst().orElse(BigDecimal.ZERO);
            LocalDate firstDue = regular.isEmpty() ? null : regular.get(0).due;
            br.com.livrocaixa.model.Consulta real = context.consultas.prepararConsulta(
                    realClient, realType, appointment.date, appointment.notes, appointment.value,
                    cash, cash ? null : regular.size(), firstDue, entry);
            if (!cash) {
                for (int index = 0; index < regular.size(); index++) {
                    context.consultas.alterarValorParcela(real, index + 1, regular.get(index).value);
                }
            }
            context.consultas.salvarConsulta(real);
            appointment.id = real.getId();
            invoiceInstallments.clear();
            for (br.com.livrocaixa.model.Parcela parcela : real.getFatura().getParcelas()) {
                String label = parcela.getTipoParcela() != null && parcela.getTipoParcela().getId() == 3
                        ? "Entrada" : parcela.getPosicaoParcela() + "/" + parcela.getNumParcelas();
                Installment projection = new Installment(parcela.getId(), real.getId(), appointment.client,
                        label, parcela.getDataVencimento(), parcela.getValor(), parcela.getValorPago(), false);
                invoiceInstallments.add(projection);
                realInstallments.put(projection.id, parcela);
            }
            invoicePayments.clear();
        }
        payments.addAll(invoicePayments);
        installments.addAll(invoiceInstallments);
        appointment.billed = true;
        save();
    }

    public Client saveClient(Client client, String name, String cpf) {
        if (name == null || name.isBlank() || cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("Informe o nome e o CPF.");
        }
        if (context != null) {
            br.com.livrocaixa.model.Cliente real = client == null
                    ? new br.com.livrocaixa.model.Cliente()
                    : realClients.get(client.id);
            if (real == null) throw new IllegalArgumentException("Cliente não encontrado.");
            real.setNome(name.trim()); real.setCpf(cpf.trim()); context.clientes.salvar(real);
            if (client == null) {
                client = new Client(real.getId(), real.getNome(), real.getCpf(), "", "", "");
                clients.add(client); realClients.put(real.getId(), real);
            } else { client.name = real.getNome(); client.cpf = real.getCpf(); }
            return client;
        } else if (client == null) {
            client = new Client(nextClient++, name.trim(), cpf.trim(), "", "", ""); clients.add(client);
        } else {
            int clientId = client.id;
            if (clients.stream().noneMatch(item -> item.id == clientId)) {
                throw new IllegalArgumentException("Cliente não encontrado.");
            }
            client.name = name.trim();
            client.cpf = cpf.trim();
        }
        save();
        return client;
    }

    public void removeClient(Client client) {
        if (context != null) { context.clientes.excluir(client.id); realClients.remove(client.id); }
        clients.remove(client);
        save();
    }

    public String addAppointmentType(String description) {
        return saveCategory(appointmentTypes, -1, description);
    }

    public String addExpenseType(String description) {
        return saveCategory(expenseTypes, -1, description);
    }

    public String saveCategory(List<String> values, int index, String description) {
        String text = description == null ? "" : description.trim();
        if (text.isBlank()) throw new IllegalArgumentException("Informe a descrição.");
        if (context == null) {
            if (index >= 0) values.set(index, text); else values.add(text);
            return text;
        }
        String previous = index >= 0 ? values.get(index) : null;
        if (values == appointmentTypes) {
            br.com.livrocaixa.model.TipoConsulta item = previous == null
                    ? new br.com.livrocaixa.model.TipoConsulta()
                    : context.tiposConsulta.listar().stream().filter(v -> v.getDescricao().equals(previous)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Tipo de consulta não encontrado."));
            item.setDescricao(text); context.tiposConsulta.salvar(item);
        } else if (values == expenseTypes) {
            br.com.livrocaixa.model.TipoSaida item = previous == null
                    ? new br.com.livrocaixa.model.TipoSaida()
                    : context.tiposSaida.listar().stream().filter(v -> v.getDescricao().equals(previous)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Tipo de saída não encontrado."));
            item.setDescricao(text); context.tiposSaida.salvar(item);
        } else if (values == paymentMethods) {
            br.com.livrocaixa.model.FormaPagamento item = previous == null
                    ? new br.com.livrocaixa.model.FormaPagamento()
                    : context.formasPagamento.listar().stream().filter(v -> v.getDescricao().equals(previous)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Forma de pagamento não encontrada."));
            item.setDescricao(text); context.formasPagamento.salvar(item);
        }
        if (index >= 0) values.set(index, text); else values.add(text);
        return text;
    }

    public void deleteCategory(List<String> values, int index) {
        String description = values.get(index);
        if (context != null) {
            if (values == appointmentTypes) context.tiposConsulta.listar().stream()
                    .filter(v -> v.getDescricao().equals(description)).findFirst()
                    .ifPresent(v -> context.tiposConsulta.excluir(v.getId()));
            else if (values == expenseTypes) context.tiposSaida.listar().stream()
                    .filter(v -> v.getDescricao().equals(description)).findFirst()
                    .ifPresent(v -> context.tiposSaida.excluir(v.getId()));
            else if (values == paymentMethods) context.formasPagamento.listar().stream()
                    .filter(v -> v.getDescricao().equals(description)).findFirst()
                    .ifPresent(v -> context.formasPagamento.excluir(v.getId()));
        }
        values.remove(index);
    }

    public Appointment saveAppointment(Appointment appointment, String client, String type,
                                       LocalDate date, String employee, BigDecimal value, String notes) {
        if (client == null || client.isBlank() || type == null || type.isBlank()
                || date == null || employee == null || employee.isBlank()
                || value == null || value.signum() <= 0) {
            throw new IllegalArgumentException("Dados da consulta inválidos.");
        }
        if (appointment == null) {
            appointment = new Appointment(nextAppointment++, client, type, date, employee,
                    value, notes == null ? "" : notes, false);
            appointments.add(appointment);
        } else {
            int appointmentId = appointment.id;
            if (appointments.stream().noneMatch(item -> item.id == appointmentId)) {
                throw new IllegalArgumentException("Consulta não encontrada.");
            }
            if (appointment.billed) {
                throw new IllegalArgumentException("Uma consulta faturada não pode ser alterada.");
            }
            appointment.client = client;
            appointment.type = type;
            appointment.date = date;
            appointment.employee = employee;
            appointment.value = value;
            appointment.notes = notes == null ? "" : notes;
        }
        save();
        return appointment;
    }

    public Expense saveExpense(Expense expense, String type, String description, BigDecimal value,
                               LocalDate date, String employee, String notes) {
        if (type == null || type.isBlank() || value == null || value.signum() <= 0
                || date == null || employee == null || employee.isBlank()) {
            throw new IllegalArgumentException("Dados da saída inválidos.");
        }
        if (context != null) {
            if (expense != null) throw new IllegalArgumentException("A edição de saídas depende do método de atualização no Controller.");
            br.com.livrocaixa.model.TipoSaida realType = context.tiposSaida.listar().stream()
                    .filter(item -> item.getDescricao().equals(type)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Tipo de saída não encontrado."));
            br.com.livrocaixa.model.Saida real = context.saidas.registrarSaida(date, value, realType, notes);
            expense = new Expense(real.getId(), type, description, value, date,
                    real.getFuncionario().getNome(), notes == null ? "" : notes);
            expenses.add(expense);
        } else if (expense == null) {
            expense = new Expense(nextExpense++, type, description, value, date, employee,
                    notes == null ? "" : notes);
            expenses.add(expense);
        } else {
            int expenseId = expense.id;
            if (expenses.stream().noneMatch(item -> item.id == expenseId)) {
                throw new IllegalArgumentException("Saída não encontrada.");
            }
            expense.type = type;
            expense.description = description;
            expense.value = value;
            expense.date = date;
            expense.employee = employee;
            expense.notes = notes == null ? "" : notes;
        }
        save();
        return expense;
    }

    public void removeExpense(Expense expense) {
        if (context != null) throw new IllegalArgumentException("A exclusão de saídas ainda não existe no Controller do projeto.");
        expenses.remove(expense);
        save();
    }

    public Payment registerInstallmentPayment(Installment installment, LocalDate date,
                                              BigDecimal value, String method, BigDecimal discount) {
        if (installment == null || installments.stream().noneMatch(item -> item.id == installment.id)) {
            throw new IllegalArgumentException("Parcela não encontrada.");
        }
        if (installment.cancelled || installment.isPaid() || date == null
                || value == null || value.signum() <= 0 || method == null || method.isBlank()
                || discount == null || discount.signum() < 0
                || value.add(discount).compareTo(installment.remaining()) > 0) {
            throw new IllegalArgumentException("Pagamento inválido.");
        }
        if (context != null && discount.signum() > 0)
            throw new IllegalArgumentException("Descontos não existem no Model de pagamento do projeto.");
        br.com.livrocaixa.model.Pagamento realPayment = null;
        if (context != null) {
            br.com.livrocaixa.model.Parcela realInstallment = realInstallments.get(installment.id);
            br.com.livrocaixa.model.FormaPagamento realMethod = context.formasPagamento.listar().stream()
                    .filter(item -> item.getDescricao().equals(method)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Forma de pagamento não encontrada."));
            if (realInstallment == null) throw new IllegalArgumentException("Parcela não encontrada no Controller.");
            realPayment = context.parcelamentos.baixarParcela(realInstallment.getId(), value, realMethod, date);
            installment.paid = realInstallment.getValorPago();
        } else installment.paid = installment.paid.add(value).add(discount);
        Payment payment = new Payment(realPayment == null ? nextPayment++ : realPayment.getId(), installment.appointmentId, installment.client,
                installment.label, date, value, method, discount, false);
        payments.add(payment);
        if (realPayment != null) realPayments.put(payment.id, realPayment);
        save();
        return payment;
    }

    public void cancelInstallment(Installment installment) {
        if (installment == null || installments.stream().noneMatch(item -> item.id == installment.id)) {
            throw new IllegalArgumentException("Parcela não encontrada.");
        }
        if (installment.isPaid()) {
            throw new IllegalArgumentException("Uma parcela quitada não pode ser cancelada.");
        }
        if (context != null) context.parcelamentos.cancelarParcela(installment.id);
        installment.cancelled = true;
        save();
    }

    public void reversePayment(Installment installment, Payment payment) {
        if (installment == null || payment == null || payment.reversed
                || installments.stream().noneMatch(item -> item.id == installment.id)
                || payments.stream().noneMatch(item -> item.id == payment.id)
                || payment.appointmentId != installment.appointmentId
                || !payment.label.equals(installment.label)) {
            throw new IllegalArgumentException("Pagamento não pertence a esta parcela.");
        }
        if (context != null) context.parcelamentos.desfazerBaixa(installment.id, payment.id);
        payment.reversed = true;
        installment.paid = installment.paid
                .subtract(payment.value.add(payment.discount))
                .max(BigDecimal.ZERO);
        save();
    }

    public BigDecimal income() {
        return payments.stream().filter(payment -> !payment.reversed)
                .map(payment -> payment.value).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal expenseTotal() {
        return expenses.stream().map(expense -> expense.value)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal balance() {
        return income().subtract(expenseTotal());
    }

    public BigDecimal pending() {
        return installments.stream().filter(item -> !item.cancelled)
                .map(Installment::remaining)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Parcelas abertas usadas pelo quadro de próximos vencimentos. */
    public List<Installment> upcomingInstallments() {
        return installments.stream()
                .filter(item -> !item.cancelled && !item.isPaid())
                .sorted(Comparator.comparing((Installment item) -> item.due).thenComparingInt(item -> item.id))
                .toList();
    }
}
