package br.com.livrocaixa.view;

import br.com.livrocaixa.view.dados.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntPredicate;

/** Telas de relatórios e configurações reproduzidas a partir dos protótipos 13–16. */
final class TelasGerenciamento {
    private TelasGerenciamento() {}

    private static final List<EmployeeRow> EMPLOYEES = new ArrayList<>(List.of(
            new EmployeeRow(1, "Ana Souza", "Administrador"),
            new EmployeeRow(2, "Bruno Reis", "Atendente")
    ));

    static JComponent buildReports(TelaLivroCaixa.JanelaPrincipal frame) {
        JPanel root = TelaLivroCaixa.page();
        root.setBorder(new EmptyBorder(12, 14, 14, 14));

        CardLayout cardsLayout = new CardLayout();
        JPanel cards = new JPanel(cardsLayout);
        cards.setOpaque(false);
        cards.add(scrollableReport(overview(frame)), "Visão geral");
        cards.add(scrollableReport(expenses(frame)), "Saídas");
        cards.add(scrollableReport(appointments(frame)), "Consultas");
        cards.add(scrollableReport(receipts(frame)), "Recebimentos");

        JPanel north = new JPanel(new BorderLayout(0, 10));
        north.setOpaque(false);
        JPanel tabBar = new JPanel(new GridLayout(1, 4, 10, 0));
        tabBar.setOpaque(false);
        Map<String, ReportTabButton> buttons = new LinkedHashMap<>();
        addReportTab(tabBar, buttons, "Visão geral", "chart");
        addReportTab(tabBar, buttons, "Saídas", "swap");
        addReportTab(tabBar, buttons, "Consultas", "calendar");
        addReportTab(tabBar, buttons, "Recebimentos", "coins");
        buttons.forEach((name, button) -> button.addActionListener(e -> {
            cardsLayout.show(cards, name);
            buttons.forEach((n, b) -> b.setSelected(n.equals(name)));
        }));
        buttons.get("Visão geral").setSelected(true);
        north.add(tabBar, BorderLayout.NORTH);
        north.add(reportFilters(frame), BorderLayout.CENTER);
        root.add(north, BorderLayout.NORTH);
        root.add(cards);
        return root;
    }

    /** Mantém os relatórios largos como a janela e permite percorrer gráfico e tabela verticalmente. */
    private static JComponent scrollableReport(JComponent report) {
        JPanel canvas = new JPanel(new BorderLayout());
        canvas.setOpaque(false);
        canvas.setBorder(new EmptyBorder(0, 0, 8, 8));
        canvas.add(report, BorderLayout.CENTER);
        canvas.setPreferredSize(new Dimension(1, 540));

        JScrollPane scroll = new JScrollPane(
                canvas,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        scroll.getVerticalScrollBar().setBlockIncrement(180);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(14, 0));
        return scroll;
    }

    private static void addReportTab(JPanel bar, Map<String, ReportTabButton> map, String text, String icon) {
        ReportTabButton button = new ReportTabButton(text, ComponentesVisuais.vectorIcon(icon, 29, ComponentesVisuais.INK));
        map.put(text, button);
        bar.add(button);
    }

