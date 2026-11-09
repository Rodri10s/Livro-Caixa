package livrocaixa.view;

import livrocaixa.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/** Telas de listagem desenhadas individualmente conforme os protótipos 02 e 04. */
final class OperationalListScreens {
    private static final Color FILTER_BG = new Color(242, 247, 255);
    private static final Color HEADER_BG = new Color(225, 235, 249);

    private OperationalListScreens() {}

    static JComponent buildClients(LivroCaixaView.MainFrame frame) {
        JPanel root = LivroCaixaView.page();
        root.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel north = new JPanel(new BorderLayout(0, 16));
        north.setOpaque(false);
        north.add(LivroCaixaView.title("Cadastro de clientes", 28), BorderLayout.NORTH);

        UiKit.RoundPanel filters = new UiKit.RoundPanel(15).fill(FILTER_BG).shadow(false);
        filters.setLayout(new GridBagLayout());
        filters.setBorder(new EmptyBorder(17, 20, 17, 20));
        GridBagConstraints g = baseGrid();

        JLabel filterLabel = LivroCaixaView.title("Nome ou CPF", 18);
        g.gridx = 0; g.weightx = 0; g.insets = new Insets(0, 0, 0, 14);
        filters.add(filterLabel, g);

        JTextField search = LivroCaixaView.modernField("");
        search.setPreferredSize(new Dimension(370, 48));
        g.gridx = 1; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(0, 0, 0, 16);
        filters.add(search, g);

        JButton find = primary("Buscar", "search");
        JButton clear = secondary("Limpar", "clear");
        JButton add = primary("Novo cliente", "user");
        dimension(find, 160, 52); dimension(clear, 150, 52); dimension(add, 220, 52);
        g.weightx = 0; g.fill = GridBagConstraints.NONE; g.insets = new Insets(0, 0, 0, 12);
        g.gridx = 2; filters.add(find, g);
        g.gridx = 3; filters.add(clear, g);
        g.gridx = 4; g.insets = new Insets(0, 0, 0, 0); filters.add(add, g);
        north.add(filters);
        root.add(north, BorderLayout.NORTH);

        List<Client> source = new ArrayList<>(frame.store.clients);
        source.sort(Comparator.comparingInt(c -> c.id));
        DefaultTableModel model = new DefaultTableModel(new String[]{"Código", "Nome", "CPF", "Ações"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        for (Client c : source) model.addRow(new Object[]{String.format("%03d", c.id), c.name, c.cpf, ""});

        JTable table = UiKit.modernTable(model);
        table.setRowHeight(62);
        table.getTableHeader().setPreferredSize(new Dimension(0, 54));
        table.getColumnModel().getColumn(3).setCellRenderer(new ActionRenderer(ActionKind.CLIENT));
        setWidths(table.getColumnModel(), new int[]{130, 360, 310, 340});

        TableRowSorter<DefaultTableModel> sorter = castSorter(table);
        Runnable applyFilter = () -> {
            String query = search.getText().trim();
            sorter.setRowFilter(query.isBlank() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(query), 1, 2));
        };
        find.addActionListener(e -> applyFilter.run());
        search.addActionListener(e -> applyFilter.run());
        clear.addActionListener(e -> { search.setText(""); applyFilter.run(); });
        add.addActionListener(e -> frame.clientDialog(null));

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int viewRow = table.rowAtPoint(e.getPoint()), col = table.columnAtPoint(e.getPoint());
                if (viewRow < 0 || col != 3) return;
                int row = table.convertRowIndexToModel(viewRow);
                int id = Integer.parseInt(String.valueOf(model.getValueAt(row, 0)));
                Client client = frame.store.clients.stream().filter(c -> c.id == id).findFirst().orElse(null);
                if (client == null) return;
                Rectangle cell = table.getCellRect(viewRow, col, true);
                if (e.getX() - cell.x < cell.width / 2) frame.clientDialog(client);
                else if (LivroCaixaView.confirm(frame, "Excluir este cliente?")) {
                    frame.store.clients.remove(client); frame.store.save(); frame.showPage("Clientes");
                }
            }
        });

        UiKit.RoundPanel tableCard = new UiKit.RoundPanel(15).fill(Color.WHITE).shadow(false);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(0, 0, 0, 0));
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(UiKit.LINE));
        tableCard.add(scroll);
        JLabel footer = new JLabel(frame.store.clients.size() + " clientes cadastrados");
        footer.setFont(new Font("Segoe UI", Font.BOLD, 18));
        footer.setForeground(UiKit.INK);
        footer.setOpaque(true); footer.setBackground(HEADER_BG); footer.setBorder(new EmptyBorder(13, 18, 13, 18));
        tableCard.add(footer, BorderLayout.SOUTH);
        JPanel holder = new JPanel(new BorderLayout()); holder.setOpaque(false); holder.setBorder(new EmptyBorder(16, 0, 0, 0)); holder.add(tableCard);
        root.add(holder);
        return root;
    }

    static JComponent buildAppointments(LivroCaixaView.MainFrame frame) {
        JPanel root = LivroCaixaView.page();
        root.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel upper = new JPanel(new BorderLayout(0, 16)); upper.setOpaque(false);
        UiKit.RoundPanel filters = new UiKit.RoundPanel(15).fill(FILTER_BG).shadow(false);
        filters.setLayout(new GridBagLayout()); filters.setBorder(new EmptyBorder(17, 18, 17, 18));
        GridBagConstraints g = baseGrid();

        JTextField initial = LivroCaixaView.modernField("01/11/2026");
        JTextField end = LivroCaixaView.modernField("30/11/2026");
        JComboBox<String> client = combo(withAll(frame.store.clients.stream().map(c -> c.name).toList()));
        JComboBox<String> type = combo(withAll(frame.store.appointmentTypes));
        JComboBox<String> employee = combo(List.of("Todos", frame.employee, "Bruno Reis"));
        Component[] controls = {dateField(initial), dateField(end), client, type, employee};
        String[] labels = {"Data inicial", "Data final", "Cliente", "Tipo de consulta", "Funcionário"};
        double[] weights = {.9, .9, 1.0, 1.05, 1.1};
        for (int i = 0; i < labels.length; i++) {
            g.gridx = i; g.gridy = 0; g.weightx = weights[i]; g.fill = GridBagConstraints.HORIZONTAL;
            g.insets = new Insets(0, 0, 7, i == labels.length - 1 ? 0 : 16);
            filters.add(LivroCaixaView.title(labels[i], 17), g);
            g.gridy = 1; g.insets = new Insets(0, 0, 0, i == labels.length - 1 ? 0 : 16);
            controls[i].setPreferredSize(new Dimension(190, 48)); filters.add(controls[i], g);
        }
        JButton apply = primary("Aplicar filtros", "filter"); JButton clear = secondary("Limpar", "clear");
        dimension(apply, 220, 54); dimension(clear, 165, 54);
        g.gridy = 2; g.gridx = 0; g.gridwidth = 1; g.weightx = 0; g.fill = GridBagConstraints.NONE; g.anchor = GridBagConstraints.WEST; g.insets = new Insets(18, 0, 0, 12);
        filters.add(apply, g); g.gridx = 1; g.insets = new Insets(18, 0, 0, 0); filters.add(clear, g);
        upper.add(filters, BorderLayout.NORTH);
        JPanel createBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); createBar.setOpaque(false);
        JButton create = primary("Nova consulta", "newDocument"); dimension(create, 260, 56); create.addActionListener(e -> frame.appointmentDialog(null)); createBar.add(create);
        upper.add(createBar);
        root.add(upper, BorderLayout.NORTH);

        List<Appointment> source = new ArrayList<>(frame.store.appointments);
        source.sort(Comparator.comparingInt((Appointment a) -> a.id).reversed());
        DefaultTableModel model = new DefaultTableModel(new String[]{"Código", "Data", "Cliente", "Tipo", "Funcionário", "Ações"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        for (Appointment a : source) model.addRow(new Object[]{a.id, LivroCaixaView.fmt(a.date), a.client, a.type, a.employee, ""});
        JTable table = UiKit.modernTable(model); table.setRowHeight(62); table.getTableHeader().setPreferredSize(new Dimension(0, 54));
        table.getColumnModel().getColumn(5).setCellRenderer(new ActionRenderer(ActionKind.APPOINTMENT));
        setWidths(table.getColumnModel(), new int[]{105, 145, 225, 250, 215, 340});
        TableRowSorter<DefaultTableModel> sorter = castSorter(table);
        Runnable applyFilters = () -> {
            LocalDate from = safeDate(initial.getText(), LocalDate.MIN), to = safeDate(end.getText(), LocalDate.MAX);
            String cl = String.valueOf(client.getSelectedItem()), ty = String.valueOf(type.getSelectedItem()), em = String.valueOf(employee.getSelectedItem());
            sorter.setRowFilter(new RowFilter<>() {
                @Override public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    LocalDate date = safeDate(String.valueOf(entry.getValue(1)), LocalDate.MIN);
                    return !date.isBefore(from) && !date.isAfter(to)
                            && ("Todos".equals(cl) || cl.equals(String.valueOf(entry.getValue(2))))
                            && ("Todos".equals(ty) || ty.equals(String.valueOf(entry.getValue(3))))
                            && ("Todos".equals(em) || em.equals(String.valueOf(entry.getValue(4))));
                }
            });
        };
        apply.addActionListener(e -> applyFilters.run());
        clear.addActionListener(e -> {initial.setText("01/11/2026");end.setText("30/11/2026");client.setSelectedIndex(0);type.setSelectedIndex(0);employee.setSelectedIndex(0);sorter.setRowFilter(null);});
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int viewRow = table.rowAtPoint(e.getPoint()), col = table.columnAtPoint(e.getPoint());
                if (viewRow < 0 || col != 5) return;
                int row = table.convertRowIndexToModel(viewRow), id = Integer.parseInt(String.valueOf(model.getValueAt(row, 0)));
                Appointment appointment = frame.store.appointments.stream().filter(a -> a.id == id).findFirst().orElse(null);
                if (appointment == null) return;
                Rectangle cell = table.getCellRect(viewRow, col, true);
                if (e.getX() - cell.x < cell.width / 2) frame.appointmentDialog(appointment);
                else if (appointment.billed) LivroCaixaView.error(frame, "Esta consulta já foi faturada.");
                else frame.billingDialog(appointment);
            }
        });
        UiKit.RoundPanel tableCard = new UiKit.RoundPanel(15).fill(Color.WHITE).shadow(false); tableCard.setLayout(new BorderLayout());
        JScrollPane scroll = new JScrollPane(table); scroll.setBorder(new LineBorder(UiKit.LINE)); tableCard.add(scroll);
        JPanel holder = new JPanel(new BorderLayout()); holder.setOpaque(false); holder.setBorder(new EmptyBorder(16, 0, 0, 0)); holder.add(tableCard);
        root.add(holder);
        return root;
    }

    private static GridBagConstraints baseGrid() {
        GridBagConstraints g = new GridBagConstraints(); g.gridy = 0; g.anchor = GridBagConstraints.CENTER;
        return g;
    }

    private static JPanel dateField(JTextField field) {
        JPanel panel = new JPanel(new BorderLayout()); panel.setBackground(Color.WHITE); panel.setBorder(new LineBorder(new Color(163, 181, 211)));
        field.setBorder(new EmptyBorder(7, 10, 7, 3)); panel.add(field); JLabel icon = new JLabel(UiKit.vectorIcon("calendar", 22, UiKit.INK)); icon.setBorder(new EmptyBorder(0, 8, 0, 10)); panel.add(icon, BorderLayout.EAST); return panel;
    }

    private static JComboBox<String> combo(List<String> values) {
        JComboBox<String> box = new JComboBox<>(values.toArray(String[]::new)); box.setFont(new Font("Segoe UI", Font.PLAIN, 17)); return box;
    }

    private static List<String> withAll(List<String> values) {List<String> result = new ArrayList<>(); result.add("Todos"); result.addAll(values); return result;}
    private static LocalDate safeDate(String value, LocalDate fallback) {try {return LivroCaixaView.parseDate(value);} catch (Exception ignored) {return fallback;}}

    @SuppressWarnings("unchecked") private static TableRowSorter<DefaultTableModel> castSorter(JTable table) {return (TableRowSorter<DefaultTableModel>) table.getRowSorter();}
    private static JButton primary(String text, String icon) {return new UiKit.RoundButton(text, listIcon(icon, 22, Color.WHITE), UiKit.BLUE, Color.WHITE);}
    private static JButton secondary(String text, String icon) {return new UiKit.RoundButton(text, listIcon(icon, 21, UiKit.INK), new Color(233, 240, 250), UiKit.INK);}
    private static Icon listIcon(String name,int size,Color color){
        if(!name.equals("filter")&&!name.equals("clear")&&!name.equals("newDocument"))return UiKit.vectorIcon(name,size,color);
        return new Icon(){public int getIconWidth(){return size;}public int getIconHeight(){return size;}public void paintIcon(Component c,Graphics graphics,int x,int y){Graphics2D g=(Graphics2D)graphics.create();UiKit.antialias(g);g.translate(x,y);g.scale(size/24d,size/24d);g.setColor(color);g.setStroke(new BasicStroke(2f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));if(name.equals("filter")){Polygon p=new Polygon(new int[]{2,22,14,14,10,10},new int[]{3,3,11,20,22,11},6);g.drawPolygon(p);}else if(name.equals("clear")){g.drawLine(5,5,19,19);g.drawLine(19,5,5,19);}else{g.drawRoundRect(4,2,13,20,2,2);g.drawLine(8,7,14,7);g.drawLine(8,11,14,11);g.drawLine(19,13,19,23);g.drawLine(14,18,24,18);}g.dispose();}};
    }
    private static void dimension(JComponent c, int width, int height) {Dimension d = new Dimension(width, height); c.setPreferredSize(d); c.setMinimumSize(d);}
    private static void setWidths(TableColumnModel columns, int[] widths) {for(int i=0;i<widths.length;i++) columns.getColumn(i).setPreferredWidth(widths[i]);}

    private enum ActionKind { CLIENT, APPOINTMENT }
    private static final class ActionRenderer implements TableCellRenderer {
        private final ActionKind kind;
        ActionRenderer(ActionKind kind) {this.kind = kind;}
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
            JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0)); panel.setBorder(new EmptyBorder(7, 12, 7, 12)); panel.setOpaque(true);
            panel.setBackground(selected ? table.getSelectionBackground() : (row & 1) == 0 ? Color.WHITE : new Color(249, 251, 255));
            JButton first = new UiKit.RoundButton("Editar", UiKit.vectorIcon("edit", 17, kind == ActionKind.CLIENT ? UiKit.BLUE : UiKit.INK), new Color(231, 240, 252), kind == ActionKind.CLIENT ? UiKit.BLUE : UiKit.INK);
            JButton second = kind == ActionKind.CLIENT
                    ? new UiKit.RoundButton("Excluir", UiKit.vectorIcon("trash", 17, UiKit.RED), new Color(255, 230, 232), UiKit.RED)
                    : new UiKit.RoundButton("Faturar", UiKit.vectorIcon("save", 17, Color.WHITE), UiKit.BLUE, Color.WHITE);
            first.setFont(new Font("Segoe UI", Font.BOLD, 14)); second.setFont(new Font("Segoe UI", Font.BOLD, 14));
            first.setBorder(new EmptyBorder(6, 10, 6, 10)); second.setBorder(new EmptyBorder(6, 10, 6, 10)); panel.add(first); panel.add(second); return panel;
        }
    }
}
