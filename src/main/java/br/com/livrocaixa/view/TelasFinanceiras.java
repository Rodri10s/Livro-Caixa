package br.com.livrocaixa.view;

import br.com.livrocaixa.view.dados.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Telas financeiras de listagem, desenhadas conforme os protótipos 08 e 11. */
final class TelasFinanceiras {
    private static final LocalDate REFERENCE_DATE = LivroCaixaModel.DEMO_DATE;

    private TelasFinanceiras() {}

    static JComponent buildReceivables(TelaLivroCaixa.JanelaPrincipal frame) {
        JPanel page = TelaLivroCaixa.page();
        ComponentesVisuais.RoundPanel surface = new ComponentesVisuais.RoundPanel(16).fill(Color.WHITE);
        surface.setLayout(new BorderLayout(0, 15));
        surface.setBorder(new EmptyBorder(17, 18, 17, 18));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JLabel heading = TelaLivroCaixa.title("Contas a receber", 29);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(heading);
        top.add(Box.createVerticalStrut(14));

        JPanel cards = new JPanel(new GridLayout(1, 3, 14, 0));
        cards.setOpaque(false);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 118));
        cards.setPreferredSize(new Dimension(0, 118));
        cards.add(financialCard("A receber", TelaLivroCaixa.money(frame.store.pending()), ComponentesVisuais.BLUE,
                new Color(236, 243, 255), "coins"));
        BigDecimal overdue = frame.store.installments.stream()
                .filter(x -> !x.cancelled && x.due.isBefore(REFERENCE_DATE) && x.paid.compareTo(x.value) < 0)
                .map(x -> x.value.subtract(x.paid)).reduce(BigDecimal.ZERO, BigDecimal::add);
        cards.add(financialCard("Vencido", TelaLivroCaixa.money(overdue), new Color(205, 23, 31),
                new Color(255, 232, 233), "warning"));
        cards.add(financialCard("Recebido no mês", TelaLivroCaixa.money(frame.store.income()), new Color(8, 119, 30),
                new Color(229, 247, 227), "check"));
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(cards);
        top.add(Box.createVerticalStrut(14));

        JTextField start = TelaLivroCaixa.modernField("01/11/2026");
        JTextField end = TelaLivroCaixa.modernField("30/11/2026");
        JComboBox<String> client = new JComboBox<>();
        client.addItem("Todos");
        frame.store.clients.forEach(c -> client.addItem(c.name));
        JComboBox<String> status = new JComboBox<>(new String[]{"Todos", "Pendente", "Vencido", "Pago", "Cancelada"});
        ComponentesVisuais.RoundPanel filters = new ComponentesVisuais.RoundPanel(14).fill(new Color(245, 248, 255)).shadow(false);
        filters.setBorder(new EmptyBorder(11, 12, 11, 12));
        filters.setLayout(new GridBagLayout());
        GridBagConstraints gc = constraints();
        addFilter(filters, gc, 0, "Data inicial", start, 1.0);
        addFilter(filters, gc, 1, "Data final", end, 1.0);
        addFilter(filters, gc, 2, "Cliente", client, 1.15);
        addFilter(filters, gc, 3, "Status", status, 1.0);
        JButton apply = ComponentesVisuais.primary("Aplicar filtros", "filter");
        JButton clear = ComponentesVisuais.secondary("Limpar", "refresh");
        gc.gridx = 4; gc.weightx = 0; gc.fill = GridBagConstraints.NONE; gc.anchor = GridBagConstraints.SOUTH;
        gc.insets = new Insets(20, 8, 0, 0); filters.add(apply, gc);
        gc.gridx = 5; gc.insets = new Insets(20, 8, 0, 0); filters.add(clear, gc);
        filters.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(filters);
        surface.add(top, BorderLayout.NORTH);

        String[] columns = {"Cliente", "Fatura", "Parcela", "Vencimento", "Valor", "Status", "Ações"};
        DefaultTableModel model = ComponentesVisuais.readOnlyModel(columns);
        JTable table = ComponentesVisuais.modernTable(model);
        table.setRowHeight(48);
        table.getColumnModel().getColumn(1).setMaxWidth(110);
        table.getColumnModel().getColumn(2).setMaxWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(135);
        table.getColumnModel().getColumn(5).setPreferredWidth(125);
        table.getColumnModel().getColumn(6).setPreferredWidth(330);
        table.getColumnModel().getColumn(6).setCellRenderer(new DualActionRenderer("Registrar pagamento", "Ver fatura", true));
        List<Installment> visible = new ArrayList<>();

        Runnable refresh = () -> {
            LocalDate from = parseDateOr(start.getText(), LocalDate.MIN);
            LocalDate to = parseDateOr(end.getText(), LocalDate.MAX);
            String chosenClient = String.valueOf(client.getSelectedItem());
            String chosenStatus = String.valueOf(status.getSelectedItem());
            visible.clear();
            model.setRowCount(0);
            frame.store.installments.stream()
                    .filter(x -> !x.due.isBefore(from) && !x.due.isAfter(to))
                    .filter(x -> "Todos".equals(chosenClient) || x.client.equals(chosenClient))
                    .forEach(x -> {
                        String state = installmentStatus(x);
                        if (!"Todos".equals(chosenStatus) && !state.equals(chosenStatus)) return;
                        visible.add(x);
                        model.addRow(new Object[]{x.client, String.format("%03d", x.appointmentId), x.label,
                                TelaLivroCaixa.fmt(x.due), TelaLivroCaixa.money(x.value), state, ""});
                    });
        };
        apply.addActionListener(e -> refresh.run());
        clear.addActionListener(e -> {
            start.setText("01/11/2026");
            end.setText("30/11/2026");
            client.setSelectedIndex(0);
            status.setSelectedIndex(0);
            refresh.run();
        });
        ComponentesVisuais.onClick(table, event -> {
            int viewRow = table.rowAtPoint(event.getPoint());
            int col = table.columnAtPoint(event.getPoint());
            if (viewRow < 0 || col != 6) return;
            int row = table.convertRowIndexToModel(viewRow);
            if (row >= visible.size()) return;
            Installment item = visible.get(row);
            int action = actionIndex(table, viewRow, col, event);
            if (action == 0) {
                if (item.cancelled) return;
                if (item.paid.compareTo(item.value) >= 0) {
                    frame.paymentConfirmed(item);
                } else {
                    frame.paymentDialog(item);
                }
            } else if (action == 1) {
                TelaPagamentoConfirmado.showInvoice(frame, item.appointmentId);
            }
        });
        refresh.run();
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(ComponentesVisuais.LINE));
        surface.add(scroll);
        page.add(surface);
        return page;
    }

    static JComponent buildExpenses(TelaLivroCaixa.JanelaPrincipal frame) {
        JPanel page = TelaLivroCaixa.page();
        ComponentesVisuais.RoundPanel surface = new ComponentesVisuais.RoundPanel(16).fill(Color.WHITE);
        surface.setLayout(new BorderLayout(0, 12));
        surface.setBorder(new EmptyBorder(17, 18, 17, 18));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JLabel heading = TelaLivroCaixa.title("Controle de saídas", 29);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(heading);
        top.add(Box.createVerticalStrut(12));

        JTextField start = TelaLivroCaixa.modernField("01/11/2026");
        JTextField end = TelaLivroCaixa.modernField("30/11/2026");
        JComboBox<String> type = new JComboBox<>();
        type.addItem("Todos");
        frame.store.expenseTypes.forEach(type::addItem);
        JComboBox<String> employee = new JComboBox<>(new String[]{"Todos", frame.employee, "Bruno Reis"});
        ComponentesVisuais.RoundPanel filters = new ComponentesVisuais.RoundPanel(14).fill(new Color(245, 248, 255)).shadow(false);
        filters.setBorder(new EmptyBorder(11, 12, 11, 12));
        filters.setLayout(new GridBagLayout());
        GridBagConstraints gc = constraints();
        addFilter(filters, gc, 0, "Data inicial", start, 1.0);
        addFilter(filters, gc, 1, "Data final", end, 1.0);
        addFilter(filters, gc, 2, "Tipo de saída", type, 1.15);
        addFilter(filters, gc, 3, "Funcionário", employee, 1.0);
        JButton apply = ComponentesVisuais.primary("Aplicar filtros", "filter");
        JButton clear = ComponentesVisuais.secondary("Limpar", "refresh");
        gc.gridx = 4;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        gc.anchor = GridBagConstraints.SOUTH;
        gc.insets = new Insets(20, 8, 0, 0);
        filters.add(apply, gc);
        gc.gridx = 5;
        filters.add(clear, gc);
        filters.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(filters);
        top.add(Box.createVerticalStrut(11));

        JPanel addRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        addRow.setOpaque(false);
        JButton add = ComponentesVisuais.primary("Nova saída", "plus");
        add.addActionListener(e -> frame.expenseDialog(null));
        addRow.add(add);
        addRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(addRow);
        surface.add(top, BorderLayout.NORTH);

        String[] columns = {"Código", "Data", "Tipo de saída", "Funcionário", "Valor", "Ações"};
        DefaultTableModel model = ComponentesVisuais.readOnlyModel(columns);
        JTable table = ComponentesVisuais.modernTable(model);
        table.setRowHeight(43);
        table.getColumnModel().getColumn(0).setMaxWidth(115);
        table.getColumnModel().getColumn(1).setPreferredWidth(145);
        table.getColumnModel().getColumn(4).setPreferredWidth(165);
        table.getColumnModel().getColumn(5).setPreferredWidth(225);
        table.getColumnModel().getColumn(5).setCellRenderer(new DualActionRenderer("Editar", "Excluir", false));

        List<Expense> visible = new ArrayList<>();
        JLabel totalValue = TelaLivroCaixa.title(TelaLivroCaixa.money(BigDecimal.ZERO), 22);
        totalValue.setForeground(Color.WHITE);
        Runnable refresh = () -> {
            LocalDate from = parseDateOr(start.getText(), LocalDate.MIN);
            LocalDate to = parseDateOr(end.getText(), LocalDate.MAX);
            String chosenType = String.valueOf(type.getSelectedItem());
            String chosenEmployee = String.valueOf(employee.getSelectedItem());
            visible.clear();
            model.setRowCount(0);
            BigDecimal[] total = {BigDecimal.ZERO};
            frame.store.expenses.stream()
                    .filter(x -> !x.date.isBefore(from) && !x.date.isAfter(to))
                    .filter(x -> "Todos".equals(chosenType) || x.type.equals(chosenType))
                    .filter(x -> "Todos".equals(chosenEmployee) || x.employee.equals(chosenEmployee))
                    .forEach(x -> {
                        visible.add(x);
                        total[0] = total[0].add(x.value);
                        model.addRow(new Object[]{String.format("%03d", x.id), TelaLivroCaixa.fmt(x.date),
                                x.type, x.employee, TelaLivroCaixa.money(x.value), ""});
                    });
            totalValue.setText(TelaLivroCaixa.money(total[0]));
        };
        apply.addActionListener(e -> refresh.run());
        clear.addActionListener(e -> {
            start.setText("01/11/2026");
            end.setText("30/11/2026");
            type.setSelectedIndex(0);
            employee.setSelectedIndex(0);
            refresh.run();
        });
        ComponentesVisuais.onClick(table, event -> {
            int viewRow = table.rowAtPoint(event.getPoint());
            int col = table.columnAtPoint(event.getPoint());
            if (viewRow < 0 || col != 5) return;
            int row = table.convertRowIndexToModel(viewRow);
            if (row >= visible.size()) return;
            Expense item = visible.get(row);
            int action = actionIndex(table, viewRow, col, event);
            if (action == 0) {
                frame.expenseDialog(item);
            } else if (action == 1 && TelaLivroCaixa.confirm(frame,
                    "Excluir a saída \"" + item.type + "\" de "
                            + TelaLivroCaixa.money(item.value) + "?")) {
                frame.store.removeExpense(item);
                refresh.run();
            }
        });

        refresh.run();
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(ComponentesVisuais.LINE));
        surface.add(scroll);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        ComponentesVisuais.RoundPanel totalBox = new ComponentesVisuais.RoundPanel(13).fill(new Color(244, 248, 255)).shadow(false);
        totalBox.setLayout(new BorderLayout());
        totalBox.setBorder(new EmptyBorder(5, 18, 5, 5));
        JLabel totalLabel = TelaLivroCaixa.title("Total do período", 18);
        totalLabel.setBorder(new EmptyBorder(0, 0, 0, 20));
        totalBox.add(totalLabel);
        ComponentesVisuais.RoundPanel blue = new ComponentesVisuais.RoundPanel(11).fill(new Color(25, 68, 210)).line(null).shadow(false);
        blue.setBorder(new EmptyBorder(8, 20, 8, 20));
        blue.add(totalValue);
        totalBox.add(blue, BorderLayout.EAST);
        footer.add(totalBox);
        surface.add(footer, BorderLayout.SOUTH);

        page.add(surface);
        return page;
    }

    private static ComponentesVisuais.RoundPanel financialCard(String label, String value, Color color, Color fill, String icon) {
        ComponentesVisuais.RoundPanel card = new ComponentesVisuais.RoundPanel(15).fill(fill)
                .line(new Color(color.getRed(), color.getGreen(), color.getBlue(), 105)).shadow(false);
        card.setLayout(new BorderLayout(14, 0));
        card.setBorder(new EmptyBorder(17, 22, 17, 22));
        JLabel glyph = new JLabel(ComponentesVisuais.vectorIcon(icon, 48, color));
        glyph.setBorder(new EmptyBorder(0, 0, 0, 18));
        card.add(glyph, BorderLayout.WEST);
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(TelaLivroCaixa.title(label, 20));
        text.add(Box.createVerticalStrut(8));
        JLabel amount = TelaLivroCaixa.title(value, 28);
        amount.setForeground(color);
        text.add(amount);
        card.add(text);
        return card;
    }

    private static GridBagConstraints constraints() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.weighty = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;
        return gc;
    }

    private static void addFilter(JPanel target, GridBagConstraints base, int x, String label,
                                  JComponent field, double weight) {
        GridBagConstraints gc = (GridBagConstraints) base.clone();
        gc.gridx = x;
        gc.weightx = weight;
        gc.insets = new Insets(0, x == 0 ? 0 : 8, 0, 0);
        JPanel box = new JPanel(new BorderLayout(0, 4));
        box.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 15));
        box.add(l, BorderLayout.NORTH);
        field.setPreferredSize(new Dimension(150, 39));
        box.add(field);
        target.add(box, gc);
    }

    private static LocalDate parseDateOr(String value, LocalDate fallback) {
        try {
            return TelaLivroCaixa.parseDate(value);
        } catch (Exception e) {
            return fallback;
        }
    }

    private static String installmentStatus(Installment x) {
        if (x.cancelled) return "Cancelada";
        if (x.paid.compareTo(x.value) >= 0) return "Pago";
        return x.due.isBefore(REFERENCE_DATE) ? "Vencido" : "Pendente";
    }

    /** Mantém a área clicável exatamente sobre os dois botões desenhados pelo renderer. */
    private static int actionIndex(JTable table, int viewRow, int column, MouseEvent event) {
        Rectangle cell = table.getCellRect(viewRow, column, true);
        TableCellRenderer renderer = table.getCellRenderer(viewRow, column);
        Component rendered = renderer.getTableCellRendererComponent(
                table, table.getValueAt(viewRow, column), false, false, viewRow, column);
        if (!(rendered instanceof Container container)) return -1;
        container.setBounds(0, 0, cell.width, cell.height);
        container.doLayout();
        Point point = new Point(event.getX() - cell.x, event.getY() - cell.y);
        int action = 0;
        for (Component component : container.getComponents()) {
            if (!(component instanceof AbstractButton)) continue;
            if (component.getBounds().contains(point)) return action;
            action++;
        }
        return -1;
    }

    private static final class DualActionRenderer implements TableCellRenderer {
        private final String first, second;
        private final boolean primaryFirst;

        DualActionRenderer(String first, String second, boolean primaryFirst) {
            this.first = first;
            this.second = second;
            this.primaryFirst = primaryFirst;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus,
                                                       int row, int column) {
            JPanel p = new JPanel(new GridBagLayout());
            p.setBorder(new EmptyBorder(4, 7, 4, 7));
            p.setOpaque(true);
            p.setBackground(selected ? table.getSelectionBackground()
                    : (row & 1) == 0 ? Color.WHITE : new Color(249, 251, 255));
            String state = primaryFirst && table.getColumnCount() > 5
                    ? String.valueOf(table.getValueAt(row, 5)) : "";
            boolean paid = "Pago".equals(state);
            boolean cancelled = "Cancelada".equals(state);
            String firstLabel = cancelled ? "Cancelada" : paid ? "Ver pagamentos" : first;
            JButton a = cancelled ? ComponentesVisuais.secondary(firstLabel, "close")
                    : primaryFirst && !paid ? ComponentesVisuais.primary(firstLabel, "wallet")
                    : ComponentesVisuais.secondary(firstLabel, primaryFirst ? "wallet" : "edit");
            a.setEnabled(!cancelled);
            JButton b = primaryFirst ? ComponentesVisuais.secondary(second, "document")
                    : new ComponentesVisuais.RoundButton(second, ComponentesVisuais.vectorIcon("trash", 16, ComponentesVisuais.RED),
                    new Color(255, 235, 237), ComponentesVisuais.RED);
            a.setFont(new Font("Segoe UI", Font.BOLD, 12));
            b.setFont(new Font("Segoe UI", Font.BOLD, 12));
            a.setBorder(new EmptyBorder(5, 9, 5, 9));
            b.setBorder(new EmptyBorder(5, 9, 5, 9));
            ComponentesVisuais.addActionPair(p, a, b);
            return p;
        }
    }
}
