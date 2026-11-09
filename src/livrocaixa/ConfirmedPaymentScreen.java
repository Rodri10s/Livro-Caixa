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
import java.util.ArrayList;
import java.util.List;

/** Tela visual do fluxo 10: confirmação, resumo e desfazer baixa. */
final class ConfirmedPaymentScreen {
    private ConfirmedPaymentScreen() {}

    static void show(LivroCaixaApp.MainFrame frame, LivroCaixaApp.Installment item) {
        JPanel page = LivroCaixaApp.page();
        UiKit.RoundPanel surface = new UiKit.RoundPanel(16).fill(Color.WHITE);
        surface.setLayout(new BorderLayout(0, 12));
        surface.setBorder(new EmptyBorder(15, 18, 15, 18));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(LivroCaixaApp.title("Pagamento confirmado", 29));
        top.add(Box.createVerticalStrut(10));
        UiKit.RoundPanel success = new UiKit.RoundPanel(13).fill(new Color(224, 249, 222)).line(new Color(76, 190, 78)).shadow(false);
        success.setLayout(new BorderLayout());
        success.setBorder(new EmptyBorder(13, 18, 13, 18));
        JLabel successText = new JLabel("Pagamento registrado com sucesso", UiKit.vectorIcon("check", 42, new Color(7, 132, 31)), SwingConstants.LEFT);
        successText.setIconTextGap(14);
        successText.setFont(new Font("Segoe UI", Font.BOLD, 25));
        successText.setForeground(new Color(7, 117, 26));
        success.add(successText);
        top.add(success);
        top.add(Box.createVerticalStrut(10));
        top.add(detailBanner(item));
        surface.add(top, BorderLayout.NORTH);

        List<LivroCaixaApp.Payment> payments = new ArrayList<>();
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
        UiKit.RoundPanel hint = new UiKit.RoundPanel(12).fill(new Color(231, 240, 255)).shadow(false);
        hint.setBorder(new EmptyBorder(10, 14, 10, 14));
        hint.setLayout(new BorderLayout(10, 0));
        hint.add(new JLabel(UiKit.vectorIcon("info", 25, UiKit.BLUE)), BorderLayout.WEST);
        JLabel hintText = new JLabel("Desfazer baixa reverte o pagamento e o desconto associados.");
        hintText.setFont(new Font("Segoe UI", Font.BOLD, 16));
        hint.add(hintText);
        bottom.add(hint, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton invoice = UiKit.secondary("Ver fatura", "document");
        JButton back = UiKit.primary("Voltar para contas a receber", "document");
        invoice.addActionListener(e -> LivroCaixaApp.info(frame,
                "Fatura " + item.appointmentId + "\nCliente: " + item.client + "\nParcela: " + item.label
                        + "\nValor: " + LivroCaixaApp.money(item.value)));
        back.addActionListener(e -> frame.showPage("Contas a receber"));
        actions.add(invoice);
        actions.add(back);
        bottom.add(actions, BorderLayout.SOUTH);
        surface.add(bottom, BorderLayout.SOUTH);
        page.add(surface);
        frame.showCustom("Contas a receber", page);
    }

    private static JComponent detailBanner(LivroCaixaApp.Installment item) {
        UiKit.RoundPanel banner = new UiKit.RoundPanel(12).fill(new Color(232, 241, 255)).shadow(false);
        banner.setBorder(new EmptyBorder(11, 15, 11, 15));
        banner.setLayout(new GridLayout(1, 5));
        banner.add(pair("Cliente:", item.client));
        banner.add(pair("Fatura:", String.valueOf(item.appointmentId)));
        banner.add(pair("Parcela:", item.label));
        banner.add(pair("Valor liquidado:", LivroCaixaApp.money(item.paid)));
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
        JLabel b = LivroCaixaApp.title(value, 18);
        b.setForeground(new Color(23, 58, 145));
        p.add(a);
        p.add(b);
        return p;
    }

    private static JComponent historyPanel(LivroCaixaApp.MainFrame frame, LivroCaixaApp.Installment item,
                                           List<LivroCaixaApp.Payment> payments) {
        UiKit.RoundPanel card = new UiKit.RoundPanel(13).fill(new Color(248, 250, 255)).shadow(false);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(11, 11, 11, 11));
        card.add(LivroCaixaApp.title("Histórico de pagamentos", 21), BorderLayout.NORTH);
        DefaultTableModel model = new DefaultTableModel(new String[]{"Data", "Forma de pagamento", "Valor", "Desconto", "Ações"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (LivroCaixaApp.Payment payment : payments) {
            model.addRow(new Object[]{LivroCaixaApp.fmt(payment.date), payment.method,
                    LivroCaixaApp.money(payment.value), LivroCaixaApp.money(payment.discount), ""});
        }
        JTable table = UiKit.modernTable(model);
        table.setRowHeight(52);
        table.getColumnModel().getColumn(4).setPreferredWidth(190);
        table.getColumnModel().getColumn(4).setCellRenderer(new UndoRenderer());
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int viewRow = table.rowAtPoint(e.getPoint());
                int column = table.columnAtPoint(e.getPoint());
                if (viewRow < 0 || column != 4) return;
                int row = table.convertRowIndexToModel(viewRow);
                if (row >= payments.size()) return;
                LivroCaixaApp.Payment payment = payments.get(row);
                if (LivroCaixaApp.confirm(frame, "Desfazer esta baixa?")) {
                    payment.reversed = true;
                    item.paid = item.paid.subtract(payment.value.add(payment.discount)).max(BigDecimal.ZERO);
                    frame.paymentConfirmed(item);
                }
            }
        });
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(UiKit.LINE));
        card.add(scroll);
        return card;
    }

    private static JComponent summaryPanel(LivroCaixaApp.Installment item, List<LivroCaixaApp.Payment> payments) {
        BigDecimal received = payments.stream().map(p -> p.value).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discounts = payments.stream().map(p -> p.discount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remaining = item.value.subtract(item.paid).max(BigDecimal.ZERO);
        UiKit.RoundPanel card = new UiKit.RoundPanel(13).fill(new Color(248, 250, 255)).shadow(false);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(11, 14, 11, 14));
        card.add(LivroCaixaApp.title("Resumo da parcela", 21), BorderLayout.NORTH);
        JPanel rows = new JPanel(new GridLayout(5, 2, 8, 0));
        rows.setOpaque(false);
        addSummaryRow(rows, "Valor original", item.value, false);
        addSummaryRow(rows, "Total recebido", received, false);
        addSummaryRow(rows, "Total em descontos", discounts, false);
        addSummaryRow(rows, "Valor liquidado", item.paid, false);
        addSummaryRow(rows, "Saldo restante", remaining, true);
        card.add(rows);
        return card;
    }

    private static void addSummaryRow(JPanel rows, String label, BigDecimal value, boolean highlight) {
        JLabel a = new JLabel(label);
        JLabel b = LivroCaixaApp.title(LivroCaixaApp.money(value), highlight ? 22 : 18);
        b.setHorizontalAlignment(SwingConstants.RIGHT);
        b.setForeground(highlight ? new Color(7, 125, 31) : new Color(23, 58, 145));
        if (highlight) {
            a.setFont(new Font("Segoe UI", Font.BOLD, 18));
            a.setForeground(new Color(7, 90, 25));
        }
        a.setBorder(new EmptyBorder(8, 3, 8, 3));
        b.setBorder(new EmptyBorder(8, 3, 8, 3));
        rows.add(a);
        rows.add(b);
    }

    private static final class UndoRenderer implements TableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                                 boolean focus, int row, int column) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 7));
            p.setOpaque(true);
            p.setBackground(selected ? table.getSelectionBackground() : (row & 1) == 0 ? Color.WHITE : new Color(249, 251, 255));
            JButton button = UiKit.secondary("Desfazer baixa", "refresh");
            button.setForeground(new Color(205, 23, 31));
            button.setBorder(new EmptyBorder(5, 10, 5, 10));
            p.add(button);
            return p;
        }
    }
}
