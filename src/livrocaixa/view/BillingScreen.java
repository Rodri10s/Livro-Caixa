package livrocaixa.view;

import livrocaixa.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Tela visual de faturamento com os dois caminhos apresentados no protótipo:
 * pagamento à vista, com uma ou mais formas, e pagamento a prazo, com prévia
 * das parcelas. Nenhuma operação desta classe persiste dados.
 */
final class BillingScreen {
    private BillingScreen() {}

    static void show(LivroCaixaView.MainFrame frame, Appointment appointment) {
        JPanel page = LivroCaixaView.page();
        UiKit.RoundPanel surface = new UiKit.RoundPanel(17)
                .fill(new Color(252, 253, 255))
                .line(new Color(174, 198, 234))
                .shadow(false);
        surface.setLayout(new BorderLayout(0, 12));
        surface.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(LivroCaixaView.title("Faturamento da consulta", 41), BorderLayout.NORTH);
        top.add(invoiceBanner(appointment));
        surface.add(top, BorderLayout.NORTH);

        JLabel informed = summaryValue(LivroCaixaView.money(appointment.value), UiKit.INK, 22);
        JLabel remaining = summaryValue(LivroCaixaView.money(BigDecimal.ZERO), new Color(6, 128, 28), 24);
        JComponent summary = summaryPanel(appointment.value, informed, remaining);

        JPanel left = new JPanel(new BorderLayout(0, 10));
        left.setOpaque(false);
        JLabel typeTitle = LivroCaixaView.title("Tipo de pagamento", 25);
        left.add(typeTitle, BorderLayout.NORTH);

        JPanel leftBody = new JPanel(new BorderLayout(0, 12));
        leftBody.setOpaque(false);
        JPanel segmented = new JPanel(new GridLayout(1, 2));
        segmented.setOpaque(false);
        SegmentToggle cash = new SegmentToggle("À vista");
        SegmentToggle term = new SegmentToggle("À prazo");
        ButtonGroup group = new ButtonGroup();
        group.add(cash); group.add(term); cash.setSelected(true);
        segmented.add(cash); segmented.add(term);
        segmented.setPreferredSize(new Dimension(0, 50));
        leftBody.add(segmented, BorderLayout.NORTH);

        // CardLayout alterna os dois formulários sem trocar a tela inteira.
        CardLayout modes = new CardLayout();
        JPanel cards = new JPanel(modes);
        cards.setOpaque(false);

        DefaultTableModel cashModel = new DefaultTableModel(
                new Object[][]{{"PIX", LivroCaixaView.money(new BigDecimal("900.00")), ""}, {"Dinheiro", LivroCaixaView.money(new BigDecimal("500.00")), ""}},
                new String[]{"Forma de pagamento", "Valor", "Ação"}) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable cashTable = prominentTable(cashModel);
        cashTable.getColumnModel().getColumn(0).setPreferredWidth(330);
        cashTable.getColumnModel().getColumn(1).setPreferredWidth(235);
        cashTable.getColumnModel().getColumn(2).setPreferredWidth(155);
        cashTable.getColumnModel().getColumn(2).setCellRenderer(new RemoveRenderer());
        JPanel cashPanel = new JPanel();
        cashPanel.setOpaque(false);
        cashPanel.setLayout(new BoxLayout(cashPanel, BoxLayout.Y_AXIS));
        JLabel formsTitle = LivroCaixaView.title("Formas de pagamento", 25);
        formsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        cashPanel.add(formsTitle);
        cashPanel.add(Box.createVerticalStrut(9));
        JScrollPane cashScroll = new JScrollPane(cashTable);
        cashScroll.setBorder(new LineBorder(new Color(146, 177, 224), 1, true));
        cashScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        Dimension tableSize = new Dimension(Integer.MAX_VALUE, 154);
        cashScroll.setPreferredSize(tableSize);
        cashScroll.setMaximumSize(tableSize);
        cashScroll.setMinimumSize(new Dimension(200, 154));
        cashPanel.add(cashScroll);
        cashPanel.add(Box.createVerticalStrut(14));
        JButton addMethod = new UiKit.RoundButton("Adicionar forma de pagamento", UiKit.vectorIcon("plus", 27, Color.WHITE), UiKit.BLUE, Color.WHITE);
        addMethod.setFont(new Font("Segoe UI", Font.BOLD, 20));
        addMethod.setPreferredSize(new Dimension(390, 56));
        addMethod.setMaximumSize(new Dimension(390, 56));
        addMethod.setAlignmentX(Component.LEFT_ALIGNMENT);
        cashPanel.add(addMethod);
        cards.add(cashPanel, "cash");

        TermComponents termComponents = termPanel(appointment);
        cards.add(termComponents.panel, "term");
        leftBody.add(cards);
        left.add(leftBody);

        JPanel mainRow = new JPanel(new BorderLayout(18, 0));
        mainRow.setOpaque(false);
        mainRow.add(left, BorderLayout.CENTER);
        JPanel summarySlot = new JPanel(new BorderLayout());
        summarySlot.setOpaque(false);
        summarySlot.setPreferredSize(new Dimension(400, 0));
        summarySlot.setMinimumSize(new Dimension(340, 0));
        summarySlot.add(summary, BorderLayout.NORTH);
        mainRow.add(summarySlot, BorderLayout.EAST);

        JPanel middle = new JPanel(new BorderLayout(0, 12));
        middle.setOpaque(false);
        middle.add(mainRow);
        middle.add(infoBar(appointment.date), BorderLayout.SOUTH);
        surface.add(middle);

        // Recalcula o resumo quando uma forma de pagamento é removida.
        Runnable refreshCash = () -> {
            BigDecimal total = BigDecimal.ZERO;
            for (int i = 0; i < cashModel.getRowCount(); i++) {
                try { total = total.add(LivroCaixaView.parseMoney(String.valueOf(cashModel.getValueAt(i, 1)))); }
                catch (Exception ignored) {}
            }
            informed.setText(LivroCaixaView.money(total));
            BigDecimal rest = appointment.value.subtract(total);
            remaining.setText(LivroCaixaView.money(rest));
            remaining.setForeground(rest.signum() == 0 ? new Color(6, 128, 28) : UiKit.RED);
        };
        cashTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = cashTable.rowAtPoint(e.getPoint());
                int col = cashTable.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 2) {
                    cashModel.removeRow(cashTable.convertRowIndexToModel(row));
                    refreshCash.run();
                }
            }
        });
        addMethod.addActionListener(e -> LivroCaixaView.info(frame, "No protótipo visual, esta opção adiciona outra forma de pagamento."));
        cash.addActionListener(e -> { modes.show(cards, "cash"); refreshCash.run(); });
        term.addActionListener(e -> {
            modes.show(cards, "term");
            generateTermRows(appointment, termComponents, informed, remaining);
        });
        termComponents.generate.addActionListener(e -> generateTermRows(appointment, termComponents, informed, remaining));
        refreshCash.run();

        JPanel footer = new JPanel(new BorderLayout(0, 12));
        footer.setOpaque(false);
        footer.add(new JSeparator(), BorderLayout.NORTH);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);
        JButton back = actionButton("Voltar", "swap", false, 205);
        JButton save = actionButton("Salvar fatura", "document", true, 310);
        back.addActionListener(e -> frame.showPage("Consultas"));
        save.addActionListener(e -> frame.showSuccessPage("Fatura salva com sucesso", "Consultas"));
        actions.add(back); actions.add(save);
        footer.add(actions, BorderLayout.CENTER);
        surface.add(footer, BorderLayout.SOUTH);

        page.add(surface);
        frame.showCustom("Consultas", page);
    }

    private static JComponent invoiceBanner(Appointment a) {
        UiKit.RoundPanel banner = new UiKit.RoundPanel(12).fill(new Color(229, 239, 255)).shadow(false);
        banner.setBorder(new EmptyBorder(12, 15, 12, 15));
        banner.setLayout(new GridLayout(1, 5));
        banner.add(bannerCell("Consulta nº " + a.id, true));
        banner.add(bannerCell("Cliente:  " + a.client, false));
        banner.add(bannerCell("Tipo:  " + a.type, false));
        banner.add(bannerCell("Data:  " + LivroCaixaView.fmt(a.date), false));
        banner.add(bannerCell("Valor total:  " + LivroCaixaView.money(a.value), false));
        banner.setPreferredSize(new Dimension(0, 60));
        return banner;
    }

    private static JLabel bannerCell(String text, boolean strong) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", strong ? Font.BOLD : Font.PLAIN, strong ? 21 : 17));
        label.setForeground(new Color(8, 22, 45));
        label.setBorder(new EmptyBorder(0, 6, 0, 6));
        return label;
    }

    private static JTable prominentTable(DefaultTableModel model) {
        JTable table = UiKit.modernTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        table.setRowHeight(50);
        table.getTableHeader().setPreferredSize(new Dimension(0, 50));
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            { setHorizontalAlignment(CENTER); setFont(new Font("Segoe UI", Font.BOLD, 20)); setBorder(new LineBorder(UiKit.LINE)); }
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                setBackground(new Color(222, 234, 250));
                setForeground(UiKit.INK);
                return this;
            }
        });
        return table;
    }

    private static JComponent summaryPanel(BigDecimal total, JLabel informed, JLabel remaining) {
        UiKit.RoundPanel card = new UiKit.RoundPanel(15)
                .fill(new Color(248, 250, 255))
                .line(new Color(142, 172, 220))
                .shadow(false);
        card.setPreferredSize(new Dimension(425, 286));
        card.setLayout(new BorderLayout());
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(225, 236, 252));
        header.setBorder(new EmptyBorder(12, 17, 12, 17));
        header.add(LivroCaixaView.title("Resumo", 30));
        card.add(header, BorderLayout.NORTH);
        JPanel rows = new JPanel();
        rows.setOpaque(false);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setBorder(new EmptyBorder(9, 17, 12, 17));
        addSummaryLine(rows, "Valor da consulta", summaryValue(LivroCaixaView.money(total), UiKit.INK, 22));
        addSummaryLine(rows, "Total informado", informed);
        addSummaryLine(rows, "Saldo restante", remaining);
        card.add(rows);
        return card;
    }

    private static void addSummaryLine(JPanel rows, String label, JLabel value) {
        JPanel line = new JPanel(new BorderLayout(10, 0));
        line.setOpaque(false);
        JLabel name = new JLabel(label);
        name.setFont(new Font("Segoe UI", label.startsWith("Saldo") ? Font.BOLD : Font.PLAIN, 20));
        line.add(name);
        line.add(value, BorderLayout.EAST);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 49));
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        rows.add(line);
        JSeparator separator = new JSeparator();
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        rows.add(separator);
    }

    private static JLabel summaryValue(String text, Color color, int size) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, size));
        label.setForeground(color);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        return label;
    }

    private static JComponent infoBar(LocalDate date) {
        UiKit.RoundPanel info = new UiKit.RoundPanel(12).fill(new Color(228, 239, 255)).line(new Color(123, 167, 232)).shadow(false);
        info.setLayout(new BorderLayout(12, 0));
        info.setBorder(new EmptyBorder(10, 16, 10, 16));
        info.add(new JLabel(UiKit.vectorIcon("info", 29, new Color(27, 100, 229))), BorderLayout.WEST);
        JLabel text = new JLabel("Se houver saldo restante, será criada uma parcela com vencimento em " + LivroCaixaView.fmt(date) + ".");
        text.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        text.setForeground(new Color(20, 38, 70));
        info.add(text);
        info.setPreferredSize(new Dimension(0, 50));
        return info;
    }

    private static TermComponents termPanel(Appointment appointment) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        JPanel fields = new JPanel(new GridLayout(2, 3, 10, 4));
        fields.setOpaque(false);
        JTextField entry = largeField("200,00");
        JTextField entryDate = largeField(LivroCaixaView.fmt(appointment.date));
        JTextField firstDue = largeField(LivroCaixaView.fmt(appointment.date.plusDays(30)));
        JTextField count = largeField("4");
        JComboBox<String> period = new JComboBox<>(new String[]{"Mensal", "Quinzenal", "Semanal"});
        period.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        JPanel valueMode = new JPanel(new GridLayout(1, 2));
        valueMode.setOpaque(false);
        SegmentToggle equal = new SegmentToggle("Iguais");
        SegmentToggle custom = new SegmentToggle("Personalizados");
        equal.setFont(new Font("Segoe UI", Font.BOLD, 17));
        equal.setBorder(new EmptyBorder(5, 6, 5, 6));
        custom.setFont(new Font("Segoe UI", Font.BOLD, 15));
        custom.setBorder(new EmptyBorder(5, 3, 5, 3));
        ButtonGroup values = new ButtonGroup(); values.add(equal); values.add(custom); equal.setSelected(true);
        valueMode.add(equal); valueMode.add(custom);
        fields.add(fieldBlock("Entrada", entry));
        fields.add(fieldBlock("Data da entrada", entryDate));
        fields.add(fieldBlock("Primeiro vencimento", firstDue));
        fields.add(fieldBlock("Número de parcelas", count));
        fields.add(fieldBlock("Periodicidade", period));
        fields.add(fieldBlock("Valores das parcelas", valueMode));
        panel.add(fields, BorderLayout.NORTH);
        DefaultTableModel model = new DefaultTableModel(new String[]{"Parcela", "Tipo", "Vencimento", "Valor", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = UiKit.modernTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setRowHeight(28);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(146, 177, 224)));
        JButton generate = new UiKit.RoundButton("Gerar parcelas", UiKit.vectorIcon("chart", 23, Color.WHITE), UiKit.BLUE, Color.WHITE);
        generate.setFont(new Font("Segoe UI", Font.BOLD, 16));
        generate.setPreferredSize(new Dimension(186, 38));
        JPanel preview = new JPanel(new BorderLayout(0, 5));
        preview.setOpaque(false);
        JPanel previewHeader = new JPanel(new BorderLayout(12, 0));
        previewHeader.setOpaque(false);
        previewHeader.add(LivroCaixaView.title("Prévia das parcelas", 21));
        previewHeader.add(generate, BorderLayout.EAST);
        preview.add(previewHeader, BorderLayout.NORTH);
        preview.add(scroll);
        panel.add(preview);
        return new TermComponents(panel, entry, entryDate, firstDue, count, period, model, generate);
    }

    private static JTextField largeField(String value) {
        JTextField field = LivroCaixaView.modernField(value);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        field.setPreferredSize(new Dimension(130, 34));
        return field;
    }

    private static JPanel fieldBlock(String label, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(0, 2));
        panel.setOpaque(false);
        JLabel name = new JLabel(label);
        name.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(name, BorderLayout.NORTH);
        panel.add(component);
        return panel;
    }

    /** Gera apenas a prévia visual das parcelas; não cria registros persistentes. */
    private static void generateTermRows(Appointment appointment, TermComponents c, JLabel informed, JLabel remaining) {
        try {
            BigDecimal entry = LivroCaixaView.parseMoney(c.entry.getText());
            int count = Integer.parseInt(c.count.getText().trim());
            LocalDate first = LivroCaixaView.parseDate(c.firstDue.getText());
            c.model.setRowCount(0);
            c.model.addRow(new Object[]{"Entrada", "Entrada", c.entryDate.getText(), LivroCaixaView.money(entry), "Pendente"});
            BigDecimal rest = appointment.value.subtract(entry);
            BigDecimal each = rest.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
            for (int i = 1; i <= count; i++) {
                BigDecimal value = i == count ? rest.subtract(each.multiply(BigDecimal.valueOf(count - 1))) : each;
                c.model.addRow(new Object[]{i + "/" + count, c.period.getSelectedItem(), LivroCaixaView.fmt(first.plusMonths(i - 1)), LivroCaixaView.money(value), "Pendente"});
            }
            informed.setText(LivroCaixaView.money(appointment.value));
            remaining.setText(LivroCaixaView.money(BigDecimal.ZERO));
            remaining.setForeground(new Color(6, 128, 28));
        } catch (Exception ex) {
            informed.setText("Revise os dados");
            remaining.setText("—");
            remaining.setForeground(UiKit.RED);
        }
    }

    private static JButton actionButton(String text, String icon, boolean primary, int width) {
        JButton button = new UiKit.RoundButton(
                text,
                UiKit.vectorIcon(icon, 27, primary ? Color.WHITE : UiKit.INK),
                primary ? UiKit.BLUE : new Color(235, 241, 251),
                primary ? Color.WHITE : UiKit.INK
        );
        button.setFont(new Font("Segoe UI", Font.BOLD, 21));
        button.setPreferredSize(new Dimension(width, 56));
        return button;
    }

    private record TermComponents(JPanel panel, JTextField entry, JTextField entryDate, JTextField firstDue,
                                  JTextField count, JComboBox<String> period, DefaultTableModel model, JButton generate) {}

    private static final class SegmentToggle extends JToggleButton {
        SegmentToggle(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 21));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setBorder(new EmptyBorder(10, 18, 10, 18));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addActionListener(e -> repaint());
        }
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            UiKit.antialias(g);
            g.setColor(isSelected() ? UiKit.BLUE : new Color(235, 241, 251));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 13, 13);
            g.dispose();
            setForeground(isSelected() ? Color.WHITE : UiKit.INK);
            super.paintComponent(graphics);
        }
    }

    private static final class RemoveRenderer implements TableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                                 boolean focus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6));
            panel.setOpaque(true);
            panel.setBackground(selected ? table.getSelectionBackground() : (row & 1) == 0 ? Color.WHITE : new Color(249, 251, 255));
            JButton remove = new UiKit.RoundButton("Remover", null, new Color(255, 242, 242), new Color(213, 28, 35));
            remove.setFont(new Font("Segoe UI", Font.PLAIN, 17));
            remove.setBorder(new EmptyBorder(5, 17, 5, 17));
            panel.add(remove);
            return panel;
        }
    }
}