    private static JComponent reportFilters(TelaLivroCaixa.JanelaPrincipal frame) {
        ComponentesVisuais.RoundPanel box = new ComponentesVisuais.RoundPanel(14).fill(Color.WHITE).shadow(false);
        box.setLayout(new GridBagLayout());
        box.setBorder(new EmptyBorder(10, 12, 10, 12));
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.insets = new Insets(0, 0, 0, 12);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;
        g.weightx = .18;
        box.add(fieldBlock("Data inicial", TelaLivroCaixa.modernField("01/11/2026")), g);
        g.gridx = 1;
        box.add(fieldBlock("Data final", TelaLivroCaixa.modernField("30/11/2026")), g);
        JComboBox<String> employee = new JComboBox<>(new String[]{"Todos", "Ana Souza", "Bruno Reis"});
        g.gridx = 2;
        g.weightx = .20;
        box.add(fieldBlock("Funcionário", employee), g);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 18));
        actions.setOpaque(false);
        JButton apply = ComponentesVisuais.primary("Aplicar filtros", "search");
        JButton clear = ComponentesVisuais.secondary("Limpar", "refresh");
        JButton print = ComponentesVisuais.secondary("Imprimir", "print");
        apply.addActionListener(e -> TelaLivroCaixa.info(frame, "Filtros aplicados ao período informado."));
        clear.addActionListener(e -> employee.setSelectedIndex(0));
        print.addActionListener(e -> TelaLivroCaixa.info(frame, "Relatório preparado para impressão."));
        actions.add(apply);
        actions.add(clear);
        actions.add(print);
        g.gridx = 3;
        g.weightx = .44;
        g.insets = new Insets(0, 0, 0, 0);
        box.add(actions, g);
        return box;
    }

    private static JComponent overview(TelaLivroCaixa.JanelaPrincipal frame) {
        JPanel page = new JPanel(new BorderLayout(0, 10));
        page.setOpaque(false);
        JPanel stats = statsRow(
                stat("Entradas", TelaLivroCaixa.money(frame.store.income()),
                        new Color(223, 255, 222), new Color(82, 210, 84)),
                stat("Saídas", TelaLivroCaixa.money(frame.store.expenseTotal()),
                        new Color(255, 229, 231), new Color(244, 130, 137)),
                stat("Saldo", TelaLivroCaixa.money(frame.store.balance()),
                        new Color(229, 239, 255), new Color(114, 160, 245)),
                stat("A receber", TelaLivroCaixa.money(frame.store.pending()),
                        new Color(255, 247, 207), new Color(242, 190, 36))
        );
        stats.setPreferredSize(new Dimension(0, 92));
        page.add(stats, BorderLayout.NORTH);

        ComponentesVisuais.RoundPanel chartCard = panelCard(
                "Entradas × Saídas",
                "Comparação dos valores movimentados no período selecionado");
        chartCard.add(new TwoBarChart(
                frame.store.income().doubleValue(), frame.store.expenseTotal().doubleValue()));
        ComponentesVisuais.RoundPanel summaryCard = panelCard("Resumo por sessão");
        JPanel summaries = new JPanel(new GridLayout(3, 1, 0, 8));
        summaries.setOpaque(false);
        summaries.add(sessionCard("Saídas", "7 registros", "R$ 4.260,00", ComponentesVisuais.RED, "swap"));
        summaries.add(sessionCard("Consultas", "18 realizadas", "16 clientes atendidos", ComponentesVisuais.BLUE, "calendar"));
        summaries.add(sessionCard("Recebimentos", "22 pagamentos", "R$ 12.680,00", ComponentesVisuais.GREEN, "coins"));
        summaryCard.add(summaries);

        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{"05/11/2026", "Entrada", "Parcela 1/4 - Carlos Mendes",
                "Ana Souza", "Cartão de crédito", "R$ 350,00"});
        rows.add(new Object[]{"05/11/2026", "Saída", "Aluguel", "Ana Souza", "—", "R$ 1.200,00"});
        rows.add(new Object[]{"04/11/2026", "Saída", "Energia", "Ana Souza", "—", "R$ 620,00"});
        ComponentesVisuais.RoundPanel table = tableCard(
                "Últimas movimentações",
                new String[]{"Data", "Movimento", "Descrição", "Funcionário", "Forma de pagamento", "Valor"},
                rows.toArray(Object[][]::new),
                "Exibindo 3 de 27 movimentações");
        page.add(reportWorkspace(chartCard, summaryCard, table, .30, .19, .51));
        return page;
    }

    private static JComponent expenses(TelaLivroCaixa.JanelaPrincipal frame) {
        JPanel page = new JPanel(new BorderLayout(0, 10));
        page.setOpaque(false);
        BigDecimal average = frame.store.expenses.isEmpty()
                ? BigDecimal.ZERO
                : frame.store.expenseTotal().divide(
                        BigDecimal.valueOf(frame.store.expenses.size()), 2, RoundingMode.HALF_UP);
        JPanel stats = statsRow(
                stat("Total de saídas", TelaLivroCaixa.money(frame.store.expenseTotal()),
                        new Color(255, 229, 231), new Color(244, 130, 137)),
                stat("Saídas realizadas", String.valueOf(frame.store.expenses.size()),
                        new Color(229, 239, 255), new Color(114, 160, 245)),
                stat("Média por saída", TelaLivroCaixa.money(average),
                        new Color(224, 255, 222), new Color(86, 210, 87)),
                stat("Maior saída", "Aluguel - R$ 1.200,00",
                        new Color(255, 247, 207), new Color(242, 190, 36))
        );
        stats.setPreferredSize(new Dimension(0, 92));
        page.add(stats, BorderLayout.NORTH);
        String[] labels = {
                "Aluguel", "Manutenção", "Energia", "Limpeza", "Água", "Material de escritório", "Internet"
        };
        double[] values = {1200, 760, 620, 500, 500, 480, 200};
        ComponentesVisuais.RoundPanel chart = panelCard(
                "Saídas por tipo",
                "Total gasto em cada categoria — do maior para o menor");
        chart.add(new HorizontalBarChart(labels, values, true, ComponentesVisuais.RED));
        ComponentesVisuais.RoundPanel summary = summaryColumn(
                "Resumo das saídas",
                sessionCard("Maior saída", "Aluguel", "R$ 1.200,00", ComponentesVisuais.RED, "swap"),
                sessionCard("Quantidade", "No período", "7 registros", ComponentesVisuais.BLUE, "document"),
                sessionCard("Média por saída", "Valor médio", TelaLivroCaixa.money(average), ComponentesVisuais.GREEN, "chart")
        );
        Object[][] rows = frame.store.expenses.stream()
                .map(x -> new Object[]{
                        TelaLivroCaixa.fmt(x.date), x.type, x.employee, TelaLivroCaixa.money(x.value)})
                .toArray(Object[][]::new);
        ComponentesVisuais.RoundPanel table = tableCard("Detalhamento das saídas", new String[]{"Data", "Tipo de saída", "Funcionário", "Valor"}, rows, "");
        page.add(reportWorkspace(chart, summary, table, .42, .19, .39));
        return page;
    }

    private static JComponent appointments(TelaLivroCaixa.JanelaPrincipal frame) {
        JPanel page = new JPanel(new BorderLayout(0, 10));
        page.setOpaque(false);
        JPanel stats = statsRow(
                stat("Consultas realizadas", "18", new Color(255, 229, 231), new Color(244, 130, 137)),
                stat("Clientes atendidos", "16", new Color(229, 239, 255), new Color(114, 160, 245)),
                stat("Tipo mais realizado", "Consulta de rotina", new Color(224, 255, 222), new Color(86, 210, 87)),
                stat("Mais consultas", "Ana Souza", new Color(255, 247, 207), new Color(242, 190, 36))
        );
        stats.setPreferredSize(new Dimension(0, 92));
        page.add(stats, BorderLayout.NORTH);
        ComponentesVisuais.RoundPanel chart = panelCard(
                "Consultas por tipo",
                "Quantidade de atendimentos agrupada pelo tipo de consulta");
        chart.add(new HorizontalBarChart(
                new String[]{"Consulta de rotina", "Retorno", "Avaliação"},
                new double[]{9, 6, 3}, false, ComponentesVisuais.BLUE));
        ComponentesVisuais.RoundPanel summary = summaryColumn(
                "Resumo das consultas",
                sessionCard("Mais realizado", "Tipo de consulta", "Consulta de rotina", ComponentesVisuais.BLUE, "calendar"),
                sessionCard("Atendimentos", "No período", "18 consultas", ComponentesVisuais.GREEN, "document"),
                sessionCard("Clientes", "Pessoas atendidas", "16 clientes", new Color(196, 126, 0), "group")
        );
        Object[][] rows = frame.store.appointments.stream()
                .map(x -> new Object[]{TelaLivroCaixa.fmt(x.date), x.client, x.type, x.employee})
                .toArray(Object[][]::new);
        ComponentesVisuais.RoundPanel table = tableCard(
                "Detalhamento das consultas", new String[]{"Data", "Cliente", "Tipo", "Funcionário"}, rows,
                "Exibindo " + rows.length + " de 18 consultas");
        page.add(reportWorkspace(chart, summary, table, .42, .19, .39));
        return page;
    }

    private static JComponent receipts(TelaLivroCaixa.JanelaPrincipal frame) {
        JPanel page = new JPanel(new BorderLayout(0, 10));
        page.setOpaque(false);
        JPanel stats = statsRow(
                stat("Total recebido", "R$ 12.680,00", new Color(255, 229, 231), new Color(244, 130, 137)),
                stat("À vista", "R$ 7.430,00", new Color(229, 239, 255), new Color(114, 160, 245)),
                stat("À prazo", "R$ 5.250,00", new Color(224, 255, 222), new Color(86, 210, 87)),
                stat("Pagamentos registrados", "22", new Color(255, 247, 207), new Color(242, 190, 36))
        );
        stats.setPreferredSize(new Dimension(0, 92));
        page.add(stats, BorderLayout.NORTH);
        ComponentesVisuais.RoundPanel chart = panelCard(
                "Recebimentos por forma de pagamento",
                "Total recebido em cada meio de pagamento no período");
        chart.add(new HorizontalBarChart(
                new String[]{"Cartão de crédito", "PIX", "Dinheiro", "Cartão de débito"},
                new double[]{4200, 3980, 2500, 2000}, true, ComponentesVisuais.GREEN));
        ComponentesVisuais.RoundPanel summary = summaryColumn(
                "Resumo dos recebimentos",
                sessionCard("Maior forma", "Cartão de crédito", "R$ 4.200,00", ComponentesVisuais.GREEN, "wallet"),
                sessionCard("À vista", "Total recebido", "R$ 7.430,00", ComponentesVisuais.BLUE, "coins"),
                sessionCard("À prazo", "Total recebido", "R$ 5.250,00", new Color(196, 126, 0), "calendar")
        );
        Object[][] rows = {
                {"05/11/2026", "Carlos Mendes", "Parcela 1/4", "À prazo", "Cartão de crédito", "R$ 350,00"},
                {"05/11/2026", "Mariana Silva", "Consulta 104", "À vista", "PIX", "R$ 420,00"},
                {"04/11/2026", "Fernanda Costa", "Parcela 1/2", "À prazo", "Dinheiro", "R$ 600,00"}
        };
        ComponentesVisuais.RoundPanel table = tableCard(
                "Detalhamento dos recebimentos", new String[]{"Data", "Cliente", "Origem", "Tipo", "Forma de pagamento", "Valor"},
                rows, "Exibindo 3 de 22 pagamentos");
        page.add(reportWorkspace(chart, summary, table, .42, .19, .39));
        return page;
    }

    /** Organiza cada relatório em gráfico, resumo compacto e detalhamento lado a lado. */
    private static JComponent reportWorkspace(JComponent chart, JComponent summary, JComponent table,
                                              double chartWeight, double summaryWeight, double tableWeight) {
        JPanel workspace = new JPanel(new GridBagLayout());
        workspace.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.weighty = 1;
        g.fill = GridBagConstraints.BOTH;

        g.gridx = 0;
        g.weightx = chartWeight;
        g.insets = new Insets(0, 0, 0, 10);
        workspace.add(chart, g);

        g.gridx = 1;
        g.weightx = summaryWeight;
        workspace.add(summary, g);

        g.gridx = 2;
        g.weightx = tableWeight;
        g.insets = new Insets(0, 0, 0, 0);
        workspace.add(table, g);
        return workspace;
    }

    private static ComponentesVisuais.RoundPanel summaryColumn(String title, JComponent... cards) {
        ComponentesVisuais.RoundPanel summary = panelCard(title);
        JPanel content = new JPanel(new GridLayout(cards.length, 1, 0, 8));
        content.setOpaque(false);
        for (JComponent card : cards) content.add(card);
        summary.add(content);
        return summary;
    }

    static JComponent buildSettings(TelaLivroCaixa.JanelaPrincipal frame) {
        JPanel root = TelaLivroCaixa.page();
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        ComponentesVisuais.RoundPanel body = new ComponentesVisuais.RoundPanel(17).fill(new Color(249, 251, 255));
        body.setLayout(new BorderLayout(0, 22));
        body.setBorder(new EmptyBorder(22, 22, 22, 22));
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(TelaLivroCaixa.title("Configurações do sistema", 34));
        JLabel sub = new JLabel("Gerencie usuários e cadastros auxiliares");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        heading.add(Box.createVerticalStrut(4));
        heading.add(sub);
        body.add(heading, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 5, 12, 0));
        cards.setOpaque(false);
        cards.add(settingsCard(frame, "Funcionários", "2 cadastrados", "group",
                new Color(227, 255, 225), new Color(95, 212, 98), 0));
        cards.add(settingsCard(frame, "Perfis", "2 cadastrados", "shield",
                new Color(255, 230, 231), new Color(239, 128, 132), 1));
        cards.add(settingsCard(frame, "Tipos de consulta", frame.store.appointmentTypes.size() + " cadastrados",
                "calendar", new Color(231, 239, 255), new Color(115, 154, 238), 2));
        cards.add(settingsCard(frame, "Tipos de saída", frame.store.expenseTypes.size() + " cadastrados",
                "logout", new Color(255, 247, 210), new Color(235, 189, 49), 3));
        cards.add(settingsCard(frame, "Formas de pagamento", frame.store.paymentMethods.size() + " cadastradas",
                "card", new Color(239, 232, 255), new Color(162, 125, 231), 4));
        cards.setMinimumSize(new Dimension(0, 315));
        cards.setPreferredSize(new Dimension(0, 390));
        JPanel cardsArea = new JPanel(new GridBagLayout());
        cardsArea.setOpaque(false);
        GridBagConstraints cardsPosition = new GridBagConstraints();
        cardsPosition.gridx = 0;
        cardsPosition.gridy = 0;
        cardsPosition.weightx = 1;
        cardsPosition.weighty = 1;
        cardsPosition.fill = GridBagConstraints.HORIZONTAL;
        cardsPosition.anchor = GridBagConstraints.FIRST_LINE_START;
        cardsArea.add(cards, cardsPosition);
        body.add(cardsArea);

        ComponentesVisuais.RoundPanel info = new ComponentesVisuais.RoundPanel(13).fill(new Color(231, 240, 255)).shadow(false);
        info.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 12));
        JLabel infoLabel = new JLabel("Cadastros em uso não podem ser excluídos.",
                ComponentesVisuais.vectorIcon("info", 28, ComponentesVisuais.BLUE), SwingConstants.LEFT);
        infoLabel.setIconTextGap(16);
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        info.add(infoLabel);
        body.add(info, BorderLayout.SOUTH);
        root.add(body);
        return root;
    }

    private static JComponent settingsCard(TelaLivroCaixa.JanelaPrincipal frame, String name, String count,
                                           String icon, Color fill, Color line, int route) {
        ComponentesVisuais.RoundPanel card = new ComponentesVisuais.RoundPanel(18).fill(fill).line(line).shadow(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 14, 18, 14));
        JLabel glyph = new JLabel(ComponentesVisuais.vectorIcon(icon, 52, new Color(23, 46, 176)));
        glyph.setAlignmentX(.5f);
        String displayName = switch (name) {
            case "Tipos de consulta" -> "Tipos de<br>consulta";
            case "Tipos de saída" -> "Tipos de<br>saída";
            case "Formas de pagamento" -> "Formas de<br>pagamento";
            default -> name;
        };
        JLabel title = TelaLivroCaixa.title(
                "<html><center>" + displayName + "</center></html>", 20);
        title.setAlignmentX(.5f);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setMinimumSize(new Dimension(0, 52));
        title.setPreferredSize(new Dimension(180, 52));
        title.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        JLabel amount = new JLabel(count);
        amount.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        amount.setAlignmentX(.5f);
        JButton manage = ComponentesVisuais.primary("Gerenciar", "gear");
        manage.setAlignmentX(.5f);
        manage.setFont(new Font("Segoe UI", Font.BOLD, 15));
        manage.setIconTextGap(6);
        manage.setBorder(new EmptyBorder(10, 6, 10, 6));
        manage.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        manage.setPreferredSize(new Dimension(0, 48));
        manage.addActionListener(e -> {
            if (route <= 1) {
                frame.showCustom("Configurações", employeesAndProfiles(frame, route));
            } else {
                frame.showCustom("Configurações", auxiliary(frame, route - 2));
            }
        });
        card.add(glyph);
        card.add(Box.createVerticalStrut(14));
        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(amount);
        card.add(Box.createVerticalGlue());
        card.add(manage);
        return card;
    }

    private static JComponent employeesAndProfiles(TelaLivroCaixa.JanelaPrincipal frame, int selectedTab) {
        JPanel root = TelaLivroCaixa.page();
        ComponentesVisuais.RoundPanel body = new ComponentesVisuais.RoundPanel(17).fill(new Color(249, 251, 255));
        body.setLayout(new BorderLayout(20, 14));
        body.setBorder(new EmptyBorder(18, 20, 18, 20));
        body.add(TelaLivroCaixa.title("Funcionários e perfis", 32), BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(1, 3, 20, 0));
        content.setOpaque(false);
        JPanel left = new JPanel(new BorderLayout(0, 12));
        left.setOpaque(false);
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tabs.addTab("Funcionários", employeeList(frame));
        tabs.addTab("Perfis", profileList());
        tabs.setSelectedIndex(Math.min(1, selectedTab));
        left.add(tabs);
        content.add(left);
        content.add(employeeForm(frame));
        content.add(profileSummary(frame));
        body.add(content);

        JButton back = ComponentesVisuais.secondary("Voltar", "back");
        back.addActionListener(e -> frame.showPage("Configurações"));
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setOpaque(false);
        south.add(back);
        body.add(south, BorderLayout.SOUTH);
        root.add(body);
        return root;
    }

    private static JComponent employeeList(TelaLivroCaixa.JanelaPrincipal frame) {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);
        JPanel search = new JPanel(new BorderLayout(8, 0));
        search.setOpaque(false);
        JTextField field = TelaLivroCaixa.modernField("");
        search.add(field);
        search.add(ComponentesVisuais.primary("Buscar", "search"), BorderLayout.EAST);
        p.add(search, BorderLayout.NORTH);

        DefaultTableModel model = employeeModel();
        JTable table = ComponentesVisuais.modernTable(model);
        installActions(table, row -> false, row -> {}, row -> {
            if (TelaLivroCaixa.confirm(frame,
                    "Excluir o funcionário \"" + EMPLOYEES.get(row).name() + "\"?")) {
                EMPLOYEES.remove(row);
                model.setDataVector(employeeRows(), new String[]{"Código", "Nome", "Perfil", "Ações"});
            }
        });
        p.add(new JScrollPane(table));
        return p;
    }

    private static JComponent profileList() {
        return tableCard("Perfis cadastrados", new String[]{"Código", "Perfil"},
                new Object[][]{{"001", "Administrador"}, {"002", "Atendente"}}, "");
    }

    private static JComponent employeeForm(TelaLivroCaixa.JanelaPrincipal frame) {
        ComponentesVisuais.RoundPanel p = panelCard("Novo funcionário");
        p.setBorder(new EmptyBorder(14, 16, 14, 16));
        JPanel fields = new JPanel();
        fields.setOpaque(false);
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        JTextField name = TelaLivroCaixa.modernField("Mariana Alves");
        JComboBox<String> profile = new JComboBox<>(new String[]{"Administrador", "Atendente"});
        JPasswordField password = TelaLivroCaixa.modernPassword("12345678");
        fields.add(fieldBlock("Nome", name));
        fields.add(Box.createVerticalStrut(9));
        fields.add(fieldBlock("Perfil", profile));
        fields.add(Box.createVerticalStrut(9));
        fields.add(fieldBlock("Senha", password));
        fields.add(Box.createVerticalGlue());
        p.add(fields);

        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setOpaque(false);
        JButton cancel = ComponentesVisuais.secondary("Cancelar", "close");
        JButton save = ComponentesVisuais.primary("Salvar", "save");
        for (JButton button : new JButton[]{cancel, save}) {
            button.setFont(new Font("Segoe UI", Font.BOLD, 15));
            button.setIconTextGap(6);
            button.setBorder(new EmptyBorder(10, 8, 10, 8));
        }
        save.addActionListener(e -> {
            if (!name.getText().isBlank()) {
                EMPLOYEES.add(new EmployeeRow(EMPLOYEES.size() + 1, name.getText().trim(),
                        String.valueOf(profile.getSelectedItem())));
                TelaLivroCaixa.info(frame, "Funcionário salvo com sucesso.");
                frame.showCustom("Configurações", employeesAndProfiles(frame, 0));
            }
        });
        actions.add(cancel);
        actions.add(save);
        p.add(actions, BorderLayout.SOUTH);
        return p;
    }

    private static JComponent profileSummary(TelaLivroCaixa.JanelaPrincipal frame) {
        ComponentesVisuais.RoundPanel p = panelCard("Perfis cadastrados");
        JTable table = ComponentesVisuais.modernTable(ComponentesVisuais.readOnlyModel(
                new Object[][]{{"001", "Administrador"}, {"002", "Atendente"}},
                "Código", "Perfil"));
        table.setRowHeight(32);
        p.add(new JScrollPane(table));
        JButton manage = ComponentesVisuais.primary("Gerenciar perfis", "gear");
        manage.addActionListener(e -> frame.showCustom(
                "Configurações", employeesAndProfiles(frame, 1)));
        p.add(manage, BorderLayout.SOUTH);
        return p;
    }

    static JComponent auxiliary(TelaLivroCaixa.JanelaPrincipal frame, int selected) {
        JPanel root = TelaLivroCaixa.page();
        ComponentesVisuais.RoundPanel body = new ComponentesVisuais.RoundPanel(17).fill(new Color(249, 251, 255));
        body.setLayout(new BorderLayout(0, 14));
        body.setBorder(new EmptyBorder(14, 16, 14, 16));
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(TelaLivroCaixa.title("Cadastros auxiliares", 32));
        heading.add(new JLabel("Escolha uma categoria para consultar, cadastrar ou editar."));
        JPanel headingBar = new JPanel(new BorderLayout());
        headingBar.setOpaque(false);
        headingBar.add(heading);
        JButton back = ComponentesVisuais.secondary(
                frame.administrator ? "Voltar para configurações" : "Voltar para saídas", "back");
        back.setPreferredSize(new Dimension(285, 48));
        back.addActionListener(e -> frame.showPage(frame.administrator ? "Configurações" : "Saídas"));
        headingBar.add(back, BorderLayout.EAST);
        body.add(headingBar, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 18));
        if (frame.administrator) {
            tabs.addTab("Tipos de consulta", categoryEditor(
                    frame,
                    frame.store.appointmentTypes,
                    "tipo de consulta",
                    "Estas opções aparecem no cadastro de consultas.",
                    v -> frame.store.appointments.stream().anyMatch(a -> a.type.equals(v))));
        }
        tabs.addTab("Tipos de saída", categoryEditor(
                frame,
                frame.store.expenseTypes,
                "tipo de saída",
                "Estas opções aparecem no cadastro de saídas.",
                v -> frame.store.expenses.stream().anyMatch(x -> x.type.equals(v))));
        if (frame.administrator) {
            tabs.addTab("Formas de pagamento", categoryEditor(
                    frame,
                    frame.store.paymentMethods,
                    "forma de pagamento",
                    "Estas opções aparecem no registro de pagamentos.",
                    v -> frame.store.payments.stream().anyMatch(x -> x.method.equals(v))));
            tabs.setSelectedIndex(Math.max(0, Math.min(selected, 2)));
        }
        body.add(tabs);

        ComponentesVisuais.RoundPanel tips = new ComponentesVisuais.RoundPanel(12).fill(new Color(231, 240, 255)).shadow(false);
        tips.setLayout(new GridLayout(1, 4, 10, 0));
        tips.setBorder(new EmptyBorder(10, 14, 10, 14));
        tips.add(flowTip("Registros em uso são protegidos", "shield", ComponentesVisuais.RED));
        tips.add(flowTip("Escolha uma categoria", "card", new Color(111, 76, 205)));
        tips.add(flowTip("Clique em Novo ou Editar", "edit", ComponentesVisuais.BLUE));
        tips.add(flowTip("Salve as alterações", "save", ComponentesVisuais.GREEN));
        body.add(tips, BorderLayout.SOUTH);
        root.add(body);
        return root;
    }

    private static JComponent categoryEditor(TelaLivroCaixa.JanelaPrincipal frame, List<String> values,
                                             String singular, String hint,
                                             java.util.function.Predicate<String> used) {
        JPanel content = new JPanel(new GridLayout(1, 2, 18, 0));
        content.setBorder(new EmptyBorder(14, 8, 14, 8));
        content.setOpaque(false);

        ComponentesVisuais.RoundPanel left = panelCard(capitalize(singular) + " cadastrados");
        JLabel helper = new JLabel(
                hint, ComponentesVisuais.vectorIcon("info", 22, ComponentesVisuais.BLUE), SwingConstants.LEFT);
        helper.setForeground(new Color(23, 54, 178));
        left.add(helper, BorderLayout.NORTH);
        DefaultTableModel model = listModel(values);
        JTable table = ComponentesVisuais.modernTable(model);
        JPanel tableHolder = new JPanel(new BorderLayout(0, 10));
        tableHolder.setOpaque(false);
        JButton add = ComponentesVisuais.primary("Novo " + singular, "plus");
        tableHolder.add(add, BorderLayout.NORTH);
        tableHolder.add(new JScrollPane(table));
        left.add(tableHolder);

        ComponentesVisuais.RoundPanel right = panelCard("Novo " + singular);
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        JTextField description = TelaLivroCaixa.modernField("");
        form.add(fieldBlock("Descrição", description));
        right.add(form);
        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setOpaque(false);
        JButton cancel = ComponentesVisuais.secondary("Cancelar", "close");
        JButton save = ComponentesVisuais.primary("Salvar", "save");
        actions.add(cancel);
        actions.add(save);
        right.add(actions, BorderLayout.SOUTH);

        final int[] editing = {-1};
        add.addActionListener(e -> {
            editing[0] = -1;
            description.setText("");
            description.requestFocusInWindow();
        });
        cancel.addActionListener(e -> {
            editing[0] = -1;
            description.setText("");
        });
        save.addActionListener(e -> {
            String text = description.getText().trim();
            if (text.isBlank()) return;
            if (editing[0] >= 0) {
                frame.store.saveCategory(values, editing[0], text);
            } else {
                frame.store.saveCategory(values, -1, text);
            }
            model.setDataVector(listRows(values), new String[]{"Código", "Descrição", "Ações"});
            editing[0] = -1;
            description.setText("");
        });
        installActions(
                table,
                row -> used.test(values.get(row)),
                row -> {
                    editing[0] = row;
                    description.setText(values.get(row));
                },
                row -> {
                    if (used.test(values.get(row))) {
                        TelaLivroCaixa.error(frame, "Este cadastro está em uso e não pode ser excluído.");
                        return;
                    }
                    if (TelaLivroCaixa.confirm(frame,
                            "Excluir o cadastro \"" + values.get(row) + "\"?")) {
                        frame.store.deleteCategory(values, row);
                        model.setDataVector(
                                listRows(values), new String[]{"Código", "Descrição", "Ações"});
                    }
                });
        content.add(left);
        content.add(right);
        return content;
    }

    private static JLabel flowTip(String text, String icon, Color color) {
        JLabel tip = new JLabel(text, ComponentesVisuais.vectorIcon(icon, 22, color), SwingConstants.LEFT);
        tip.setIconTextGap(9);
        tip.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tip.setForeground(ComponentesVisuais.INK);
        return tip;
    }

    private static void installActions(JTable table, IntPredicate locked,
                                       java.util.function.IntConsumer edit,
                                       java.util.function.IntConsumer delete) {
        int col = table.getColumnCount() - 1;
        table.getColumnModel().getColumn(col).setCellRenderer(new ActionRenderer(locked));
        table.getColumnModel().getColumn(col).setMinWidth(150);
        table.getColumnModel().getColumn(col).setPreferredWidth(170);
        ComponentesVisuais.onClick(table, e -> {
            int view = table.rowAtPoint(e.getPoint());
            int column = table.columnAtPoint(e.getPoint());
            if (view < 0 || column != col) return;
            int model = table.convertRowIndexToModel(view);
            Rectangle r = table.getCellRect(view, column, true);
            if (e.getX() - r.x < r.width / 2) {
                edit.accept(model);
            } else {
                delete.accept(model);
            }
        });
    }

    private static DefaultTableModel employeeModel() {
        return ComponentesVisuais.readOnlyModel(
                employeeRows(), "Código", "Nome", "Perfil", "Ações");
    }

    private static Object[][] employeeRows() {
        return EMPLOYEES.stream()
                .map(x -> new Object[]{String.format("%03d", x.id), x.name, x.profile, ""})
                .toArray(Object[][]::new);
    }

    private static DefaultTableModel listModel(List<String> values) {
        return ComponentesVisuais.readOnlyModel(listRows(values), "Código", "Descrição", "Ações");
    }

    private static Object[][] listRows(List<String> values) {
        Object[][] rows = new Object[values.size()][3];
        for (int i = 0; i < values.size(); i++) {
            rows[i] = new Object[]{String.format("%03d", i + 1), values.get(i), ""};
        }
        return rows;
    }

    private static JPanel statsRow(JComponent... cards) {
        JPanel row = new JPanel(new GridLayout(1, cards.length, 10, 0));
        row.setOpaque(false);
        for (JComponent c : cards) row.add(c);
        return row;
    }

    private static JComponent stat(String title, String value, Color fill, Color line) {
        ComponentesVisuais.RoundPanel p = new ComponentesVisuais.RoundPanel(14).fill(fill).line(line).shadow(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(10, 18, 10, 18));
        JLabel t = TelaLivroCaixa.title(title, 18);
        JLabel v = TelaLivroCaixa.title(value, 22);
        v.setForeground(new Color(20, 40, 184));
        p.add(t);
        p.add(Box.createVerticalStrut(5));
        p.add(v);
        return p;
    }

    private static ComponentesVisuais.RoundPanel panelCard(String title) {
        return panelCard(title, "");
    }

    private static ComponentesVisuais.RoundPanel panelCard(String title, String subtitle) {
        ComponentesVisuais.RoundPanel p = new ComponentesVisuais.RoundPanel(14).fill(Color.WHITE).shadow(false);
        p.setLayout(new BorderLayout(0, 8));
        p.setBorder(new EmptyBorder(10, 12, 10, 12));
        if (!title.isBlank()) {
            JPanel heading = new JPanel();
            heading.setOpaque(false);
            heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
            heading.add(TelaLivroCaixa.title(title, 20));
            if (!subtitle.isBlank()) {
                heading.add(Box.createVerticalStrut(2));
                JLabel explanation = new JLabel(subtitle);
                explanation.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                explanation.setForeground(ComponentesVisuais.MUTED);
                heading.add(explanation);
            }
            p.add(heading, BorderLayout.NORTH);
        }
        return p;
    }

    private static JComponent sessionCard(String title, String first, String second,
                                          Color color, String icon) {
        ComponentesVisuais.RoundPanel p = new ComponentesVisuais.RoundPanel(13)
                .fill(new Color(color.getRed(), color.getGreen(), color.getBlue(), 23))
                .line(new Color(color.getRed(), color.getGreen(), color.getBlue(), 115))
                .shadow(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(14, 10, 14, 10));
        JLabel h = new JLabel(title, ComponentesVisuais.vectorIcon(icon, 24, color), SwingConstants.LEFT);
        h.setIconTextGap(6);
        h.setFont(new Font("Segoe UI", Font.BOLD, 16));
        p.add(h);
        p.add(Box.createVerticalStrut(13));
        JLabel firstLabel = new JLabel(first);
        firstLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        p.add(firstLabel);
        JLabel value = TelaLivroCaixa.title(second, second.length() > 16 ? 14 : 17);
        value.setForeground(new Color(18, 39, 185));
        p.add(Box.createVerticalStrut(5));
        p.add(value);
        return p;
    }

    private static ComponentesVisuais.RoundPanel tableCard(String title, String[] columns,
                                               Object[][] rows, String footer) {
        ComponentesVisuais.RoundPanel p = panelCard(title);
        JTable table = ComponentesVisuais.modernTable(ComponentesVisuais.readOnlyModel(rows, columns));
        table.setRowHeight(28);
        p.add(new JScrollPane(table));
        if (!footer.isBlank()) {
            JLabel f = new JLabel(footer);
            f.setFont(new Font("Segoe UI", Font.BOLD, 14));
            f.setOpaque(true);
            f.setBackground(new Color(232, 238, 248));
            f.setBorder(new EmptyBorder(6, 8, 6, 8));
            p.add(f, BorderLayout.SOUTH);
        }
        return p;
    }

    private static JComponent fieldBlock(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMinimumSize(new Dimension(0, 72));
        p.setPreferredSize(new Dimension(250, 72));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        field.setPreferredSize(new Dimension(250, 46));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 15));
        p.add(l, BorderLayout.NORTH);
        p.add(field);
        return p;
    }
    private static String capitalize(String s) { return Character.toUpperCase(s.charAt(0)) + s.substring(1); }

    private record EmployeeRow(int id, String name, String profile) {}

    private static final class ReportTabButton extends JButton {
        private boolean selected;

        ReportTabButton(String text, Icon icon) {
            super(text, icon);
            setIconTextGap(12);
            setFont(new Font("Segoe UI", Font.BOLD, 19));
            setForeground(ComponentesVisuais.INK);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(0, 58));
        }

        @Override
        public void setSelected(boolean selected) {
            super.setSelected(selected);
            this.selected = selected;
            setForeground(selected ? Color.WHITE : ComponentesVisuais.INK);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            ComponentesVisuais.antialias(g);
            g.setColor(selected ? ComponentesVisuais.BLUE : new Color(237, 242, 250));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g.setColor(selected ? ComponentesVisuais.BLUE : new Color(167, 186, 218));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static final class ActionRenderer implements TableCellRenderer {
        private final IntPredicate locked;

        ActionRenderer(IntPredicate locked) {
            this.locked = locked;
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                       boolean focus, int row, int column) {
            JPanel p = new JPanel(new GridBagLayout());
            p.setBorder(new EmptyBorder(3, 4, 3, 4));
            p.setBackground(selected ? table.getSelectionBackground()
                    : row % 2 == 0 ? Color.WHITE : new Color(249, 251, 255));
            JButton edit = new ComponentesVisuais.RoundButton(
                    "Editar", ComponentesVisuais.vectorIcon("edit", 15, ComponentesVisuais.BLUE),
                    new Color(231, 240, 255), ComponentesVisuais.BLUE);
            boolean isLocked = locked.test(table.convertRowIndexToModel(row));
            JButton del = new ComponentesVisuais.RoundButton(
                    isLocked ? "Em uso" : "Excluir",
                    ComponentesVisuais.vectorIcon("trash", 15, isLocked ? Color.GRAY : ComponentesVisuais.RED),
                    isLocked ? new Color(235, 235, 235) : new Color(255, 235, 237),
                    isLocked ? Color.GRAY : ComponentesVisuais.RED);
            edit.setFont(new Font("Segoe UI", Font.BOLD, 12));
            del.setFont(new Font("Segoe UI", Font.BOLD, 12));
            edit.setIconTextGap(5);
            del.setIconTextGap(5);
            edit.setBorder(new EmptyBorder(4, 6, 4, 6));
            del.setBorder(new EmptyBorder(4, 6, 4, 6));
            ComponentesVisuais.addActionPair(p, edit, del);
            return p;
        }
    }

    private static final class HorizontalBarChart extends JComponent {
        private final String[] labels;
        private final double[] values;
        private final boolean money;
        private final Color accent;

        HorizontalBarChart(String[] labels, double[] values, boolean money, Color accent) {
            this.labels = labels;
            this.values = values;
            this.money = money;
            this.accent = accent;
            setPreferredSize(new Dimension(0, 230));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            ComponentesVisuais.antialias(g);
            int left = 215;
            int right = 100;
            int top = 28;
            int bottom = 14;
            int w = getWidth() - left - right;
            int h = getHeight() - top - bottom;
            double max = 1;
            for (double v : values) max = Math.max(max, v);
            int row = Math.max(18, h / Math.max(1, values.length));
            int barHeight = Math.min(18, Math.max(12, row - 4));
            int chartWidth = Math.max(1, w - 20);

            g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g.setColor(ComponentesVisuais.MUTED);
            g.drawString("0", left, 15);
            String maximum = formatChartValue(max, money);
            FontMetrics scaleMetrics = g.getFontMetrics();
            g.drawString(maximum, left + chartWidth - scaleMetrics.stringWidth(maximum), 15);
            g.setColor(new Color(225, 232, 243));
            for (int step = 0; step <= 4; step++) {
                int x = left + chartWidth * step / 4;
                g.drawLine(x, top - 3, x, getHeight() - bottom);
            }

            for (int i = 0; i < values.length; i++) {
                int y = top + i * row + 2;
                int bw = (int) (values[i] / max * chartWidth);
                g.setColor(ComponentesVisuais.INK);
                g.setFont(new Font("Segoe UI", Font.BOLD, 13));
                FontMetrics fm = g.getFontMetrics();
                g.drawString(labels[i], left - 12 - fm.stringWidth(labels[i]), y + barHeight - 2);
                g.setColor(new Color(235, 240, 247));
                g.fillRoundRect(left, y, chartWidth, barHeight, 8, 8);
                Color end = accent.darker();
                g.setPaint(new GradientPaint(left, y, accent, left + bw, y, end));
                g.fillRoundRect(left, y, Math.max(8, bw), barHeight, 8, 8);
                String val = formatChartValue(values[i], money);
                g.setColor(ComponentesVisuais.INK);
                g.setFont(new Font("Segoe UI", Font.BOLD, 13));
                int valueX = Math.min(left + bw + 10, getWidth() - right + 12);
                g.drawString(val, valueX, y + barHeight - 2);
            }
            g.dispose();
        }

        private static String formatChartValue(double value, boolean money) {
            if (!money) return String.format("%.0f", value);
            return "R$ " + String.format("%,.2f", value)
                    .replace(',', 'X').replace('.', ',').replace('X', '.');
        }
    }

    private static final class TwoBarChart extends JComponent {
        final double income, expense;

        TwoBarChart(double income, double expense) {
            this.income = income;
            this.expense = expense;
            setPreferredSize(new Dimension(0, 190));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            ComponentesVisuais.antialias(g);
            int top = 30;
            int bottom = 30;
            int base = getHeight() - bottom;
            int maxH = base - top;
            double max = Math.max(1, Math.max(income, expense));
            int bar = Math.min(120, getWidth() / 5);
            int gap = 70;
            int total = bar * 2 + gap;
            int start = (getWidth() - total) / 2;
            g.setFont(new Font("Segoe UI",Font.PLAIN,11));
            for (int step=0;step<=4;step++) {
                int y=base-(maxH-22)*step/4;
                g.setColor(new Color(226,233,243));
                g.drawLine(28,y,getWidth()-18,y);
                g.setColor(ComponentesVisuais.MUTED);
                String scale="R$ "+String.format("%,.0f",max*step/4)
                        .replace(',', 'X').replace('.', ',').replace('X', '.');
                g.drawString(scale,32,y-3);
            }
            paintBar(g,start,base,bar,maxH,income,max,new Color(22,158,76),"Entradas");
            paintBar(g,start+bar+gap,base,bar,maxH,expense,max,new Color(225,55,65),"Saídas");
            g.dispose();
        }

        private void paintBar(Graphics2D g, int x, int base, int w, int maxH,
                              double value, double max, Color color, String label) {
            int h = (int) (value / max * (maxH - 22));
            g.setPaint(new GradientPaint(x, base - h, color.brighter(), x + w, base - h, color));
            g.fillRoundRect(x,base-h,w,h,12,12);
            g.setColor(ComponentesVisuais.INK);
            g.setFont(new Font("Segoe UI", Font.BOLD, 14));
            String v = "R$ " + String.format("%,.2f", value)
                    .replace(',', 'X').replace('.', ',').replace('X', '.');
            FontMetrics fm = g.getFontMetrics();
            g.drawString(v, x + (w - fm.stringWidth(v)) / 2, base - h - 6);
            g.drawString(label, x + (w - fm.stringWidth(label)) / 2, base + 20);
        }
    }
}
