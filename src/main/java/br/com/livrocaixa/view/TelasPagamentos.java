package br.com.livrocaixa.view;

import br.com.livrocaixa.view.dados.Installment;
import br.com.livrocaixa.view.dados.LivroCaixaModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Fluxo de registro de pagamentos e descontos das parcelas. */
final class TelasPagamentos {
    private TelasPagamentos() {}

    static void showPayment(TelaLivroCaixa.JanelaPrincipal frame, Installment item) {
        if (item.cancelled) {
            TelaLivroCaixa.error(frame, "Parcela cancelada.");
            return;
        }
        if (item.paid.compareTo(item.value) >= 0) {
            frame.paymentConfirmed(item);
            return;
        }

        BigDecimal originalBalance = item.value.subtract(item.paid);
        JPanel page = TelaLivroCaixa.page();
        ComponentesVisuais.RoundPanel surface = new ComponentesVisuais.RoundPanel(16).fill(Color.WHITE);
        surface.setLayout(new BorderLayout(0, 13));
        surface.setBorder(new EmptyBorder(15, 18, 15, 18));
        surface.add(TelaLivroCaixa.title("Registrar pagamento", 29), BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout(0, 13));
        main.setOpaque(false);
        main.add(paymentBanner(item), BorderLayout.NORTH);

        JComboBox<String> method = new JComboBox<>(frame.store.paymentMethods.toArray(String[]::new));
        method.setSelectedItem("Cartão de crédito");
        BigDecimal suggestedDiscount = originalBalance.min(new BigDecimal("20.00"));
        JTextField received = TelaLivroCaixa.modernField(
                originalBalance.subtract(suggestedDiscount).toPlainString());
        JTextField date = TelaLivroCaixa.modernField(
                TelaLivroCaixa.fmt(LivroCaixaModel.DEMO_DATE));
        JTextField discount = TelaLivroCaixa.modernField(suggestedDiscount.toPlainString());
        ComponentesVisuais.SegmentToggle money = new ComponentesVisuais.SegmentToggle("R$");
        ComponentesVisuais.SegmentToggle percent = new ComponentesVisuais.SegmentToggle("%");

        JPanel left = new JPanel(new BorderLayout(0, 11));
        left.setOpaque(false);
        left.add(paymentForm(method, received, date, discount, money, percent), BorderLayout.NORTH);
        left.add(paymentHistory(frame, item));

        JLabel discountValue = summaryValue(TelaLivroCaixa.money(BigDecimal.ZERO), new Color(202, 112, 0));
        JLabel currentValue = summaryValue(TelaLivroCaixa.money(BigDecimal.ZERO), new Color(18, 137, 62));
        JLabel balanceValue = summaryValue(TelaLivroCaixa.money(originalBalance), new Color(202, 112, 0));

        JPanel columns = new JPanel(new GridLayout(1, 2, 15, 0));
        columns.setOpaque(false);
        columns.add(left);
        columns.add(paymentSummary(item, discountValue, currentValue, balanceValue));
        main.add(columns);
        surface.add(main);

        Runnable recalculate = () -> updateSummary(
                originalBalance, received, discount, percent,
                discountValue, currentValue, balanceValue);
        attach(received, recalculate);
        attach(discount, recalculate);
        money.addActionListener(event -> recalculate.run());
        percent.addActionListener(event -> recalculate.run());
        recalculate.run();

        surface.add(paymentActions(
                frame, item, originalBalance, method, received, date, discount, percent), BorderLayout.SOUTH);
        page.add(surface);
        frame.showCustom("Contas a receber", page);
    }

    private static ComponentesVisuais.RoundPanel paymentBanner(Installment item) {
        ComponentesVisuais.RoundPanel banner = new ComponentesVisuais.RoundPanel(13)
                .fill(new Color(231, 240, 255))
                .shadow(false);
        banner.setLayout(new GridLayout(1, 5));
        banner.setBorder(new EmptyBorder(13, 16, 13, 16));
        banner.add(bannerStack("Cliente:", item.client));
        banner.add(bannerStack("Fatura:", String.valueOf(item.appointmentId)));
        banner.add(bannerStack("Parcela:", item.label));
        banner.add(bannerStack("Vencimento:", TelaLivroCaixa.fmt(item.due)));
        banner.add(bannerStack("Valor da parcela:", TelaLivroCaixa.money(item.value)));
        return banner;
    }

