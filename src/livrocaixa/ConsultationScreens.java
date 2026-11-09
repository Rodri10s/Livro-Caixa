package livrocaixa;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Constrói a tela de criação/edição de consultas e mantém o resumo lateral
 * sincronizado com os campos visíveis. Nesta versão, os botões demonstram o fluxo
 * entre telas sem gravar dados em banco.
 */
final class ConsultationScreens {
    private ConsultationScreens() {}

    static void showForm(LivroCaixaApp.MainFrame frame, LivroCaixaApp.Appointment edit) {
        if (frame.store.clients.isEmpty()) {
            LivroCaixaApp.error(frame, "Cadastre um cliente primeiro.");
            return;
        }

        JPanel page = LivroCaixaApp.page();
        UiKit.RoundPanel surface = new UiKit.RoundPanel(17)
                .fill(new Color(252, 253, 255))
                .line(new Color(185, 205, 237))
                .shadow(false);
        surface.setLayout(new BorderLayout(0, 14));
        surface.setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = LivroCaixaApp.title(edit == null ? "Nova consulta" : "Editar consulta", 42);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        heading.add(title);
        heading.add(Box.createVerticalStrut(3));
        JLabel subtitle = new JLabel("Registre os dados do atendimento");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 27));
        subtitle.setForeground(new Color(29, 44, 70));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        heading.add(subtitle);
        heading.add(Box.createVerticalStrut(9));
        JLabel hint = new JLabel("Cadastre uma nova opção sem sair desta tela.", UiKit.vectorIcon("info", 23, new Color(24, 67, 164)), SwingConstants.LEFT);
        hint.setIconTextGap(9);
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 18));
        hint.setForeground(new Color(34, 59, 106));
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        heading.add(hint);
        // Reserva a altura completa das três linhas do cabeçalho, inclusive em 1500x850.
        heading.setPreferredSize(new Dimension(0, 138));
        surface.add(heading, BorderLayout.NORTH);

        JComboBox<String> client = new JComboBox<>(frame.store.clients.stream().map(c -> c.name).toArray(String[]::new));
        JComboBox<String> type = new JComboBox<>(frame.store.appointmentTypes.toArray(String[]::new));
        JComboBox<String> employee = new JComboBox<>(new String[]{frame.employee, "Bruno Reis"});
        styleCombo(client); styleCombo(type); styleCombo(employee);
        // O texto mais longo precisa caber também na janela padrão de 1500 px.
        type.setFont(new Font("Segoe UI", Font.PLAIN, 19));
        JTextField date = LivroCaixaApp.modernField(LivroCaixaApp.fmt(edit == null ? LocalDate.of(2026, 11, 5) : edit.date));
        date.setFont(new Font("Segoe UI", Font.PLAIN, 21));
        JTextArea notes = new JTextArea(edit == null ? "Retorno em 30 dias." : edit.notes, 5, 30);
        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);
        notes.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        notes.setForeground(UiKit.INK);
        notes.setBorder(new EmptyBorder(12, 14, 12, 14));
        if (edit != null) {
            client.setSelectedItem(edit.client);
            type.setSelectedItem(edit.type);
            employee.setSelectedItem(edit.employee);
        }

        JButton newClient = largeSecondary("Novo cliente", "plus", 190);
        JButton newType = largeSecondary("Novo tipo", "plus", 170);
        newClient.addActionListener(e -> frame.clientDialog(null));
        newType.addActionListener(e -> LivroCaixaApp.info(frame, "No protótipo visual, esta opção abre o cadastro de tipos."));

        JPanel clientLine = lineWithButton(client, newClient);
        JPanel typeLine = lineWithButton(type, newType);
        JPanel dateWithIcon = iconField(date, "calendar");

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints fc = new GridBagConstraints();
        fc.fill = GridBagConstraints.BOTH;
        fc.weightx = .9;
        fc.insets = new Insets(0, 0, 16, 16);
        fc.gridx = 0; fc.gridy = 0;
        form.add(fieldBlock("Cliente", clientLine), fc);
        fc.gridx = 1; fc.weightx = 1.1; fc.insets = new Insets(0, 0, 16, 0);
        form.add(fieldBlock("Tipo de consulta", typeLine), fc);
        fc.gridx = 0; fc.gridy = 1; fc.weightx = .9; fc.insets = new Insets(0, 0, 16, 16);
        form.add(fieldBlock("Data", dateWithIcon), fc);
        fc.gridx = 1; fc.weightx = 1.1; fc.insets = new Insets(0, 0, 16, 0);
        form.add(fieldBlock("Funcionário", employee), fc);
        JScrollPane notesScroll = new JScrollPane(notes);
        notesScroll.setBorder(new LineBorder(new Color(133, 164, 219), 1, true));
        notesScroll.setPreferredSize(new Dimension(0, 190));
        fc.gridx = 0; fc.gridy = 2; fc.gridwidth = 2; fc.weightx = 1; fc.weighty = 1;
        fc.insets = new Insets(0, 0, 0, 0);
        form.add(fieldBlock("Observações", notesScroll), fc);

        JLabel summaryClient = summaryValue(String.valueOf(client.getSelectedItem()));
        JLabel summaryType = summaryValue(String.valueOf(type.getSelectedItem()));
        JLabel summaryDate = summaryValue(date.getText());
        JLabel summaryEmployee = summaryValue(String.valueOf(employee.getSelectedItem()));
        JComponent summary = summaryPanel(summaryClient, summaryType, summaryDate, summaryEmployee);
        JPanel summarySlot = new JPanel(new BorderLayout());
        summarySlot.setOpaque(false);
        summarySlot.add(summary, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0; gc.fill = GridBagConstraints.BOTH; gc.weighty = 1;
        gc.gridx = 0; gc.weightx = 2.25; gc.insets = new Insets(0, 0, 0, 20);
        body.add(form, gc);
        gc.gridx = 1; gc.weightx = 1; gc.insets = new Insets(0, 0, 0, 0);
        body.add(summarySlot, gc);
        surface.add(body);

        // O resumo funciona como uma prévia imediata do que foi digitado/selecionado.
        Runnable refreshSummary = () -> {
            summaryClient.setText(String.valueOf(client.getSelectedItem()));
            summaryType.setText(String.valueOf(type.getSelectedItem()));
            summaryDate.setText(date.getText());
            summaryEmployee.setText(String.valueOf(employee.getSelectedItem()));
        };
        client.addActionListener(e -> refreshSummary.run());
        type.addActionListener(e -> refreshSummary.run());
        employee.addActionListener(e -> refreshSummary.run());
        date.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refreshSummary.run(); }
            public void removeUpdate(DocumentEvent e) { refreshSummary.run(); }
            public void changedUpdate(DocumentEvent e) { refreshSummary.run(); }
        });

        JPanel footer = new JPanel(new BorderLayout(0, 12));
        footer.setOpaque(false);
        footer.add(new JSeparator(), BorderLayout.NORTH);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);
        JButton cancel = actionButton("Cancelar", "close", false, 215);
        JButton save = actionButton("Salvar", "save", false, 200);
        JButton saveAndBill = actionButton("Salvar e faturar", "document", true, 310);
        cancel.addActionListener(e -> frame.showPage("Consultas"));
        save.addActionListener(e -> frame.showPage("Consultas"));
        saveAndBill.addActionListener(e -> {
            try {
                // Registro temporário usado somente para abrir a próxima etapa do protótipo.
                LivroCaixaApp.Appointment demo = new LivroCaixaApp.Appointment(
                        105,
                        String.valueOf(client.getSelectedItem()),
                        String.valueOf(type.getSelectedItem()),
                        LivroCaixaApp.parseDate(date.getText()),
                        String.valueOf(employee.getSelectedItem()),
                        new BigDecimal("1400.00"),
                        notes.getText(),
                        false
                );
                frame.billingDialog(demo);
            } catch (Exception ex) {
                LivroCaixaApp.error(frame, "Revise a data informada.");
            }
        });
        actions.add(cancel); actions.add(save); actions.add(saveAndBill);
        footer.add(actions, BorderLayout.CENTER);
        surface.add(footer, BorderLayout.SOUTH);

        page.add(surface);
        frame.showCustom("Consultas", page);
    }

    private static void styleCombo(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 21));
        combo.setBackground(Color.WHITE);
        combo.setForeground(UiKit.INK);
        combo.setPreferredSize(new Dimension(220, 50));
        combo.setMinimumSize(new Dimension(120, 50));
    }

    private static JPanel lineWithButton(JComponent field, JButton button) {
        JPanel line = new JPanel(new BorderLayout(10, 0));
        line.setOpaque(false);
        line.add(field);
        line.add(button, BorderLayout.EAST);
        return line;
    }

    private static JPanel iconField(JTextField field, String icon) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(Color.WHITE);
        box.setBorder(new LineBorder(new Color(133, 164, 219), 1, true));
        field.setBorder(new EmptyBorder(8, 12, 8, 6));
        field.setPreferredSize(new Dimension(180, 50));
        box.add(field);
        JLabel glyph = new JLabel(UiKit.vectorIcon(icon, 28, new Color(13, 42, 88)));
        glyph.setBorder(new EmptyBorder(0, 9, 0, 12));
        box.add(glyph, BorderLayout.EAST);
        return box;
    }

    private static JPanel fieldBlock(String label, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(0, 7));
        panel.setOpaque(false);
        JLabel name = new JLabel(label);
        name.setFont(new Font("Segoe UI", Font.BOLD, 21));
        name.setForeground(new Color(10, 20, 42));
        panel.add(name, BorderLayout.NORTH);
        panel.add(component);
        return panel;
    }

    private static JButton largeSecondary(String text, String icon, int width) {
        JButton button = new UiKit.RoundButton(text, UiKit.vectorIcon(icon, 23, UiKit.BLUE), new Color(235, 241, 251), UiKit.INK);
        button.setFont(new Font("Segoe UI", Font.BOLD, 18));
        button.setPreferredSize(new Dimension(width, 50));
        return button;
    }

    private static JLabel summaryValue(String text) {
        JLabel value = new JLabel(text);
        value.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        value.setForeground(new Color(10, 25, 48));
        return value;
    }

    private static JComponent summaryPanel(JLabel client, JLabel type, JLabel date, JLabel employee) {
        UiKit.RoundPanel card = new UiKit.RoundPanel(15)
                .fill(new Color(246, 249, 255))
                .line(new Color(145, 174, 224))
                .shadow(false);
        card.setPreferredSize(new Dimension(350, 408));
        card.setLayout(new BorderLayout());
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(225, 236, 252));
        header.setBorder(new EmptyBorder(14, 18, 14, 18));
        header.add(LivroCaixaApp.title("Resumo da consulta", 26));
        card.add(header, BorderLayout.NORTH);
        JPanel rows = new JPanel();
        rows.setOpaque(false);
        rows.setBorder(new EmptyBorder(12, 18, 14, 18));
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        addSummaryRow(rows, "Cliente", client);
        addSummaryRow(rows, "Tipo de consulta", type);
        addSummaryRow(rows, "Data", date);
        addSummaryRow(rows, "Funcionário", employee);
        card.add(rows);
        return card;
    }

    private static void addSummaryRow(JPanel rows, String label, JLabel value) {
        JLabel heading = new JLabel(label);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
        heading.setForeground(new Color(12, 25, 52));
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        value.setAlignmentX(Component.LEFT_ALIGNMENT);
        rows.add(heading);
        rows.add(Box.createVerticalStrut(3));
        rows.add(value);
        rows.add(Box.createVerticalStrut(10));
        JSeparator separator = new JSeparator();
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        rows.add(separator);
        rows.add(Box.createVerticalStrut(13));
    }

    private static JButton actionButton(String text, String icon, boolean primary, int width) {
        Color iconColor = primary ? Color.WHITE : UiKit.INK;
        JButton button = new UiKit.RoundButton(
                text,
                UiKit.vectorIcon(icon, 27, iconColor),
                primary ? UiKit.BLUE : new Color(235, 241, 251),
                primary ? Color.WHITE : UiKit.INK
        );
        button.setFont(new Font("Segoe UI", Font.BOLD, 21));
        button.setPreferredSize(new Dimension(width, 56));
        return button;
    }
}
