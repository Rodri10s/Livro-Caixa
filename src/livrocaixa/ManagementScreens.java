package livrocaixa;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntPredicate;

/** Telas de relatórios e configurações reproduzidas a partir dos protótipos 13–16. */
final class ManagementScreens {
    private ManagementScreens() {}

    private static final List<EmployeeRow> EMPLOYEES = new ArrayList<>(List.of(
            new EmployeeRow(1, "Ana Souza", "Administrador"),
            new EmployeeRow(2, "Bruno Reis", "Atendente")
    ));

    static JComponent buildReports(LivroCaixaApp.MainFrame frame) {
        JPanel root = LivroCaixaApp.page();
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
        canvas.setPreferredSize(new Dimension(1, 760));

        JScrollPane scroll = new JScrollPane(
                canvas,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
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
        ReportTabButton button = new ReportTabButton(text, UiKit.vectorIcon(icon, 29, UiKit.INK));
        map.put(text, button);
        bar.add(button);
    }

    private static JComponent reportFilters(LivroCaixaApp.MainFrame frame) {
        UiKit.RoundPanel box = new UiKit.RoundPanel(14).fill(Color.WHITE).shadow(false);
        box.setLayout(new GridBagLayout());
        box.setBorder(new EmptyBorder(10, 12, 10, 12));
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0; g.insets = new Insets(0, 0, 0, 12); g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.weightx = .18; box.add(fieldBlock("Data inicial", LivroCaixaApp.modernField("01/11/2026")), g);
        g.gridx = 1; box.add(fieldBlock("Data final", LivroCaixaApp.modernField("30/11/2026")), g);
        JComboBox<String> employee = new JComboBox<>(new String[]{"Todos", "Ana Souza", "Bruno Reis"});
        g.gridx = 2; g.weightx = .20; box.add(fieldBlock("Funcionário", employee), g);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 18)); actions.setOpaque(false);
        JButton apply = UiKit.primary("Aplicar filtros", "search");
        JButton clear = UiKit.secondary("Limpar", "default");
        JButton print = UiKit.secondary("Imprimir", "default");
        apply.addActionListener(e -> LivroCaixaApp.info(frame, "Filtros aplicados ao período informado."));
        clear.addActionListener(e -> employee.setSelectedIndex(0));
        print.addActionListener(e -> LivroCaixaApp.info(frame, "Relatório preparado para impressão."));
        actions.add(apply); actions.add(clear); actions.add(print);
        g.gridx = 3; g.weightx = .44; g.insets = new Insets(0, 0, 0, 0); box.add(actions, g);
        return box;
    }

    private static JComponent overview(LivroCaixaApp.MainFrame frame) {
        JPanel page = new JPanel(new BorderLayout(0, 10)); page.setOpaque(false);
        JPanel stats = statsRow(
                stat("Entradas", LivroCaixaApp.money(frame.store.income()), new Color(223, 255, 222), new Color(82, 210, 84)),
                stat("Saídas", LivroCaixaApp.money(frame.store.expenseTotal()), new Color(255, 229, 231), new Color(244, 130, 137)),
                stat("Saldo", LivroCaixaApp.money(frame.store.balance()), new Color(229, 239, 255), new Color(114, 160, 245)),
                stat("A receber", LivroCaixaApp.money(frame.store.pending()), new Color(255, 247, 207), new Color(242, 190, 36))
        );
        stats.setPreferredSize(new Dimension(0, 92)); page.add(stats, BorderLayout.NORTH);

        JPanel middle = new JPanel(new GridLayout(1, 2, 12, 0)); middle.setOpaque(false);
        UiKit.RoundPanel chartCard = panelCard("Entradas × Saídas");
        chartCard.add(new TwoBarChart(frame.store.income().doubleValue(), frame.store.expenseTotal().doubleValue()));
        middle.add(chartCard);
        UiKit.RoundPanel summaryCard = panelCard("Resumo por sessão");
        JPanel summaries = new JPanel(new GridLayout(1, 3, 10, 0)); summaries.setOpaque(false);
        summaries.add(sessionCard("Saídas", "7 registros", "R$ 4.260,00", UiKit.RED, "swap"));
        summaries.add(sessionCard("Consultas", "18 realizadas", "16 clientes atendidos", UiKit.BLUE, "calendar"));
        summaries.add(sessionCard("Recebimentos", "22 pagamentos", "R$ 12.680,00", UiKit.GREEN, "coins"));
        summaryCard.add(summaries); middle.add(summaryCard);
        page.add(middle);

        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{"05/11/2026", "Entrada", "Parcela 1/4 - Carlos Mendes", "Ana Souza", "Cartão de crédito", "R$ 350,00"});
        rows.add(new Object[]{"05/11/2026", "Saída", "Aluguel", "Ana Souza", "—", "R$ 1.200,00"});
        rows.add(new Object[]{"04/11/2026", "Saída", "Energia", "Ana Souza", "—", "R$ 620,00"});
        UiKit.RoundPanel table = tableCard("Últimas movimentações", new String[]{"Data", "Movimento", "Descrição", "Funcionário", "Forma de pagamento", "Valor"}, rows.toArray(Object[][]::new), "Exibindo 3 de 27 movimentações");
        table.setPreferredSize(new Dimension(0, 226)); page.add(table, BorderLayout.SOUTH);
        return page;
    }

    private static JComponent expenses(LivroCaixaApp.MainFrame frame) {
        JPanel page = new JPanel(new BorderLayout(0, 10)); page.setOpaque(false);
        BigDecimal average = frame.store.expenses.isEmpty() ? BigDecimal.ZERO : frame.store.expenseTotal().divide(BigDecimal.valueOf(frame.store.expenses.size()), 2, RoundingMode.HALF_UP);
        JPanel stats = statsRow(
                stat("Total de saídas", LivroCaixaApp.money(frame.store.expenseTotal()), new Color(255, 229, 231), new Color(244, 130, 137)),
                stat("Saídas realizadas", String.valueOf(frame.store.expenses.size()), new Color(229, 239, 255), new Color(114, 160, 245)),
                stat("Média por saída", LivroCaixaApp.money(average), new Color(224, 255, 222), new Color(86, 210, 87)),
                stat("Maior saída", "Aluguel - R$ 1.200,00", new Color(255, 247, 207), new Color(242, 190, 36))
        );
        stats.setPreferredSize(new Dimension(0, 92)); page.add(stats, BorderLayout.NORTH);
        String[] labels = {"Aluguel", "Manutenção", "Energia", "Limpeza", "Água", "Material de escritório", "Internet"};
        double[] values = {1200, 760, 620, 500, 500, 480, 200};
        UiKit.RoundPanel chart = panelCard("Saídas por tipo"); chart.add(new HorizontalBarChart(labels, values, true)); page.add(chart);
        Object[][] rows = frame.store.expenses.stream().map(x -> new Object[]{LivroCaixaApp.fmt(x.date), x.type, x.employee, LivroCaixaApp.money(x.value)}).toArray(Object[][]::new);
        UiKit.RoundPanel table = tableCard("", new String[]{"Data", "Tipo de saída", "Funcionário", "Valor"}, rows, "");
        table.setPreferredSize(new Dimension(0, 230)); page.add(table, BorderLayout.SOUTH);
        return page;
    }

    private static JComponent appointments(LivroCaixaApp.MainFrame frame) {
        JPanel page = new JPanel(new BorderLayout(0, 10)); page.setOpaque(false);
        JPanel stats = statsRow(
                stat("Consultas realizadas", "18", new Color(255, 229, 231), new Color(244, 130, 137)),
                stat("Clientes atendidos", "16", new Color(229, 239, 255), new Color(114, 160, 245)),
                stat("Tipo mais realizado", "Consulta de rotina", new Color(224, 255, 222), new Color(86, 210, 87)),
                stat("Mais consultas", "Ana Souza", new Color(255, 247, 207), new Color(242, 190, 36))
        );
        stats.setPreferredSize(new Dimension(0, 92)); page.add(stats, BorderLayout.NORTH);
        UiKit.RoundPanel chart = panelCard("Consultas por tipo"); chart.add(new HorizontalBarChart(new String[]{"Consulta de rotina", "Retorno", "Avaliação"}, new double[]{9, 6, 3}, false)); page.add(chart);
        Object[][] rows = frame.store.appointments.stream().map(x -> new Object[]{LivroCaixaApp.fmt(x.date), x.client, x.type, x.employee}).toArray(Object[][]::new);
        UiKit.RoundPanel table = tableCard("", new String[]{"Data", "Cliente", "Tipo", "Funcionário"}, rows, "Exibindo " + rows.length + " de 18 consultas");
        table.setPreferredSize(new Dimension(0, 230)); page.add(table, BorderLayout.SOUTH);
        return page;
    }

    private static JComponent receipts(LivroCaixaApp.MainFrame frame) {
        JPanel page = new JPanel(new BorderLayout(0, 10)); page.setOpaque(false);
        JPanel stats = statsRow(
                stat("Total recebido", "R$ 12.680,00", new Color(255, 229, 231), new Color(244, 130, 137)),
                stat("À vista", "R$ 7.430,00", new Color(229, 239, 255), new Color(114, 160, 245)),
                stat("À prazo", "R$ 5.250,00", new Color(224, 255, 222), new Color(86, 210, 87)),
                stat("Pagamentos registrados", "22", new Color(255, 247, 207), new Color(242, 190, 36))
        );
        stats.setPreferredSize(new Dimension(0, 92)); page.add(stats, BorderLayout.NORTH);
        UiKit.RoundPanel chart = panelCard("Recebimentos por forma de pagamento");
        chart.add(new HorizontalBarChart(new String[]{"Cartão de crédito", "PIX", "Dinheiro", "Cartão de débito"}, new double[]{4200, 3980, 2500, 2000}, true)); page.add(chart);
        Object[][] rows = {{"05/11/2026", "Carlos Mendes", "Parcela 1/4", "À prazo", "Cartão de crédito", "R$ 350,00"}, {"05/11/2026", "Mariana Silva", "Consulta 104", "À vista", "PIX", "R$ 420,00"}, {"04/11/2026", "Fernanda Costa", "Parcela 1/2", "À prazo", "Dinheiro", "R$ 600,00"}};
        UiKit.RoundPanel table = tableCard("", new String[]{"Data", "Cliente", "Origem", "Tipo", "Forma de pagamento", "Valor"}, rows, "Exibindo 3 de 22 pagamentos");
        table.setPreferredSize(new Dimension(0, 230)); page.add(table, BorderLayout.SOUTH);
        return page;
    }

    static JComponent buildSettings(LivroCaixaApp.MainFrame frame) {
        JPanel root = LivroCaixaApp.page(); root.setBorder(new EmptyBorder(16, 16, 16, 16));
        UiKit.RoundPanel body = new UiKit.RoundPanel(17).fill(new Color(249, 251, 255)); body.setLayout(new BorderLayout(0, 22)); body.setBorder(new EmptyBorder(22, 22, 22, 22));
        JPanel heading = new JPanel(); heading.setOpaque(false); heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(LivroCaixaApp.title("Configurações do sistema", 34)); JLabel sub = new JLabel("Gerencie usuários e cadastros auxiliares"); sub.setFont(new Font("Segoe UI", Font.PLAIN, 20)); heading.add(Box.createVerticalStrut(4)); heading.add(sub); body.add(heading, BorderLayout.NORTH);
        JPanel cards = new JPanel(new GridLayout(1, 5, 12, 0)); cards.setOpaque(false);
        cards.add(settingsCard(frame, "Funcionários", "2 cadastrados", "group", new Color(227, 255, 225), new Color(95, 212, 98), 0));
        cards.add(settingsCard(frame, "Perfis", "2 cadastrados", "shield", new Color(255, 230, 231), new Color(239, 128, 132), 1));
        cards.add(settingsCard(frame, "Tipos de consulta", frame.store.appointmentTypes.size() + " cadastrados", "calendar", new Color(231, 239, 255), new Color(115, 154, 238), 2));
        cards.add(settingsCard(frame, "Tipos de saída", frame.store.expenseTypes.size() + " cadastrados", "logout", new Color(255, 247, 210), new Color(235, 189, 49), 3));
        cards.add(settingsCard(frame, "Formas de pagamento", frame.store.paymentMethods.size() + " cadastradas", "card", new Color(239, 232, 255), new Color(162, 125, 231), 4));
        body.add(cards);
        UiKit.RoundPanel info = new UiKit.RoundPanel(13).fill(new Color(231, 240, 255)).shadow(false); info.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 12)); JLabel infoLabel = new JLabel("Cadastros em uso não podem ser excluídos.", UiKit.vectorIcon("info", 28, UiKit.BLUE), SwingConstants.LEFT); infoLabel.setIconTextGap(16); infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 17)); info.add(infoLabel); body.add(info, BorderLayout.SOUTH);
        root.add(body); return root;
    }

    private static JComponent settingsCard(LivroCaixaApp.MainFrame frame, String name, String count, String icon, Color fill, Color line, int route) {
        UiKit.RoundPanel card = new UiKit.RoundPanel(18).fill(fill).line(line).shadow(false); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); card.setBorder(new EmptyBorder(24, 14, 20, 14));
        JLabel glyph = new JLabel(UiKit.vectorIcon(icon, 58, new Color(23, 46, 176))); glyph.setAlignmentX(.5f); JLabel title = LivroCaixaApp.title("<html><center>" + name + "</center></html>", 20); title.setAlignmentX(.5f); JLabel amount = new JLabel(count); amount.setFont(new Font("Segoe UI", Font.PLAIN, 17)); amount.setAlignmentX(.5f); JButton manage = UiKit.primary("Gerenciar", "default"); manage.setAlignmentX(.5f); manage.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50)); manage.addActionListener(e -> { if (route <= 1) frame.showCustom("Configurações", employeesAndProfiles(frame, route)); else frame.showCustom("Configurações", auxiliary(frame, route - 2)); });
        card.add(Box.createVerticalGlue()); card.add(glyph); card.add(Box.createVerticalStrut(20)); card.add(title); card.add(Box.createVerticalStrut(18)); card.add(amount); card.add(Box.createVerticalStrut(28)); card.add(manage); card.add(Box.createVerticalGlue()); return card;
    }

    private static JComponent employeesAndProfiles(LivroCaixaApp.MainFrame frame, int selectedTab) {
        JPanel root = LivroCaixaApp.page(); UiKit.RoundPanel body = new UiKit.RoundPanel(17).fill(new Color(249, 251, 255)); body.setLayout(new BorderLayout(20, 14)); body.setBorder(new EmptyBorder(18, 20, 18, 20)); body.add(LivroCaixaApp.title("Funcionários e perfis", 32), BorderLayout.NORTH);
        JPanel content = new JPanel(new GridLayout(1, 3, 20, 0)); content.setOpaque(false);
        JPanel left = new JPanel(new BorderLayout(0, 12)); left.setOpaque(false);
        JTabbedPane tabs = new JTabbedPane(); tabs.setFont(new Font("Segoe UI", Font.BOLD, 18)); tabs.addTab("Funcionários", employeeList(frame)); tabs.addTab("Perfis", profileList()); tabs.setSelectedIndex(Math.min(1, selectedTab)); left.add(tabs); content.add(left); content.add(employeeForm(frame)); content.add(profileSummary(frame)); body.add(content);
        JButton back = UiKit.secondary("Voltar", "default"); back.addActionListener(e -> frame.showPage("Configurações")); JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT)); south.setOpaque(false); south.add(back); body.add(south, BorderLayout.SOUTH); root.add(body); return root;
    }

    private static JComponent employeeList(LivroCaixaApp.MainFrame frame) {
        JPanel p = new JPanel(new BorderLayout(0, 10)); p.setOpaque(false); JPanel search = new JPanel(new BorderLayout(8, 0)); search.setOpaque(false); JTextField field = LivroCaixaApp.modernField(""); search.add(field); search.add(UiKit.primary("Buscar", "search"), BorderLayout.EAST); p.add(search, BorderLayout.NORTH);
        DefaultTableModel model = employeeModel(); JTable table = UiKit.modernTable(model); installActions(table, row -> false, row -> {}, row -> { if (LivroCaixaApp.confirm(frame, "Excluir funcionário?")) { EMPLOYEES.remove(row); model.setDataVector(employeeRows(), new String[]{"Código", "Nome", "Perfil", "Ações"}); }}); p.add(new JScrollPane(table)); return p;
    }

    private static JComponent profileList() { return tableCard("Perfis cadastrados", new String[]{"Código", "Perfil"}, new Object[][]{{"001", "Administrador"}, {"002", "Atendente"}}, ""); }

    private static JComponent employeeForm(LivroCaixaApp.MainFrame frame) {
        UiKit.RoundPanel p = panelCard("Novo funcionário"); p.setBorder(new EmptyBorder(14, 16, 14, 16)); JPanel fields = new JPanel(); fields.setOpaque(false); fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS)); JTextField name = LivroCaixaApp.modernField("Mariana Alves"); JComboBox<String> profile = new JComboBox<>(new String[]{"Administrador", "Atendente"}); JPasswordField password = LivroCaixaApp.modernPassword("12345678"); fields.add(fieldBlock("Nome", name)); fields.add(Box.createVerticalStrut(9)); fields.add(fieldBlock("Perfil", profile)); fields.add(Box.createVerticalStrut(9)); fields.add(fieldBlock("Senha", password)); p.add(fields); JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0)); actions.setOpaque(false); JButton cancel = UiKit.secondary("Cancelar", "default"), save = UiKit.primary("Salvar", "save"); save.addActionListener(e -> { if (!name.getText().isBlank()) { EMPLOYEES.add(new EmployeeRow(EMPLOYEES.size() + 1, name.getText().trim(), String.valueOf(profile.getSelectedItem()))); LivroCaixaApp.info(frame, "Funcionário salvo com sucesso."); frame.showCustom("Configurações", employeesAndProfiles(frame, 0)); }}); actions.add(cancel); actions.add(save); p.add(actions, BorderLayout.SOUTH); return p;
    }

    private static JComponent profileSummary(LivroCaixaApp.MainFrame frame) {
        UiKit.RoundPanel p = panelCard("Perfis cadastrados"); p.add(tableCard("", new String[]{"Código", "Perfil"}, new Object[][]{{"001", "Administrador"}, {"002", "Atendente"}}, "")); JButton manage = UiKit.primary("Gerenciar perfis", "gear"); manage.addActionListener(e -> frame.showCustom("Configurações", employeesAndProfiles(frame, 1))); p.add(manage, BorderLayout.SOUTH); return p;
    }

    private static JComponent auxiliary(LivroCaixaApp.MainFrame frame, int selected) {
        JPanel root = LivroCaixaApp.page(); UiKit.RoundPanel body = new UiKit.RoundPanel(17).fill(new Color(249, 251, 255)); body.setLayout(new BorderLayout(0, 14)); body.setBorder(new EmptyBorder(14, 16, 14, 16)); JPanel heading = new JPanel(); heading.setOpaque(false); heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS)); heading.add(LivroCaixaApp.title("Cadastros auxiliares", 32)); heading.add(new JLabel("Escolha uma categoria para consultar, cadastrar ou editar.")); body.add(heading, BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane(); tabs.setFont(new Font("Segoe UI", Font.BOLD, 18)); tabs.addTab("Tipos de consulta", categoryEditor(frame, frame.store.appointmentTypes, "tipo de consulta", "Estas opções aparecem no cadastro de consultas.", v -> frame.store.appointments.stream().anyMatch(a -> a.type.equals(v)))); tabs.addTab("Tipos de saída", categoryEditor(frame, frame.store.expenseTypes, "tipo de saída", "Estas opções aparecem no cadastro de saídas.", v -> frame.store.expenses.stream().anyMatch(x -> x.type.equals(v)))); tabs.addTab("Formas de pagamento", categoryEditor(frame, frame.store.paymentMethods, "forma de pagamento", "Estas opções aparecem no registro de pagamentos.", v -> frame.store.payments.stream().anyMatch(x -> x.method.equals(v)))); tabs.setSelectedIndex(Math.max(0, Math.min(selected, 2))); body.add(tabs);
        UiKit.RoundPanel tips = new UiKit.RoundPanel(12).fill(new Color(231, 240, 255)).shadow(false); tips.setLayout(new GridLayout(1, 4, 10, 0)); tips.setBorder(new EmptyBorder(10, 14, 10, 14)); tips.add(new JLabel("ⓘ Registros em uso não podem ser excluídos.")); tips.add(new JLabel("① Escolha a categoria")); tips.add(new JLabel("② Clique em Novo ou Editar")); tips.add(new JLabel("③ Salve")); body.add(tips, BorderLayout.SOUTH); root.add(body); return root;
    }

    private static JComponent categoryEditor(LivroCaixaApp.MainFrame frame, List<String> values, String singular, String hint, java.util.function.Predicate<String> used) {
        JPanel content = new JPanel(new GridLayout(1, 2, 18, 0)); content.setBorder(new EmptyBorder(14, 8, 14, 8)); content.setOpaque(false);
        UiKit.RoundPanel left = panelCard(capitalize(singular) + " cadastrados"); JLabel helper = new JLabel(hint, UiKit.vectorIcon("default", 22, UiKit.BLUE), SwingConstants.LEFT); helper.setForeground(new Color(23, 54, 178)); left.add(helper, BorderLayout.NORTH);
        DefaultTableModel model = listModel(values); JTable table = UiKit.modernTable(model); JPanel tableHolder = new JPanel(new BorderLayout(0, 10)); tableHolder.setOpaque(false); JButton add = UiKit.primary("Novo " + singular, "plus"); tableHolder.add(add, BorderLayout.NORTH); tableHolder.add(new JScrollPane(table)); left.add(tableHolder);
        UiKit.RoundPanel right = panelCard("Novo " + singular); JPanel form = new JPanel(); form.setOpaque(false); form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS)); JTextField description = LivroCaixaApp.modernField(""); form.add(fieldBlock("Descrição", description)); right.add(form); JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0)); actions.setOpaque(false); JButton cancel = UiKit.secondary("Cancelar", "default"), save = UiKit.primary("Salvar", "save"); actions.add(cancel); actions.add(save); right.add(actions, BorderLayout.SOUTH);
        final int[] editing = {-1}; add.addActionListener(e -> { editing[0] = -1; description.setText(""); description.requestFocusInWindow(); }); cancel.addActionListener(e -> { editing[0] = -1; description.setText(""); }); save.addActionListener(e -> { String text = description.getText().trim(); if (text.isBlank()) return; if (editing[0] >= 0) values.set(editing[0], text); else values.add(text); frame.store.save(); model.setDataVector(listRows(values), new String[]{"Código", "Descrição", "Ações"}); editing[0] = -1; description.setText(""); });
        installActions(table, row -> used.test(values.get(row)), row -> { editing[0] = row; description.setText(values.get(row)); }, row -> { if (used.test(values.get(row))) { LivroCaixaApp.error(frame, "Este cadastro está em uso e não pode ser excluído."); return; } if (LivroCaixaApp.confirm(frame, "Excluir este cadastro?")) { values.remove(row); frame.store.save(); model.setDataVector(listRows(values), new String[]{"Código", "Descrição", "Ações"}); }});
        content.add(left); content.add(right); return content;
    }

    private static void installActions(JTable table, IntPredicate locked, java.util.function.IntConsumer edit, java.util.function.IntConsumer delete) {
        int col = table.getColumnCount() - 1; table.getColumnModel().getColumn(col).setCellRenderer(new ActionRenderer(locked)); table.getColumnModel().getColumn(col).setPreferredWidth(220);
        table.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { int view = table.rowAtPoint(e.getPoint()), column = table.columnAtPoint(e.getPoint()); if (view < 0 || column != col) return; int model = table.convertRowIndexToModel(view); Rectangle r = table.getCellRect(view, column, true); if (e.getX() - r.x < r.width / 2) edit.accept(model); else delete.accept(model); }});
    }

    private static DefaultTableModel employeeModel() { return new DefaultTableModel(employeeRows(), new String[]{"Código", "Nome", "Perfil", "Ações"}) { public boolean isCellEditable(int r, int c) { return false; }}; }
    private static Object[][] employeeRows() { return EMPLOYEES.stream().map(x -> new Object[]{String.format("%03d", x.id), x.name, x.profile, ""}).toArray(Object[][]::new); }
    private static DefaultTableModel listModel(List<String> values) { return new DefaultTableModel(listRows(values), new String[]{"Código", "Descrição", "Ações"}) { public boolean isCellEditable(int r, int c) { return false; }}; }
    private static Object[][] listRows(List<String> values) { Object[][] rows = new Object[values.size()][3]; for (int i = 0; i < values.size(); i++) rows[i] = new Object[]{String.format("%03d", i + 1), values.get(i), ""}; return rows; }

    private static JPanel statsRow(JComponent... cards) { JPanel row = new JPanel(new GridLayout(1, cards.length, 10, 0)); row.setOpaque(false); for (JComponent c : cards) row.add(c); return row; }
    private static JComponent stat(String title, String value, Color fill, Color line) { UiKit.RoundPanel p = new UiKit.RoundPanel(14).fill(fill).line(line).shadow(false); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBorder(new EmptyBorder(10, 18, 10, 18)); JLabel t = LivroCaixaApp.title(title, 18); JLabel v = LivroCaixaApp.title(value, 22); v.setForeground(new Color(20, 40, 184)); p.add(t); p.add(Box.createVerticalStrut(5)); p.add(v); return p; }
    private static UiKit.RoundPanel panelCard(String title) { UiKit.RoundPanel p = new UiKit.RoundPanel(14).fill(Color.WHITE).shadow(false); p.setLayout(new BorderLayout(0, 8)); p.setBorder(new EmptyBorder(10, 12, 10, 12)); if (!title.isBlank()) p.add(LivroCaixaApp.title(title, 20), BorderLayout.NORTH); return p; }
    private static JComponent sessionCard(String title, String first, String second, Color color, String icon) { UiKit.RoundPanel p = new UiKit.RoundPanel(13).fill(new Color(color.getRed(), color.getGreen(), color.getBlue(), 23)).line(new Color(color.getRed(), color.getGreen(), color.getBlue(), 115)).shadow(false); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBorder(new EmptyBorder(14, 10, 14, 10)); JLabel h = new JLabel(title, UiKit.vectorIcon(icon, 24, color), SwingConstants.LEFT); h.setIconTextGap(6); h.setFont(new Font("Segoe UI", Font.BOLD, 16)); p.add(h); p.add(Box.createVerticalStrut(13)); JLabel firstLabel=new JLabel(first);firstLabel.setFont(new Font("Segoe UI",Font.PLAIN,14));p.add(firstLabel); JLabel value = LivroCaixaApp.title(second, second.length()>16?14:17); value.setForeground(new Color(18, 39, 185)); p.add(Box.createVerticalStrut(5)); p.add(value); return p; }
    private static UiKit.RoundPanel tableCard(String title, String[] columns, Object[][] rows, String footer) { UiKit.RoundPanel p = panelCard(title); JTable table = UiKit.modernTable(new DefaultTableModel(rows, columns) { public boolean isCellEditable(int r, int c) { return false; }}); table.setRowHeight(28); p.add(new JScrollPane(table)); if (!footer.isBlank()) { JLabel f = new JLabel(footer); f.setFont(new Font("Segoe UI", Font.BOLD, 14)); f.setOpaque(true); f.setBackground(new Color(232, 238, 248)); f.setBorder(new EmptyBorder(6, 8, 6, 8)); p.add(f, BorderLayout.SOUTH); } return p; }
    private static JComponent fieldBlock(String label, JComponent field) { JPanel p = new JPanel(new BorderLayout(0, 4)); p.setOpaque(false); JLabel l = new JLabel(label); l.setFont(new Font("Segoe UI", Font.BOLD, 15)); p.add(l, BorderLayout.NORTH); p.add(field); return p; }
    private static String capitalize(String s) { return Character.toUpperCase(s.charAt(0)) + s.substring(1); }

    private record EmployeeRow(int id, String name, String profile) {}

    private static final class ReportTabButton extends JButton {
        private boolean selected;
        ReportTabButton(String text, Icon icon) { super(text, icon); setIconTextGap(12); setFont(new Font("Segoe UI", Font.BOLD, 19)); setForeground(UiKit.INK); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); setPreferredSize(new Dimension(0, 58)); }
        @Override public void setSelected(boolean selected) { super.setSelected(selected); this.selected = selected; setForeground(selected ? Color.WHITE : UiKit.INK); repaint(); }
        @Override protected void paintComponent(Graphics graphics) { Graphics2D g = (Graphics2D) graphics.create(); UiKit.antialias(g); g.setColor(selected ? UiKit.BLUE : new Color(237, 242, 250)); g.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14); g.setColor(selected ? UiKit.BLUE : new Color(167, 186, 218)); g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14); g.dispose(); super.paintComponent(graphics); }
    }

    private static final class ActionRenderer implements TableCellRenderer {
        private final IntPredicate locked;
        ActionRenderer(IntPredicate locked) { this.locked = locked; }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) { JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 3)); p.setBackground(selected ? table.getSelectionBackground() : row % 2 == 0 ? Color.WHITE : new Color(249, 251, 255)); JButton edit = new UiKit.RoundButton("Editar", UiKit.vectorIcon("edit", 15, UiKit.BLUE), new Color(231, 240, 255), UiKit.BLUE); boolean isLocked = locked.test(table.convertRowIndexToModel(row)); JButton del = new UiKit.RoundButton(isLocked ? "Em uso" : "Excluir", UiKit.vectorIcon("trash", 15, isLocked ? Color.GRAY : UiKit.RED), isLocked ? new Color(235, 235, 235) : new Color(255, 235, 237), isLocked ? Color.GRAY : UiKit.RED); edit.setFont(new Font("Segoe UI", Font.BOLD, 12)); del.setFont(new Font("Segoe UI", Font.BOLD, 12)); edit.setBorder(new EmptyBorder(5, 9, 5, 9)); del.setBorder(new EmptyBorder(5, 9, 5, 9)); p.add(edit); p.add(del); return p; }
    }

    private static final class HorizontalBarChart extends JComponent {
        private final String[] labels; private final double[] values; private final boolean money;
        HorizontalBarChart(String[] labels, double[] values, boolean money) { this.labels = labels; this.values = values; this.money = money; setPreferredSize(new Dimension(0, 190)); }
        @Override protected void paintComponent(Graphics graphics) { Graphics2D g = (Graphics2D) graphics.create(); UiKit.antialias(g); int left = 215, right = 55, top = 8, bottom = 24, w = getWidth() - left - right, h = getHeight() - top - bottom; double max = 1; for (double v : values) max = Math.max(max, v); int row = Math.max(26, h / Math.max(1, values.length)); g.setFont(new Font("Segoe UI", Font.PLAIN, 13)); for (int i = 0; i < values.length; i++) { int y = top + i * row + 4; int bw = (int) (values[i] / max * Math.max(1, w - 65)); g.setColor(UiKit.INK); FontMetrics fm = g.getFontMetrics(); g.drawString(labels[i], left - 12 - fm.stringWidth(labels[i]), y + 16); g.setPaint(new GradientPaint(left, y, UiKit.BLUE, left + bw, y, UiKit.BLUE_2)); g.fillRoundRect(left, y, bw, 18, 4, 4); String val = money ? "R$ " + String.format("%,.2f", values[i]).replace(',', 'X').replace('.', ',').replace('X', '.') : String.format("%.0f", values[i]); g.setColor(UiKit.INK); g.setFont(new Font("Segoe UI", Font.BOLD, 13)); g.drawString(val, left + bw + 10, y + 15); } g.dispose(); }
    }

    private static final class TwoBarChart extends JComponent {
        final double income, expense; TwoBarChart(double income, double expense) { this.income = income; this.expense = expense; setPreferredSize(new Dimension(0, 190)); }
        @Override protected void paintComponent(Graphics graphics) { Graphics2D g = (Graphics2D) graphics.create(); UiKit.antialias(g); int top = 25, bottom = 30, base = getHeight() - bottom, maxH = base - top; double max = Math.max(1, Math.max(income, expense)); int bar = Math.min(150, getWidth() / 4), gap = 55, total = bar * 2 + gap, start = (getWidth() - total) / 2; paintBar(g, start, base, bar, maxH, income, max, new Color(25, 183, 44), "Entradas"); paintBar(g, start + bar + gap, base, bar, maxH, expense, max, UiKit.RED, "Saídas"); g.dispose(); }
        private void paintBar(Graphics2D g, int x, int base, int w, int maxH, double value, double max, Color color, String label) { int h = (int) (value / max * (maxH - 22)); g.setPaint(new GradientPaint(x, base - h, color.brighter(), x + w, base - h, color)); g.fillRect(x, base - h, w, h); g.setColor(UiKit.INK); g.setFont(new Font("Segoe UI", Font.BOLD, 14)); String v = "R$ " + String.format("%,.2f", value).replace(',', 'X').replace('.', ',').replace('X', '.'); FontMetrics fm = g.getFontMetrics(); g.drawString(v, x + (w - fm.stringWidth(v)) / 2, base - h - 6); g.drawString(label, x + (w - fm.stringWidth(label)) / 2, base + 20); }
    }
}