    private static ComponentesVisuais.RoundPanel paymentForm(
            JComboBox<String> method,
            JTextField received,
            JTextField date,
            JTextField discount,
            ComponentesVisuais.SegmentToggle money,
            ComponentesVisuais.SegmentToggle percent) {
        ComponentesVisuais.RoundPanel form = new ComponentesVisuais.RoundPanel(14)
                .fill(new Color(248, 250, 255))
                .shadow(false);
        form.setLayout(new GridBagLayout());
        form.setBorder(new EmptyBorder(12, 15, 12, 15));

        ButtonGroup discountModes = new ButtonGroup();
        discountModes.add(money);
        discountModes.add(percent);
        money.setSelected(true);

        JPanel discountMode = new JPanel(new GridLayout(1, 2));
        discountMode.setOpaque(false);
        discountMode.add(money);
        discountMode.add(percent);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.gridwidth = 2;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets = new Insets(0, 0, 8, 0);
        form.add(fieldBox("Forma de pagamento", method), constraints);

        constraints.gridy++;
        form.add(fieldBox("Valor recebido", received), constraints);

        constraints.gridy++;
        constraints.gridwidth = 1;
        constraints.weightx = .55;
        constraints.insets = new Insets(0, 0, 0, 9);
        form.add(fieldBox("Data", date), constraints);

        constraints.gridx = 1;
        constraints.weightx = .45;
        constraints.insets = new Insets(0, 0, 0, 0);
        JPanel discountLine = new JPanel(new BorderLayout(8, 0));
        discountLine.setOpaque(false);
        discountLine.add(discountMode, BorderLayout.WEST);
        discountLine.add(discount);
        form.add(fieldBox("Desconto", discountLine), constraints);
        return form;
    }

    private static JPanel paymentHistory(TelaLivroCaixa.JanelaPrincipal frame, Installment item) {
        Object[][] history = frame.store.payments.stream()
                .filter(payment -> payment.appointmentId == item.appointmentId
                        && payment.label.equals(item.label)
                        && !payment.reversed)
                .map(payment -> new Object[]{
                        TelaLivroCaixa.fmt(payment.date),
                        payment.method,
                        TelaLivroCaixa.money(payment.value)})
                .toArray(Object[][]::new);
        return tablePanel(
                "Pagamentos já realizados",
                new String[]{"Data", "Forma de pagamento", "Valor"},
                history);
    }

    private static ComponentesVisuais.RoundPanel paymentSummary(
            Installment item,
            JLabel discountValue,
            JLabel currentValue,
            JLabel balanceValue) {
        JLabel installmentValue = summaryValue(
                TelaLivroCaixa.money(item.value), new Color(24, 58, 145));
        JLabel receivedValue = summaryValue(TelaLivroCaixa.money(item.paid), ComponentesVisuais.BLUE);
        return paymentSummaryPanel(
                installmentValue,
                receivedValue,
                discountValue,
                currentValue,
                balanceValue);
    }

    private static JPanel paymentActions(
            TelaLivroCaixa.JanelaPrincipal frame,
            Installment item,
            BigDecimal originalBalance,
            JComboBox<String> method,
            JTextField received,
            JTextField date,
            JTextField discount,
            ComponentesVisuais.SegmentToggle percent) {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton back = ComponentesVisuais.secondary("Voltar", "back");
        JButton cancel = ComponentesVisuais.danger("Cancelar parcela", "close");
        JButton confirm = ComponentesVisuais.primary("Confirmar pagamento", "wallet");

        back.addActionListener(event -> frame.showPage("Contas a receber"));
        cancel.addActionListener(event -> {
            String message = "Cancelar a parcela " + item.label + " de " + item.client + "?";
            if (TelaLivroCaixa.confirm(frame, message)) {
                frame.store.cancelInstallment(item);
                frame.showPage("Contas a receber");
            }
        });
        confirm.addActionListener(event -> {
            try {
                PaymentAmounts amounts = calculatePayment(
                        originalBalance, received, discount, percent.isSelected());
                if (amounts.payment().signum() <= 0
                        || amounts.discount().signum() < 0
                        || amounts.payment().add(amounts.discount()).compareTo(originalBalance) > 0) {
                    throw new IllegalArgumentException();
                }
                frame.store.registerInstallmentPayment(
                        item,
                        TelaLivroCaixa.parseDate(date.getText()),
                        amounts.payment(),
                        String.valueOf(method.getSelectedItem()),
                        amounts.discount());
                frame.paymentConfirmed(item);
            } catch (Exception exception) {
                TelaLivroCaixa.error(frame, "Revise o pagamento, o desconto e a data.");
            }
        });

        actions.add(back);
        actions.add(cancel);
        actions.add(confirm);
        return actions;
    }

