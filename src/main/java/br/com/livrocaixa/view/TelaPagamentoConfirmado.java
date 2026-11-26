package br.com.livrocaixa.view;

import br.com.livrocaixa.view.dados.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Tela visual do fluxo 10: confirmação, resumo e desfazer baixa. */
final class TelaPagamentoConfirmado {
    private static final LocalDate REFERENCE_DATE = LivroCaixaModel.DEMO_DATE;

    private TelaPagamentoConfirmado() {}

    static void show(TelaLivroCaixa.JanelaPrincipal frame, Installment item) {
        JPanel page = TelaLivroCaixa.page();
        ComponentesVisuais.RoundPanel surface = new ComponentesVisuais.RoundPanel(16).fill(Color.WHITE);
        surface.setLayout(new BorderLayout(0, 12));
        surface.setBorder(new EmptyBorder(15, 18, 15, 18));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(TelaLivroCaixa.title("Pagamento confirmado", 29));
        top.add(Box.createVerticalStrut(10));
        ComponentesVisuais.RoundPanel success = new ComponentesVisuais.RoundPanel(13).fill(new Color(224, 249, 222)).line(new Color(76, 190, 78)).shadow(false);
        success.setLayout(new BorderLayout());
        success.setBorder(new EmptyBorder(13, 18, 13, 18));
        JLabel successText = new JLabel("Pagamento registrado com sucesso", ComponentesVisuais.vectorIcon("check", 42, new Color(7, 132, 31)), SwingConstants.LEFT);
        successText.setIconTextGap(14);
        successText.setFont(new Font("Segoe UI", Font.BOLD, 25));
        successText.setForeground(new Color(7, 117, 26));
        success.add(successText);
        top.add(success);
        top.add(Box.createVerticalStrut(10));
        top.add(detailBanner(item));
        surface.add(top, BorderLayout.NORTH);

        List<Payment> payments = new ArrayList<>();
        frame.store.payments.stream()
                .filter(p -> p.appointmentId == item.appointmentId && p.label.equals(item.label) && !p.reversed)
                .forEach(payments::add);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1;
        gc.gridx = 0;
        gc.weightx = 2.1;
        gc.insets = new Insets(0, 0, 0, 15);
        center.add(historyPanel(frame, item, payments), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        gc.insets = new Insets(0, 0, 0, 0);
        center.add(summaryPanel(item, payments), gc);
        surface.add(center);

        JPanel bottom = new JPanel(new BorderLayout(12, 10));
        bottom.setOpaque(false);
        ComponentesVisuais.RoundPanel hint = new ComponentesVisuais.RoundPanel(12).fill(new Color(231, 240, 255)).shadow(false);
        hint.setBorder(new EmptyBorder(10, 14, 10, 14));
        hint.setLayout(new BorderLayout(10, 0));
        hint.add(new JLabel(ComponentesVisuais.vectorIcon("info", 25, ComponentesVisuais.BLUE)), BorderLayout.WEST);
        JLabel hintText = new JLabel("Desfazer baixa reverte o pagamento e o desconto associados.");
        hintText.setFont(new Font("Segoe UI", Font.BOLD, 16));
        hint.add(hintText);
        bottom.add(hint, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton invoice = ComponentesVisuais.secondary("Ver fatura", "document");
        JButton back = ComponentesVisuais.primary("Voltar para contas a receber", "back");
        invoice.addActionListener(e -> showInvoice(frame, item.appointmentId));
        back.addActionListener(e -> frame.showPage("Contas a receber"));
        actions.add(invoice);
        actions.add(back);
        bottom.add(actions, BorderLayout.SOUTH);
        surface.add(bottom, BorderLayout.SOUTH);
        page.add(surface);
        frame.showCustom("Contas a receber", page);
    }

    /** Exibe a situação completa da fatura sem abrir janelas auxiliares. */
    static void showInvoice(TelaLivroCaixa.JanelaPrincipal frame, int invoiceId) {
        List<Installment> installments = frame.store.installmentsForInvoice(invoiceId);
        List<Payment> payments = frame.store.paymentsForInvoice(invoiceId);
        Appointment appointment = frame.store.appointmentForInvoice(invoiceId);
        if (installments.isEmpty() && payments.isEmpty() && appointment == null) {
            TelaLivroCaixa.error(frame, "Fatura não encontrada.");
            return;
        }

        String client = appointment != null ? appointment.client
                : !installments.isEmpty() ? installments.get(0).client
                : payments.get(0).client;
        BigDecimal total = frame.store.invoiceTotal(invoiceId);
        BigDecimal received = frame.store.invoiceReceived(invoiceId);
        BigDecimal discounts = frame.store.invoiceDiscounts(invoiceId);
        BigDecimal balance = frame.store.invoiceBalance(invoiceId);
        BigDecimal entry = payments.stream()
                .filter(payment -> !payment.reversed && "Entrada".equalsIgnoreCase(payment.label))
                .map(payment -> payment.value)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int declaredInstallments = installments.size();
        long paidCount = installments.stream().filter(item -> !item.cancelled && item.isPaid()).count();
        long openCount = installments.stream().filter(item -> !item.cancelled && !item.isPaid()).count();
        long cancelledCount = installments.stream().filter(item -> item.cancelled).count();
        long overdueCount = installments.stream()
                .filter(item -> !item.cancelled && !item.isPaid() && item.due.isBefore(REFERENCE_DATE))
                .count();
        LocalDate nextDue = installments.stream()
                .filter(item -> !item.cancelled && !item.isPaid())
                .map(item -> item.due)
                .min(Comparator.naturalOrder())
                .orElse(null);

        JPanel page = TelaLivroCaixa.page();
        ComponentesVisuais.RoundPanel surface = new ComponentesVisuais.RoundPanel(16).fill(Color.WHITE);
        surface.setLayout(new BorderLayout(0, 14));
        surface.setBorder(new EmptyBorder(17, 18, 15, 18));

        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(TelaLivroCaixa.title("Fatura nº " + String.format("%03d", invoiceId), 30));
        JLabel subtitle = new JLabel(client + (appointment == null ? "" : "  •  " + appointment.type));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        subtitle.setForeground(ComponentesVisuais.MUTED);
        heading.add(Box.createVerticalStrut(4));
        heading.add(subtitle);
        header.add(heading);
        String invoiceStatus = cancelledCount == installments.size() && !installments.isEmpty() ? "Cancelada"
                : balance.signum() == 0 ? "Quitada"
                : overdueCount > 0 ? "Com parcelas vencidas" : "Em aberto";
        header.add(invoiceStatusPill(invoiceStatus), BorderLayout.EAST);

        JPanel top = new JPanel(new BorderLayout(0, 13));
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(invoiceInformation(client, appointment, entry), BorderLayout.CENTER);
        surface.add(top, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 13));
        content.setOpaque(false);
        JPanel values = new JPanel(new GridLayout(1, 4, 12, 0));
        values.setOpaque(false);
        values.add(invoiceMetric("Valor original", TelaLivroCaixa.money(total), "document", ComponentesVisuais.BLUE,
                "Entrada: " + TelaLivroCaixa.money(entry)));
        values.add(invoiceMetric("Valor recebido", TelaLivroCaixa.money(received), "coins", new Color(22, 137, 59),
                "Descontos: " + TelaLivroCaixa.money(discounts)));
        values.add(invoiceMetric("Saldo restante", TelaLivroCaixa.money(balance), "wallet",
                balance.signum() == 0 ? new Color(22, 137, 59) : ComponentesVisuais.RED,
                balance.signum() == 0 ? "Fatura liquidada" : "Ainda falta receber"));
        values.add(invoiceMetric("Parcelas restantes", String.valueOf(openCount), "calendar",
                openCount == 0 ? new Color(22, 137, 59) : new Color(214, 119, 0),
                nextDue == null ? "Nenhum vencimento pendente"
                        : (overdueCount > 0 ? "Mais antigo: " : "Próxima: ") + TelaLivroCaixa.fmt(nextDue)));
        content.add(values, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(14, 0));
        center.setOpaque(false);
        center.add(invoiceInstallmentsTable(installments, payments), BorderLayout.CENTER);
        center.add(invoiceCounts(declaredInstallments, paidCount, openCount, cancelledCount, overdueCount), BorderLayout.EAST);
        content.add(center);
        surface.add(content);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, ComponentesVisuais.LINE), new EmptyBorder(13, 0, 0, 0)));
        JButton back = ComponentesVisuais.secondary("Voltar para contas a receber", "back");
        back.addActionListener(e -> frame.showPage("Contas a receber"));
        footer.add(back);
        surface.add(footer, BorderLayout.SOUTH);

        page.add(surface);
        frame.showCustom("Contas a receber", page);
    }

    private static JComponent invoiceInformation(String client, Appointment appointment, BigDecimal entry) {
        ComponentesVisuais.RoundPanel banner = new ComponentesVisuais.RoundPanel(12).fill(new Color(232, 241, 255)).shadow(false);
        banner.setLayout(new GridLayout(1, 4));
        banner.setBorder(new EmptyBorder(11, 15, 11, 15));
        banner.add(pair("Cliente:", client));
        banner.add(pair("Consulta:", appointment == null ? "Não vinculada" : String.valueOf(appointment.id)));
        banner.add(pair("Data:", appointment == null ? "—" : TelaLivroCaixa.fmt(appointment.date)));
        banner.add(pair("Entrada:", TelaLivroCaixa.money(entry)));
        return banner;
    }

    private static JComponent invoiceMetric(String label, String value, String icon, Color color, String detail) {
        ComponentesVisuais.RoundPanel card = new ComponentesVisuais.RoundPanel(13).fill(new Color(color.getRed(), color.getGreen(), color.getBlue(), 18))
                .line(new Color(color.getRed(), color.getGreen(), color.getBlue(), 92)).shadow(false);
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(new EmptyBorder(13, 15, 13, 15));
        ComponentesVisuais.RoundPanel iconBox = new ComponentesVisuais.RoundPanel(11).fill(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30))
                .line(null).shadow(false);
        iconBox.setPreferredSize(new Dimension(50, 50));
        iconBox.setLayout(new GridBagLayout());
        iconBox.add(new JLabel(ComponentesVisuais.vectorIcon(icon, 27, color)));
        card.add(iconBox, BorderLayout.WEST);
        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        JLabel caption = new JLabel(label);
        caption.setFont(new Font("Segoe UI", Font.BOLD, 14));
        caption.setForeground(ComponentesVisuais.MUTED);
        JLabel amount = TelaLivroCaixa.title(value, 21);
        amount.setForeground(color);
        JLabel helper = new JLabel(detail);
        helper.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        helper.setForeground(ComponentesVisuais.MUTED);
        copy.add(caption);
        copy.add(Box.createVerticalStrut(4));
        copy.add(amount);
        copy.add(Box.createVerticalStrut(3));
        copy.add(helper);
        card.add(copy);
        return card;
    }

    private static JComponent invoiceCounts(long total, long paid, long open, long cancelled, long overdue) {
        ComponentesVisuais.RoundPanel card = new ComponentesVisuais.RoundPanel(13).fill(new Color(248, 250, 255)).shadow(false);
        card.setPreferredSize(new Dimension(285, 0));
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(12, 14, 12, 14));
        card.add(TelaLivroCaixa.title("Situação das parcelas", 20), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        ComponentesVisuais.RoundPanel totalCard = countCard("Parcelas cadastradas", total, "document", ComponentesVisuais.BLUE);
        totalCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        totalCard.setPreferredSize(new Dimension(0, 70));
        content.add(totalCard);
        content.add(Box.createVerticalStrut(9));

        JPanel statuses = new JPanel(new GridLayout(2, 2, 8, 8));
        statuses.setOpaque(false);
        statuses.setPreferredSize(new Dimension(0, 164));
        statuses.setMaximumSize(new Dimension(Integer.MAX_VALUE, 164));
        statuses.add(countCard("Quitadas", paid, "check", new Color(22, 137, 59)));
        statuses.add(countCard("Em aberto", open, "calendar", new Color(214, 119, 0)));
        statuses.add(countCard("Vencidas", overdue, "warning", ComponentesVisuais.RED));
        statuses.add(countCard("Canceladas", cancelled, "close", new Color(91, 108, 137)));
        content.add(statuses);
        card.add(content);
        return card;
    }

    private static ComponentesVisuais.RoundPanel countCard(String label, long value, String icon, Color color) {
        ComponentesVisuais.RoundPanel tile = new ComponentesVisuais.RoundPanel(11)
                .fill(new Color(color.getRed(), color.getGreen(), color.getBlue(), 18))
                .line(new Color(color.getRed(), color.getGreen(), color.getBlue(), 78)).shadow(false);
        tile.setLayout(new BorderLayout(8, 0));
        tile.setBorder(new EmptyBorder(9, 10, 9, 10));
        tile.add(new JLabel(ComponentesVisuais.vectorIcon(icon, 23, color)), BorderLayout.WEST);
        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        JLabel amount = TelaLivroCaixa.title(String.valueOf(value), 22);
        amount.setForeground(color);
        JLabel name = new JLabel(label);
        name.setFont(new Font("Segoe UI", Font.BOLD, 12));
        name.setForeground(new Color(58, 72, 96));
        copy.add(amount);
        copy.add(Box.createVerticalStrut(2));
        copy.add(name);
        tile.add(copy);
        return tile;
    }

    private static JComponent invoiceInstallmentsTable(List<Installment> installments, List<Payment> payments) {
        ComponentesVisuais.RoundPanel card = new ComponentesVisuais.RoundPanel(13).fill(new Color(248, 250, 255)).shadow(false);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(11, 11, 11, 11));
        card.add(TelaLivroCaixa.title("Parcelas da fatura", 20), BorderLayout.NORTH);
        DefaultTableModel model = ComponentesVisuais.readOnlyModel(
                "Parcela", "Vencimento", "Valor", "Recebido", "Saldo", "Status");
        for (Installment installment : installments) {
            BigDecimal cashReceived = payments.stream()
                    .filter(payment -> !payment.reversed && payment.label.equals(installment.label))
                    .map(payment -> payment.value)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            model.addRow(new Object[]{installment.label, TelaLivroCaixa.fmt(installment.due),
                    TelaLivroCaixa.money(installment.value), TelaLivroCaixa.money(cashReceived),
                    TelaLivroCaixa.money(installment.remaining()), installment.status(REFERENCE_DATE)});
        }
        JTable table = ComponentesVisuais.modernTable(model);
        table.setRowHeight(39);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(ComponentesVisuais.LINE));
        card.add(scroll);
        return card;
    }

    private static JComponent invoiceStatusPill(String status) {
        boolean paid = "Quitada".equals(status);
        boolean overdue = status.contains("vencidas");
        Color foreground = paid ? new Color(7, 115, 27) : overdue ? ComponentesVisuais.RED : new Color(146, 87, 0);
        Color background = paid ? new Color(217, 244, 218) : overdue ? new Color(255, 226, 228) : new Color(255, 238, 193);
        JLabel pill = new JLabel(status, paid ? ComponentesVisuais.vectorIcon("check", 19, foreground) : ComponentesVisuais.vectorIcon("info", 19, foreground), SwingConstants.CENTER);
        pill.setFont(new Font("Segoe UI", Font.BOLD, 15));
        pill.setForeground(foreground);
        pill.setBackground(background);
        pill.setOpaque(true);
        pill.setIconTextGap(8);
        pill.setBorder(new EmptyBorder(8, 13, 8, 13));
        JPanel holder = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        holder.setOpaque(false);
        holder.add(pill);
        return holder;
    }

    private static JComponent detailBanner(Installment item) {
        ComponentesVisuais.RoundPanel banner = new ComponentesVisuais.RoundPanel(12).fill(new Color(232, 241, 255)).shadow(false);
        banner.setBorder(new EmptyBorder(11, 15, 11, 15));
        banner.setLayout(new GridLayout(1, 5));
        banner.add(pair("Cliente:", item.client));
        banner.add(pair("Fatura:", String.valueOf(item.appointmentId)));
        banner.add(pair("Parcela:", item.label));
        banner.add(pair("Valor liquidado:", TelaLivroCaixa.money(item.paid)));
        JPanel status = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        status.setOpaque(false);
        JLabel label = new JLabel("Status:");
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JLabel pill = new JLabel(item.paid.compareTo(item.value) >= 0 ? "Pago" : "Parcial");
        pill.setOpaque(true);
        pill.setForeground(new Color(7, 115, 27));
        pill.setBackground(new Color(217, 244, 218));
        pill.setFont(new Font("Segoe UI", Font.BOLD, 16));
        pill.setBorder(new EmptyBorder(5, 12, 5, 12));
        status.add(label);
        status.add(pill);
        banner.add(status);
        return banner;
    }

    private static JPanel pair(String name, String value) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        p.setOpaque(false);
        JLabel a = new JLabel(name);
        a.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JLabel b = TelaLivroCaixa.title(value, 18);
        b.setForeground(new Color(23, 58, 145));
        p.add(a);
        p.add(b);
        return p;
    }

    private static JComponent historyPanel(TelaLivroCaixa.JanelaPrincipal frame, Installment item,
                                           List<Payment> payments) {
        ComponentesVisuais.RoundPanel card = new ComponentesVisuais.RoundPanel(13).fill(new Color(248, 250, 255)).shadow(false);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));
        JPanel heading = sectionHeading("Histórico de pagamentos",
                payments.size() == 1 ? "1 pagamento registrado" : payments.size() + " pagamentos registrados");
        card.add(heading, BorderLayout.NORTH);
        DefaultTableModel model = ComponentesVisuais.readOnlyModel(
                "Data", "Forma de pagamento", "Valor", "Desconto", "Ações");
        for (Payment payment : payments) {
            model.addRow(new Object[]{TelaLivroCaixa.fmt(payment.date), payment.method,
                    TelaLivroCaixa.money(payment.value), TelaLivroCaixa.money(payment.discount), ""});
        }
        JTable table = ComponentesVisuais.modernTable(model);
        table.setRowHeight(48);
        table.getTableHeader().setPreferredSize(new Dimension(0, 42));
        int[] widths = {125, 235, 145, 145, 215};
        for (int column = 0; column < widths.length; column++) {
            table.getColumnModel().getColumn(column).setPreferredWidth(widths[column]);
        }
        table.getColumnModel().getColumn(1).setMinWidth(190);
        table.getColumnModel().getColumn(4).setMinWidth(190);
        table.getColumnModel().getColumn(4).setCellRenderer(new UndoRenderer());
        ComponentesVisuais.onClick(table, event -> {
            int viewRow = table.rowAtPoint(event.getPoint());
            int column = table.columnAtPoint(event.getPoint());
            if (viewRow < 0 || column != 4) return;
            int row = table.convertRowIndexToModel(viewRow);
            if (row >= payments.size()) return;
            Payment payment = payments.get(row);
            if (TelaLivroCaixa.confirm(frame,
                    "Desfazer a baixa de " + TelaLivroCaixa.money(payment.value)
                            + " via " + payment.method + "?")) {
                frame.store.reversePayment(item, payment);
                frame.paymentConfirmed(item);
            }
        });
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(ComponentesVisuais.LINE));
        card.add(scroll);
        return card;
    }

    private static JComponent summaryPanel(Installment item, List<Payment> payments) {
        BigDecimal received = payments.stream().map(p -> p.value).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discounts = payments.stream().map(p -> p.discount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remaining = item.value.subtract(item.paid).max(BigDecimal.ZERO);
        ComponentesVisuais.RoundPanel card = new ComponentesVisuais.RoundPanel(13).fill(new Color(248, 250, 255)).shadow(false);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));
        card.add(sectionHeading("Resumo da parcela", "Composição do valor liquidado"), BorderLayout.NORTH);
        JPanel rows = new JPanel();
        rows.setOpaque(false);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        addSummaryRow(rows, "Valor original", item.value, false);
        addSummaryRow(rows, "Total recebido", received, false);
        addSummaryRow(rows, "Total em descontos", discounts, false);
        addSummaryRow(rows, "Valor liquidado", item.paid, false);
        addSummaryRow(rows, "Saldo restante", remaining, true);
        JPanel rowsHolder = new JPanel(new BorderLayout());
        rowsHolder.setOpaque(false);
        rowsHolder.add(rows, BorderLayout.NORTH);
        card.add(rowsHolder);
        return card;
    }

    private static void addSummaryRow(JPanel rows, String label, BigDecimal value, boolean highlight) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(true);
        row.setBackground(highlight ? new Color(229, 247, 233) : Color.WHITE);
        row.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, highlight ? new Color(157, 218, 169) : ComponentesVisuais.LINE),
                new EmptyBorder(15, 13, 15, 13)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, highlight ? 64 : 58));
        JLabel a = new JLabel(label);
        JLabel b = TelaLivroCaixa.title(TelaLivroCaixa.money(value), highlight ? 22 : 18);
        b.setHorizontalAlignment(SwingConstants.RIGHT);
        b.setForeground(highlight ? new Color(7, 125, 31) : new Color(23, 58, 145));
        if (highlight) {
            a.setFont(new Font("Segoe UI", Font.BOLD, 18));
            a.setForeground(new Color(7, 90, 25));
        } else {
            a.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        }
        row.add(a, BorderLayout.WEST);
        row.add(b, BorderLayout.EAST);
        rows.add(row);
        rows.add(Box.createVerticalStrut(7));
    }

    private static JPanel sectionHeading(String title, String subtitle) {
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(TelaLivroCaixa.title(title, 21));
        heading.add(Box.createVerticalStrut(3));
        JLabel helper = new JLabel(subtitle);
        helper.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        helper.setForeground(ComponentesVisuais.MUTED);
        heading.add(helper);
        return heading;
    }

    private static final class UndoRenderer implements TableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                                 boolean focus, int row, int column) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 7));
            p.setOpaque(true);
            p.setBackground(selected ? table.getSelectionBackground() : (row & 1) == 0 ? Color.WHITE : new Color(249, 251, 255));
            JButton button = ComponentesVisuais.secondary("Desfazer baixa", "refresh");
            button.setForeground(new Color(205, 23, 31));
            button.setBorder(new EmptyBorder(5, 10, 5, 10));
            p.add(button);
            return p;
        }
    }
}
