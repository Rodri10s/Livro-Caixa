package br.com.livrocaixa.view;

import br.com.livrocaixa.view.dados.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Tela visual de faturamento com pagamento à vista ou a prazo.
 * Mantém a montagem da interface na View e envia ao Model somente registros
 * já validados, preservando o fluxo MVC do protótipo.
 */
final class TelaFaturamento {
    private TelaFaturamento() {}

    static void show(TelaLivroCaixa.JanelaPrincipal frame, Appointment appointment) {
        JPanel page = TelaLivroCaixa.page();
        ComponentesVisuais.RoundPanel surface = new ComponentesVisuais.RoundPanel(17)
                .fill(new Color(252, 253, 255))
                .line(new Color(174, 198, 234))
                .shadow(false);
        surface.setLayout(new BorderLayout(0, 12));
        surface.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(TelaLivroCaixa.title("Faturamento da consulta", 41), BorderLayout.NORTH);
        top.add(invoiceBanner(appointment));
        surface.add(top, BorderLayout.NORTH);

        JLabel informed = summaryValue(TelaLivroCaixa.money(appointment.value), ComponentesVisuais.INK, 22);
        JLabel remaining = summaryValue(TelaLivroCaixa.money(BigDecimal.ZERO), new Color(6, 128, 28), 24);
        JComponent summary = summaryPanel(appointment.value, informed, remaining);

        JPanel left = new JPanel(new BorderLayout(0, 10));
        left.setOpaque(false);
        JLabel typeTitle = TelaLivroCaixa.title("Tipo de pagamento", 25);
        left.add(typeTitle, BorderLayout.NORTH);

        JPanel leftBody = new JPanel(new BorderLayout(0, 12));
        leftBody.setOpaque(false);
        JPanel segmented = new JPanel(new GridLayout(1, 2));
        segmented.setOpaque(false);
        ComponentesVisuais.SegmentToggle cash = new ComponentesVisuais.SegmentToggle("À vista", 21);
        ComponentesVisuais.SegmentToggle term = new ComponentesVisuais.SegmentToggle("À prazo", 21);
        ButtonGroup group = new ButtonGroup();
        group.add(cash); group.add(term); cash.setSelected(true);
        segmented.add(cash); segmented.add(term);
        segmented.setPreferredSize(new Dimension(0, 50));
        leftBody.add(segmented, BorderLayout.NORTH);

        // CardLayout alterna os dois formulários sem trocar a tela inteira.
        CardLayout modes = new CardLayout();
        JPanel cards = new JPanel(modes);
        cards.setOpaque(false);

        DefaultTableModel cashModel = ComponentesVisuais.readOnlyModel(
                new Object[][]{{"PIX", TelaLivroCaixa.money(new BigDecimal("900.00")), ""}, {"Dinheiro", TelaLivroCaixa.money(new BigDecimal("500.00")), ""}},
                "Forma de pagamento", "Valor", "Ação");
        JTable cashTable = prominentTable(cashModel);
        cashTable.getColumnModel().getColumn(0).setPreferredWidth(330);
        cashTable.getColumnModel().getColumn(1).setPreferredWidth(235);
        cashTable.getColumnModel().getColumn(2).setPreferredWidth(155);
        cashTable.getColumnModel().getColumn(2).setCellRenderer(new RemoveRenderer());
        JPanel cashPanel = new JPanel();
        cashPanel.setOpaque(false);
        cashPanel.setLayout(new BoxLayout(cashPanel, BoxLayout.Y_AXIS));
        JLabel formsTitle = TelaLivroCaixa.title("Formas de pagamento", 25);
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
        cashPanel.add(Box.createVerticalStrut(12));

        JComboBox<String> cashMethod = new JComboBox<>(frame.store.paymentMethods.toArray(String[]::new));
        cashMethod.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        JTextField cashValue = largeField("0,00");
        JButton addMethod = new ComponentesVisuais.RoundButton("Adicionar", ComponentesVisuais.vectorIcon("plus", 22, Color.WHITE), ComponentesVisuais.BLUE, Color.WHITE);
        addMethod.setFont(new Font("Segoe UI", Font.BOLD, 17));
        addMethod.setPreferredSize(new Dimension(165, 48));
        ComponentesVisuais.RoundPanel addPayment = new ComponentesVisuais.RoundPanel(12).fill(new Color(242, 247, 255)).shadow(false);
        addPayment.setLayout(new BorderLayout(12, 0));
        addPayment.setBorder(new EmptyBorder(10, 12, 10, 12));
        addPayment.setAlignmentX(Component.LEFT_ALIGNMENT);
        addPayment.setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));
        JPanel addFields = new JPanel(new GridLayout(1, 2, 10, 0));
        addFields.setOpaque(false);
        addFields.add(fieldBlock("Forma de pagamento", cashMethod));
        addFields.add(fieldBlock("Valor", cashValue));
        addPayment.add(addFields);
        addPayment.add(addMethod, BorderLayout.EAST);
        cashPanel.add(addPayment);
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

        JLabel infoMessage = new JLabel();
        infoMessage.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        infoMessage.setForeground(new Color(20, 38, 70));

        JPanel middle = new JPanel(new BorderLayout(0, 12));
        middle.setOpaque(false);
        middle.add(mainRow);
        middle.add(infoBar(infoMessage), BorderLayout.SOUTH);
        surface.add(middle);

        // Recalcula o resumo quando uma forma de pagamento é removida.
        Runnable refreshCash = () -> {
            BigDecimal total = BigDecimal.ZERO;
            for (int i = 0; i < cashModel.getRowCount(); i++) {
                try { total = total.add(TelaLivroCaixa.parseMoney(String.valueOf(cashModel.getValueAt(i, 1)))); }
                catch (Exception ignored) {}
            }
            informed.setText(TelaLivroCaixa.money(total));
            BigDecimal rest = appointment.value.subtract(total);
            remaining.setText(TelaLivroCaixa.money(rest));
            remaining.setForeground(rest.signum() == 0 ? new Color(6, 128, 28) : ComponentesVisuais.RED);
        };
        ComponentesVisuais.onClick(cashTable, event -> {
            int row = cashTable.rowAtPoint(event.getPoint());
            int col = cashTable.columnAtPoint(event.getPoint());
            if (row >= 0 && col == 2) {
                cashModel.removeRow(cashTable.convertRowIndexToModel(row));
                refreshCash.run();
            }
        });
        addMethod.addActionListener(e -> {
            try {
                BigDecimal value = TelaLivroCaixa.parseMoney(cashValue.getText());
                if (value.signum() <= 0) throw new IllegalArgumentException();
                BigDecimal after = cashTotal(cashModel).add(value);
                if (after.compareTo(appointment.value) > 0) {
                    throw new IllegalArgumentException("O total informado não pode ultrapassar o valor da consulta.");
                }
                cashModel.addRow(new Object[]{cashMethod.getSelectedItem(), TelaLivroCaixa.money(value), ""});
                cashValue.setText("0,00");
                refreshCash.run();
            } catch (IllegalArgumentException ex) {
                TelaLivroCaixa.error(frame, ex.getMessage() == null ? "Informe um valor válido." : ex.getMessage());
            }
        });
        cash.addActionListener(e -> {
            modes.show(cards, "cash");
            infoMessage.setText("Se houver saldo restante, uma parcela de saldo será criada para esta fatura.");
            refreshCash.run();
        });
        term.addActionListener(e -> {
            modes.show(cards, "term");
            if (termComponents.model.getRowCount() == 0) {
                generateTermRows(appointment, termComponents, informed, remaining);
            } else {
                refreshTermSummary(appointment, termComponents.model, informed, remaining);
            }
            infoMessage.setText("Escolha valores iguais ou personalize vencimento e valor diretamente na tabela.");
        });
        termComponents.generate.addActionListener(e -> generateTermRows(appointment, termComponents, informed, remaining));
        termComponents.equal.addActionListener(e -> {
            termComponents.modeHint.setText("Valores calculados automaticamente.");
            generateTermRows(appointment, termComponents, informed, remaining);
            termComponents.model.fireTableDataChanged();
        });
        termComponents.custom.addActionListener(e -> {
            if (termComponents.model.getRowCount() == 0) {
                generateTermRows(appointment, termComponents, informed, remaining);
            }
            termComponents.modeHint.setText("Edite vencimento e valor das parcelas na tabela.");
            termComponents.model.fireTableDataChanged();
            refreshTermSummary(appointment, termComponents.model, informed, remaining);
        });
        termComponents.model.addTableModelListener(e ->
                refreshTermSummary(appointment, termComponents.model, informed, remaining));
        infoMessage.setText("Se houver saldo restante, uma parcela de saldo será criada para esta fatura.");
        refreshCash.run();

        JPanel footer = new JPanel(new BorderLayout(0, 12));
        footer.setOpaque(false);
        footer.add(new JSeparator(), BorderLayout.NORTH);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);
        JButton back = actionButton("Voltar", "back", false, 205);
        JButton save = actionButton("Salvar fatura", "save", true, 310);
        back.addActionListener(e -> frame.showPage("Consultas"));
        save.addActionListener(e -> {
            try {
                if (cash.isSelected()) saveCashInvoice(frame, appointment, cashModel);
                else saveTermInvoice(frame, appointment, termComponents);
                frame.showSuccessPage("Fatura salva com sucesso", "Consultas");
            } catch (IllegalArgumentException ex) {
                TelaLivroCaixa.error(frame, ex.getMessage() == null ? "Revise os dados da fatura." : ex.getMessage());
            }
        });
        actions.add(back); actions.add(save);
        footer.add(actions, BorderLayout.CENTER);
        surface.add(footer, BorderLayout.SOUTH);

        page.add(surface);
        frame.showCustom("Consultas", page);
    }

    private static JComponent invoiceBanner(Appointment a) {
        ComponentesVisuais.RoundPanel banner = new ComponentesVisuais.RoundPanel(12).fill(new Color(229, 239, 255)).shadow(false);
        banner.setBorder(new EmptyBorder(12, 15, 12, 15));
        banner.setLayout(new GridLayout(1, 5));
        banner.add(bannerCell("Consulta nº " + a.id, true));
        banner.add(bannerCell("Cliente:  " + a.client, false));
        banner.add(bannerCell("Tipo:  " + a.type, false));
        banner.add(bannerCell("Data:  " + TelaLivroCaixa.fmt(a.date), false));
        banner.add(bannerCell("Valor total:  " + TelaLivroCaixa.money(a.value), false));
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
        JTable table = ComponentesVisuais.modernTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        table.setRowHeight(50);
        table.getTableHeader().setPreferredSize(new Dimension(0, 50));
        table.getTableHeader().setDefaultRenderer(ComponentesVisuais.headerRenderer(20));
        return table;
    }

    private static JComponent summaryPanel(BigDecimal total, JLabel informed, JLabel remaining) {
        ComponentesVisuais.RoundPanel card = new ComponentesVisuais.RoundPanel(15)
                .fill(new Color(248, 250, 255))
                .line(new Color(142, 172, 220))
                .shadow(false);
        card.setPreferredSize(new Dimension(425, 286));
        card.setLayout(new BorderLayout());
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(225, 236, 252));
        header.setBorder(new EmptyBorder(12, 17, 12, 17));
        header.add(TelaLivroCaixa.title("Resumo", 30));
        card.add(header, BorderLayout.NORTH);
        JPanel rows = new JPanel();
        rows.setOpaque(false);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setBorder(new EmptyBorder(9, 17, 12, 17));
        addSummaryLine(rows, "Valor da consulta", summaryValue(TelaLivroCaixa.money(total), ComponentesVisuais.INK, 22));
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

    private static JComponent infoBar(JLabel text) {
        ComponentesVisuais.RoundPanel info = new ComponentesVisuais.RoundPanel(12).fill(new Color(228, 239, 255)).line(new Color(123, 167, 232)).shadow(false);
        info.setLayout(new BorderLayout(12, 0));
        info.setBorder(new EmptyBorder(10, 16, 10, 16));
        info.add(new JLabel(ComponentesVisuais.vectorIcon("info", 29, new Color(27, 100, 229))), BorderLayout.WEST);
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
        JTextField entryDate = largeField(TelaLivroCaixa.fmt(appointment.date));
        JTextField firstDue = largeField(TelaLivroCaixa.fmt(appointment.date.plusDays(30)));
        JTextField count = largeField("5");
        JComboBox<String> period = new JComboBox<>(new String[]{"Mensal", "Quinzenal", "Semanal"});
        period.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        JPanel valueMode = new JPanel(new GridLayout(1, 2));
        valueMode.setOpaque(false);
        ComponentesVisuais.SegmentToggle equal = new ComponentesVisuais.SegmentToggle("Iguais");
        ComponentesVisuais.SegmentToggle custom = new ComponentesVisuais.SegmentToggle("Personalizados");
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
        DefaultTableModel model = ComponentesVisuais.editableModel(
                new String[]{"Parcela", "Tipo", "Vencimento", "Valor", "Status"},
                (firstValue, column) -> custom.isSelected()
                        && !"Entrada".equals(String.valueOf(firstValue))
                        && (column == 2 || column == 3));
        JTable table = ComponentesVisuais.modernTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setRowHeight(32);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(146, 177, 224)));
        JButton generate = new ComponentesVisuais.RoundButton("Gerar parcelas", ComponentesVisuais.vectorIcon("chart", 23, Color.WHITE), ComponentesVisuais.BLUE, Color.WHITE);
        generate.setFont(new Font("Segoe UI", Font.BOLD, 16));
        generate.setPreferredSize(new Dimension(186, 38));
        JPanel preview = new JPanel(new BorderLayout(0, 5));
        preview.setOpaque(false);
        JPanel previewHeader = new JPanel(new BorderLayout(12, 0));
        previewHeader.setOpaque(false);
        previewHeader.add(TelaLivroCaixa.title("Prévia das parcelas", 21));
        JLabel modeHint = new JLabel("Valores calculados automaticamente.");
        modeHint.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        modeHint.setForeground(ComponentesVisuais.MUTED);
        modeHint.setHorizontalAlignment(SwingConstants.RIGHT);
        previewHeader.add(modeHint, BorderLayout.CENTER);
        previewHeader.add(generate, BorderLayout.EAST);
        preview.add(previewHeader, BorderLayout.NORTH);
        preview.add(scroll);
        panel.add(preview);
        return new TermComponents(panel, entry, entryDate, firstDue, count, period, model, table,
                equal, custom, modeHint, generate);
    }

    private static JTextField largeField(String value) {
        JTextField field = TelaLivroCaixa.modernField(value);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        field.setPreferredSize(new Dimension(130, 38));
        // Estes campos ficam em linhas compactas; margens menores evitam que a
        // linha de base seja recortada e pareça encostada na parte inferior.
        field.setBorder(new CompoundBorder(
                new LineBorder(new Color(163, 181, 211)),
                new EmptyBorder(4, 10, 4, 10)));
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

    /** Gera a prévia; os registros só são criados depois da validação ao salvar. */
    private static void generateTermRows(Appointment appointment, TermComponents c, JLabel informed, JLabel remaining) {
        try {
            BigDecimal entry = TelaLivroCaixa.parseMoney(c.entry.getText());
            int count = Integer.parseInt(c.count.getText().trim());
            LocalDate first = TelaLivroCaixa.parseDate(c.firstDue.getText());
            LocalDate entryDate = TelaLivroCaixa.parseDate(c.entryDate.getText());
            if (entry.signum() < 0 || entry.compareTo(appointment.value) >= 0) {
                throw new IllegalArgumentException("A entrada deve ser menor que o valor da consulta.");
            }
            if (count < 1 || count > 60) {
                throw new IllegalArgumentException("Informe entre 1 e 60 parcelas.");
            }
            c.model.setRowCount(0);
            if (entry.signum() > 0) {
                c.model.addRow(new Object[]{"Entrada", "Entrada", TelaLivroCaixa.fmt(entryDate), TelaLivroCaixa.money(entry), "Pendente"});
            }
            BigDecimal rest = appointment.value.subtract(entry);
            BigDecimal each = rest.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
            for (int i = 1; i <= count; i++) {
                BigDecimal value = i == count ? rest.subtract(each.multiply(BigDecimal.valueOf(count - 1))) : each;
                LocalDate due = installmentDate(first, String.valueOf(c.period.getSelectedItem()), i - 1);
                c.model.addRow(new Object[]{i + "/" + count, c.period.getSelectedItem(), TelaLivroCaixa.fmt(due), TelaLivroCaixa.money(value), "Pendente"});
            }
            c.modeHint.setText(c.custom.isSelected()
                    ? "Edite vencimento e valor das parcelas na tabela."
                    : "Valores calculados automaticamente.");
            refreshTermSummary(appointment, c.model, informed, remaining);
        } catch (Exception ex) {
            informed.setText("Dados inválidos");
            remaining.setText("—");
            remaining.setForeground(ComponentesVisuais.RED);
            if (ex instanceof IllegalArgumentException && ex.getMessage() != null) {
                c.modeHint.setText(ex.getMessage());
            } else {
                c.modeHint.setText("Revise entrada, datas e número de parcelas.");
            }
        }
    }

    private static LocalDate installmentDate(LocalDate first, String period, int offset) {
        return switch (period) {
            case "Semanal" -> first.plusWeeks(offset);
            case "Quinzenal" -> first.plusDays(15L * offset);
            default -> first.plusMonths(offset);
        };
    }

    private static void refreshTermSummary(Appointment appointment, DefaultTableModel model,
                                           JLabel informed, JLabel remaining) {
        BigDecimal total = BigDecimal.ZERO;
        boolean valid = model.getRowCount() > 0;
        for (int row = 0; row < model.getRowCount(); row++) {
            try {
                BigDecimal value = TelaLivroCaixa.parseMoney(String.valueOf(model.getValueAt(row, 3)));
                if (value.signum() <= 0) valid = false;
                total = total.add(value);
                TelaLivroCaixa.parseDate(String.valueOf(model.getValueAt(row, 2)));
            } catch (Exception error) {
                valid = false;
            }
        }
        informed.setText(valid ? TelaLivroCaixa.money(total) : "Revise a tabela");
        BigDecimal rest = appointment.value.subtract(total);
        remaining.setText(valid ? TelaLivroCaixa.money(rest) : "—");
        remaining.setForeground(valid && rest.signum() == 0 ? new Color(6, 128, 28) : ComponentesVisuais.RED);
    }

    private static BigDecimal cashTotal(DefaultTableModel model) {
        BigDecimal total = BigDecimal.ZERO;
        for (int row = 0; row < model.getRowCount(); row++) {
            total = total.add(TelaLivroCaixa.parseMoney(String.valueOf(model.getValueAt(row, 1))));
        }
        return total;
    }

    private static void saveCashInvoice(TelaLivroCaixa.JanelaPrincipal frame, Appointment appointment,
                                        DefaultTableModel model) {
        if (model.getRowCount() == 0) {
            throw new IllegalArgumentException("Adicione ao menos uma forma de pagamento.");
        }
        List<Payment> payments = new ArrayList<>();
        int paymentId = frame.store.nextPayment;
        BigDecimal total = BigDecimal.ZERO;
        for (int row = 0; row < model.getRowCount(); row++) {
            BigDecimal value;
            try {
                value = TelaLivroCaixa.parseMoney(String.valueOf(model.getValueAt(row, 1)));
            } catch (Exception error) {
                throw new IllegalArgumentException("Revise os valores das formas de pagamento.");
            }
            if (value.signum() <= 0) throw new IllegalArgumentException("Os pagamentos devem ser maiores que zero.");
            total = total.add(value);
            payments.add(new Payment(paymentId++, appointment.id, appointment.client, "À vista",
                    appointment.date, value, String.valueOf(model.getValueAt(row, 0)), BigDecimal.ZERO, false));
        }
        if (total.compareTo(appointment.value) > 0) {
            throw new IllegalArgumentException("O total informado excede o valor da consulta.");
        }
        List<Installment> installments = new ArrayList<>();
        BigDecimal rest = appointment.value.subtract(total);
        if (rest.signum() > 0) {
            installments.add(new Installment(frame.store.nextInstallment, appointment.id, appointment.client,
                    "Saldo", appointment.date, rest, BigDecimal.ZERO, false));
        }
        frame.store.recordInvoice(appointment, payments, installments);
    }

    private static void saveTermInvoice(TelaLivroCaixa.JanelaPrincipal frame, Appointment appointment,
                                        TermComponents components) {
        if (components.table.isEditing() && !components.table.getCellEditor().stopCellEditing()) {
            throw new IllegalArgumentException("Conclua a edição da parcela antes de salvar.");
        }
        if (components.model.getRowCount() == 0) {
            throw new IllegalArgumentException("Gere as parcelas antes de salvar.");
        }

        List<Payment> payments = new ArrayList<>();
        List<Installment> installments = new ArrayList<>();
        int paymentId = frame.store.nextPayment;
        int installmentId = frame.store.nextInstallment;
        BigDecimal total = BigDecimal.ZERO;
        LocalDate lastDue = null;

        for (int row = 0; row < components.model.getRowCount(); row++) {
            String label = String.valueOf(components.model.getValueAt(row, 0));
            BigDecimal value;
            LocalDate date;
            try {
                value = TelaLivroCaixa.parseMoney(String.valueOf(components.model.getValueAt(row, 3)));
                date = TelaLivroCaixa.parseDate(String.valueOf(components.model.getValueAt(row, 2)));
            } catch (Exception error) {
                throw new IllegalArgumentException("Revise os vencimentos e valores personalizados.");
            }
            if (value.signum() <= 0) throw new IllegalArgumentException("Todas as parcelas devem ter valor maior que zero.");
            total = total.add(value);
            if ("Entrada".equals(label)) {
                payments.add(new Payment(paymentId++, appointment.id, appointment.client, "Entrada", date,
                        value, "PIX", BigDecimal.ZERO, false));
            } else {
                if (lastDue != null && date.isBefore(lastDue)) {
                    throw new IllegalArgumentException("Os vencimentos devem estar em ordem cronológica.");
                }
                lastDue = date;
                installments.add(new Installment(installmentId++, appointment.id, appointment.client, label,
                        date, value, BigDecimal.ZERO, false));
            }
        }
        if (installments.isEmpty()) throw new IllegalArgumentException("A fatura a prazo precisa de ao menos uma parcela.");
        if (total.compareTo(appointment.value) != 0) {
            throw new IllegalArgumentException("Entrada e parcelas devem totalizar " + TelaLivroCaixa.money(appointment.value) + ".");
        }
        frame.store.recordInvoice(appointment, payments, installments);
    }

    private static JButton actionButton(String text, String icon, boolean primary, int width) {
        JButton button = new ComponentesVisuais.RoundButton(
                text,
                ComponentesVisuais.vectorIcon(icon, 27, primary ? Color.WHITE : ComponentesVisuais.INK),
                primary ? ComponentesVisuais.BLUE : new Color(235, 241, 251),
                primary ? Color.WHITE : ComponentesVisuais.INK
        );
        button.setFont(new Font("Segoe UI", Font.BOLD, 21));
        button.setPreferredSize(new Dimension(width, 56));
        return button;
    }

    private record TermComponents(JPanel panel, JTextField entry, JTextField entryDate, JTextField firstDue,
                                  JTextField count, JComboBox<String> period, DefaultTableModel model, JTable table,
                                  ComponentesVisuais.SegmentToggle equal, ComponentesVisuais.SegmentToggle custom, JLabel modeHint,
                                  JButton generate) {}

    private static final class RemoveRenderer implements TableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                                 boolean focus, int row, int column) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6));
            panel.setOpaque(true);
            panel.setBackground(selected ? table.getSelectionBackground() : (row & 1) == 0 ? Color.WHITE : new Color(249, 251, 255));
            JButton remove = new ComponentesVisuais.RoundButton("Remover", ComponentesVisuais.vectorIcon("trash", 16, ComponentesVisuais.RED),
                    new Color(255, 238, 240), ComponentesVisuais.RED);
            remove.setFont(new Font("Segoe UI", Font.PLAIN, 17));
            remove.setBorder(new EmptyBorder(5, 17, 5, 17));
            panel.add(remove);
            return panel;
        }
    }
}