    private static void updateSummary(
            BigDecimal originalBalance,
            JTextField received,
            JTextField discount,
            ComponentesVisuais.SegmentToggle percent,
            JLabel discountValue,
            JLabel currentValue,
            JLabel balanceValue) {
        try {
            PaymentAmounts amounts = calculatePayment(
                    originalBalance, received, discount, percent.isSelected());
            discountValue.setText(TelaLivroCaixa.money(amounts.discount()));
            currentValue.setText(TelaLivroCaixa.money(amounts.payment()));
            balanceValue.setText(TelaLivroCaixa.money(amounts.balance()));
            styleBalance(balanceValue, amounts.balance());
        } catch (Exception ignored) {
            balanceValue.setText("Revise os valores");
            styleSummaryCard(
                    balanceValue,
                    ComponentesVisuais.RED,
                    new Color(255, 239, 241),
                    new Color(255, 193, 199));
        }
    }

    private static PaymentAmounts calculatePayment(
            BigDecimal originalBalance,
            JTextField received,
            JTextField discount,
            boolean percentageDiscount) {
        BigDecimal payment = TelaLivroCaixa.parseMoney(received.getText());
        BigDecimal rawDiscount = TelaLivroCaixa.parseMoney(discount.getText());
        BigDecimal actualDiscount = percentageDiscount
                ? originalBalance.multiply(rawDiscount)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : rawDiscount;
        BigDecimal balance = originalBalance.subtract(payment).subtract(actualDiscount);
        return new PaymentAmounts(payment, actualDiscount, balance);
    }

    private static JPanel bannerStack(String label, String value) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel caption = new JLabel(label);
        caption.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JLabel content = TelaLivroCaixa.title(value, 21);
        content.setForeground(new Color(23, 58, 145));
        panel.add(caption);
        panel.add(Box.createVerticalStrut(7));
        panel.add(content);
        return panel;
    }

    private static JPanel fieldBox(String label, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        JLabel caption = new JLabel(label);
        caption.setFont(new Font("Segoe UI", Font.BOLD, 15));
        panel.add(caption, BorderLayout.NORTH);
        component.setPreferredSize(new Dimension(130, 38));
        panel.add(component);
        return panel;
    }

    private static JLabel summaryValue(String text, Color color) {
        JLabel label = TelaLivroCaixa.title(text, 19);
        label.setForeground(color);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        return label;
    }

    private static ComponentesVisuais.RoundPanel paymentSummaryPanel(
            JLabel installment,
            JLabel received,
            JLabel discount,
            JLabel current,
            JLabel balance) {
        ComponentesVisuais.RoundPanel panel = new ComponentesVisuais.RoundPanel(14)
                .fill(new Color(247, 249, 255))
                .line(new Color(210, 222, 239))
                .shadow(false);
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(15, 16, 15, 16));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(TelaLivroCaixa.title("Resumo do recebimento", 22));
        JLabel hint = new JLabel("Valores atualizados automaticamente");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        hint.setForeground(ComponentesVisuais.MUTED);
        header.add(Box.createVerticalStrut(3));
        header.add(hint);
        panel.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        ComponentesVisuais.RoundPanel hero = new ComponentesVisuais.RoundPanel(13)
                .fill(new Color(232, 241, 255))
                .line(new Color(185, 208, 241))
                .shadow(false);
        hero.setLayout(new BorderLayout(14, 0));
        hero.setBorder(new EmptyBorder(14, 15, 14, 15));
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));

        ComponentesVisuais.RoundPanel heroIcon = new ComponentesVisuais.RoundPanel(11)
                .fill(new Color(215, 231, 255))
                .line(null)
                .shadow(false);
        heroIcon.setPreferredSize(new Dimension(54, 54));
        heroIcon.setLayout(new GridBagLayout());
        heroIcon.add(new JLabel(ComponentesVisuais.vectorIcon("wallet", 27, ComponentesVisuais.BLUE)));
        hero.add(heroIcon, BorderLayout.WEST);

        JPanel heroText = new JPanel();
        heroText.setOpaque(false);
        heroText.setLayout(new BoxLayout(heroText, BoxLayout.Y_AXIS));
        JLabel heroLabel = new JLabel("Valor da parcela");
        heroLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        heroLabel.setForeground(ComponentesVisuais.MUTED);
        installment.setFont(new Font("Segoe UI", Font.BOLD, 25));
        installment.setHorizontalAlignment(SwingConstants.LEFT);
        heroText.add(heroLabel);
        heroText.add(Box.createVerticalStrut(5));
        heroText.add(installment);
        hero.add(heroText);
        content.add(hero);
        content.add(Box.createVerticalStrut(11));

        JPanel metrics = new JPanel(new GridLayout(2, 2, 10, 10));
        metrics.setOpaque(false);
        metrics.setAlignmentX(Component.LEFT_ALIGNMENT);
        metrics.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
        metrics.add(summaryCard(
                "Já recebido", received, "coins", ComponentesVisuais.BLUE,
                new Color(235, 243, 255), new Color(197, 216, 244)));
        metrics.add(summaryCard(
                "Desconto", discount, "arrowDown", new Color(202, 112, 0),
                new Color(255, 247, 229), new Color(246, 218, 160)));
        metrics.add(summaryCard(
                "Pagamento atual", current, "wallet", new Color(18, 137, 62),
                new Color(235, 249, 239), new Color(184, 226, 197)));
        metrics.add(summaryCard(
                "Saldo restante", balance, "calendar", new Color(202, 112, 0),
                new Color(255, 247, 229), new Color(246, 218, 160)));
        content.add(metrics);
        content.add(Box.createVerticalGlue());

        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.add(content, BorderLayout.NORTH);
        panel.add(holder);
        return panel;
    }

    private static JComponent summaryCard(
            String label,
            JLabel value,
            String icon,
            Color accent,
            Color fill,
            Color line) {
        ComponentesVisuais.RoundPanel card = new ComponentesVisuais.RoundPanel(12)
                .fill(fill)
                .line(line)
                .shadow(false);
        card.setLayout(new BorderLayout(10, 0));
        card.setBorder(new EmptyBorder(11, 12, 11, 12));

        ComponentesVisuais.RoundPanel iconBox = new ComponentesVisuais.RoundPanel(9)
                .fill(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 24))
                .line(null)
                .shadow(false);
        iconBox.setPreferredSize(new Dimension(40, 40));
        iconBox.setLayout(new GridBagLayout());
        iconBox.add(new JLabel(ComponentesVisuais.vectorIcon(icon, 20, accent)));
        card.add(iconBox, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(label);
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(ComponentesVisuais.MUTED);
        value.setFont(new Font("Segoe UI", Font.BOLD, 17));
        value.setHorizontalAlignment(SwingConstants.LEFT);
        text.add(title);
        text.add(Box.createVerticalStrut(3));
        text.add(value);
        card.add(text);
        value.putClientProperty("summaryCard", card);
        return card;
    }

    private static void styleBalance(JLabel value, BigDecimal balance) {
        if (balance.signum() < 0) {
            styleSummaryCard(
                    value, ComponentesVisuais.RED,
                    new Color(255, 239, 241), new Color(255, 193, 199));
        } else if (balance.signum() == 0) {
            styleSummaryCard(
                    value, new Color(18, 137, 62),
                    new Color(235, 249, 239), new Color(184, 226, 197));
        } else {
            styleSummaryCard(
                    value, new Color(202, 112, 0),
                    new Color(255, 247, 229), new Color(246, 218, 160));
        }
    }

    private static void styleSummaryCard(JLabel value, Color color, Color fill, Color line) {
        value.setForeground(color);
        Object property = value.getClientProperty("summaryCard");
        if (property instanceof ComponentesVisuais.RoundPanel card) {
            card.fill(fill).line(line);
            card.repaint();
        }
    }

    private static JPanel tablePanel(String heading, String[] columns, Object[][] rows) {
        ComponentesVisuais.RoundPanel panel = new ComponentesVisuais.RoundPanel(13)
                .fill(new Color(247, 249, 255))
                .shadow(false);
        panel.setLayout(new BorderLayout(0, 6));
        panel.setBorder(new EmptyBorder(9, 9, 9, 9));
        panel.add(TelaLivroCaixa.title(heading, 18), BorderLayout.NORTH);
        DefaultTableModel model = ComponentesVisuais.readOnlyModel(rows, columns);
        JTable table = ComponentesVisuais.modernTable(model);
        table.setRowHeight(35);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(ComponentesVisuais.LINE));
        panel.add(scroll);
        return panel;
    }

    private static void attach(JTextField field, Runnable runnable) {
        ComponentesVisuais.onDocumentChange(field.getDocument(), runnable);
    }

    private record PaymentAmounts(BigDecimal payment, BigDecimal discount, BigDecimal balance) {}
}
