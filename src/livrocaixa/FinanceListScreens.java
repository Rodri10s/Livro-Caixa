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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Telas financeiras de listagem, desenhadas conforme os protótipos 08 e 11. */
final class FinanceListScreens {
    private static final LocalDate REFERENCE_DATE = LocalDate.of(2026, 11, 5);

    private FinanceListScreens() {}

    static JComponent buildReceivables(LivroCaixaApp.MainFrame frame) {
        JPanel page = LivroCaixaApp.page();
        UiKit.RoundPanel surface = new UiKit.RoundPanel(16).fill(Color.WHITE);
        surface.setLayout(new BorderLayout(0, 15));
        surface.setBorder(new EmptyBorder(17, 18, 17, 18));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JLabel heading = LivroCaixaApp.title("Contas a receber", 29);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(heading);
        top.add(Box.createVerticalStrut(14));

        JPanel cards = new JPanel(new GridLayout(1, 3, 14, 0));
        cards.setOpaque(false);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 118));
        cards.setPreferredSize(new Dimension(0, 118));
        cards.add(financialCard("A receber", LivroCaixaApp.money(frame.store.pending()), UiKit.BLUE,
                new Color(236, 243, 255), "coins"));
        BigDecimal overdue = frame.store.installments.stream()
                .filter(x -> !x.cancelled && x.due.isBefore(REFERENCE_DATE) && x.paid.compareTo(x.value) < 0)
                .map(x -> x.value.subtract(x.paid)).reduce(BigDecimal.ZERO, BigDecimal::add);
        cards.add(financialCard("Vencido", LivroCaixaApp.money(overdue), new Color(205, 23, 31),
                new Color(255, 232, 233), "warning"));
        cards.add(financialCard("Recebido no mês", LivroCaixaApp.money(frame.store.income()), new Color(8, 119, 30),
                new Color(229, 247, 227), "check"));
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(cards);
        top.add(Box.createVerticalStrut(14));

        JTextField start = LivroCaixaApp.modernField("01/11/2026");
        JTextField end = LivroCaixaApp.modernField("30/11/2026");
        JComboBox<String> client = new JComboBox<>();
        client.addItem("Todos");
        frame.store.clients.forEach(c -> client.addItem(c.name));
        JComboBox<String> status = new JComboBox<>(new String[]{"Todos", "Pendente", "Vencido", "Pago", "Cancelada"});
        UiKit.RoundPanel filters = new UiKit.RoundPanel(14).fill(new Color(245, 248, 255)).shadow(false);
        filters.setBorder(new EmptyBorder(11, 12, 11, 12));
        filters.setLayout(new GridBagLayout());
        GridBagConstraints gc = constraints();
        addFilter(filters, gc, 0, "Data inicial", start, 1.0);
        addFilter(filters, gc, 1, "Data final", end, 1.0);
        addFilter(filters, gc, 2, "Cliente", client, 1.15);
        addFilter(filters, gc, 3, "Status", status, 1.0);
        JButton apply = UiKit.primary("Aplicar filtros", "filter");
        JButton clear = UiKit.secondary("Limpar", "refresh");
        gc.gridx = 4; gc.weightx = 0; gc.fill = GridBagConstraints.NONE; gc.anchor = GridBagConstraints.SOUTH;
        gc.insets = new Insets(20, 8, 0, 0); filters.add(apply, gc);
        gc.gridx = 5; gc.insets = new Insets(20, 8, 0, 0); filters.add(clear, gc);
        filters.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(filters);
        surface.add(top, BorderLayout.NORTH);

        String[] columns = {"Cliente", "Fatura", "Parcela", "Vencimento", "Valor", "Status", "Ações"};
        DefaultTableModel model = readOnlyModel(columns);
        JTable table = UiKit.modernTable(model);
        table.setRowHeight(48);
        table.getColumnModel().getColumn(1).setMaxWidth(110);
        table.getColumnModel().getColumn(2).setMaxWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(135);
        table.getColumnModel().getColumn(5).setPreferredWidth(125);
        table.getColumnModel().getColumn(6).setPreferredWidth(330);
        table.getColumnModel().getColumn(6).setCellRenderer(new DualActionRenderer("Registrar pagamento", "Detalhes", true));
        List<LivroCaixaApp.Installment> visible = new ArrayList<>();

        Runnable refresh = () -> {
            LocalDate from = parseDateOr(start.getText(), LocalDate.MIN);
            LocalDate to = parseDateOr(end.getText(), LocalDate.MAX);
            String chosenClient = String.valueOf(client.getSelectedItem());
            String chosenStatus = String.valueOf(status.getSelectedItem());
            visible.clear(); model.setRowCount(0);
            frame.store.installments.stream().filter(x -> !x.due.isBefore(from) && !x.due.isAfter(to))
                    .filter(x -> "Todos".equals(chosenClient) || x.client.equals(chosenClient))
                    .forEach(x -> {
                        String state = installmentStatus(x);
                        if (!"Todos".equals(chosenStatus) && !state.equals(chosenStatus)) return;
                        visible.add(x);
                        model.addRow(new Object[]{x.client, String.format("%03d", x.appointmentId), x.label,
                                LivroCaixaApp.fmt(x.due), LivroCaixaApp.money(x.value), state, ""});
                    });
        };
        apply.addActionListener(e -> refresh.run());
        clear.addActionListener(e -> { start.setText("01/11/2026"); end.setText("30/11/2026"); client.setSelectedIndex(0); status.setSelectedIndex(0); refresh.run(); });
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int viewRow = table.rowAtPoint(e.getPoint()), col = table.columnAtPoint(e.getPoint());
                if (viewRow < 0 || col != 6) return;
                int row = table.convertRowIndexToModel(viewRow);
                if (row >= visible.size()) return;
                LivroCaixaApp.Installment item = visible.get(row);
                Rectangle cell = table.getCellRect(viewRow, col, true);
                if (e.getX() - cell.x < cell.width * .68) {
                    if (item.paid.compareTo(item.value) >= 0) frame.paymentConfirmed(item); else frame.paymentDialog(item);
                } else {
                    JOptionPane.showMessageDialog(frame,
                            "Cliente: " + item.client + "\nFatura: " + item.appointmentId + "\nParcela: " + item.label
                                    + "\nVencimento: " + LivroCaixaApp.fmt(item.due) + "\nValor: " + LivroCaixaApp.money(item.value)
                                    + "\nRecebido: " + LivroCaixaApp.money(item.paid),
                            "Detalhes da parcela", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        refresh.run();
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(UiKit.LINE));
        surface.add(scroll);
        page.add(surface);
        return page;
    }

    static JComponent buildExpenses(LivroCaixaApp.MainFrame frame) {
        JPanel page = LivroCaixaApp.page();
        UiKit.RoundPanel surface = new UiKit.RoundPanel(16).fill(Color.WHITE);
        surface.setLayout(new BorderLayout(0, 12));
        surface.setBorder(new EmptyBorder(17, 18, 17, 18));

        JPanel top = new JPanel();
        top.setOpaque(false); top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JLabel heading = LivroCaixaApp.title("Controle de saídas", 29); heading.setAlignmentX(Component.LEFT_ALIGNMENT); top.add(heading); top.add(Box.createVerticalStrut(12));
        JTextField start = LivroCaixaApp.modernField("01/11/2026"), end = LivroCaixaApp.modernField("30/11/2026");
        JComboBox<String> type = new JComboBox<>(); type.addItem("Todos"); frame.store.expenseTypes.forEach(type::addItem);
        JComboBox<String> employee = new JComboBox<>(new String[]{"Todos", frame.employee, "Bruno Reis"});
        UiKit.RoundPanel filters = new UiKit.RoundPanel(14).fill(new Color(245, 248, 255)).shadow(false);
        filters.setBorder(new EmptyBorder(11, 12, 11, 12)); filters.setLayout(new GridBagLayout());
        GridBagConstraints gc = constraints();
        addFilter(filters, gc, 0, "Data inicial", start, 1.0); addFilter(filters, gc, 1, "Data final", end, 1.0);
        addFilter(filters, gc, 2, "Tipo de saída", type, 1.15); addFilter(filters, gc, 3, "Funcionário", employee, 1.0);
        JButton apply = UiKit.primary("Aplicar filtros", "filter"), clear = UiKit.secondary("Limpar", "refresh");
        gc.gridx=4;gc.weightx=0;gc.fill=GridBagConstraints.NONE;gc.anchor=GridBagConstraints.SOUTH;gc.insets=new Insets(20,8,0,0);filters.add(apply,gc);
        gc.gridx=5;filters.add(clear,gc);filters.setAlignmentX(Component.LEFT_ALIGNMENT);top.add(filters);top.add(Box.createVerticalStrut(11));
        JPanel addRow=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));addRow.setOpaque(false);JButton add=UiKit.primary("Nova saída","plus");add.addActionListener(e->frame.expenseDialog(null));addRow.add(add);addRow.setAlignmentX(Component.LEFT_ALIGNMENT);top.add(addRow);
        surface.add(top,BorderLayout.NORTH);

        String[] columns={"Código","Data","Tipo de saída","Funcionário","Valor","Ações"};DefaultTableModel model=readOnlyModel(columns);JTable table=UiKit.modernTable(model);table.setRowHeight(43);
        table.getColumnModel().getColumn(0).setMaxWidth(115);table.getColumnModel().getColumn(1).setPreferredWidth(145);table.getColumnModel().getColumn(4).setPreferredWidth(165);table.getColumnModel().getColumn(5).setPreferredWidth(225);table.getColumnModel().getColumn(5).setCellRenderer(new DualActionRenderer("Editar","Excluir",false));
        List<LivroCaixaApp.Expense> visible=new ArrayList<>();JLabel totalValue=LivroCaixaApp.title(LivroCaixaApp.money(BigDecimal.ZERO),22);totalValue.setForeground(Color.WHITE);
        Runnable refresh=()->{LocalDate from=parseDateOr(start.getText(),LocalDate.MIN),to=parseDateOr(end.getText(),LocalDate.MAX);String chosenType=String.valueOf(type.getSelectedItem()),chosenEmployee=String.valueOf(employee.getSelectedItem());visible.clear();model.setRowCount(0);BigDecimal[] total={BigDecimal.ZERO};frame.store.expenses.stream().filter(x->!x.date.isBefore(from)&&!x.date.isAfter(to)).filter(x->"Todos".equals(chosenType)||x.type.equals(chosenType)).filter(x->"Todos".equals(chosenEmployee)||x.employee.equals(chosenEmployee)).forEach(x->{visible.add(x);total[0]=total[0].add(x.value);model.addRow(new Object[]{String.format("%03d",x.id),LivroCaixaApp.fmt(x.date),x.type,x.employee,LivroCaixaApp.money(x.value),""});});totalValue.setText(LivroCaixaApp.money(total[0]));};
        apply.addActionListener(e->refresh.run());clear.addActionListener(e->{start.setText("01/11/2026");end.setText("30/11/2026");type.setSelectedIndex(0);employee.setSelectedIndex(0);refresh.run();});
        table.addMouseListener(new MouseAdapter(){@Override public void mouseClicked(MouseEvent e){int viewRow=table.rowAtPoint(e.getPoint()),col=table.columnAtPoint(e.getPoint());if(viewRow<0||col!=5)return;int row=table.convertRowIndexToModel(viewRow);if(row>=visible.size())return;LivroCaixaApp.Expense item=visible.get(row);Rectangle cell=table.getCellRect(viewRow,col,true);if(e.getX()-cell.x<cell.width/2)frame.expenseDialog(item);else if(LivroCaixaApp.confirm(frame,"Excluir esta saída?")){frame.store.expenses.remove(item);frame.store.save();refresh.run();}}});
        refresh.run();JScrollPane scroll=new JScrollPane(table);scroll.setBorder(new LineBorder(UiKit.LINE));surface.add(scroll);
        JPanel footer=new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));footer.setOpaque(false);UiKit.RoundPanel totalBox=new UiKit.RoundPanel(13).fill(new Color(244,248,255)).shadow(false);totalBox.setLayout(new BorderLayout());totalBox.setBorder(new EmptyBorder(5,18,5,5));JLabel totalLabel=LivroCaixaApp.title("Total do período",18);totalLabel.setBorder(new EmptyBorder(0,0,0,20));totalBox.add(totalLabel);UiKit.RoundPanel blue=new UiKit.RoundPanel(11).fill(new Color(25,68,210)).line(null).shadow(false);blue.setBorder(new EmptyBorder(8,20,8,20));blue.add(totalValue);totalBox.add(blue,BorderLayout.EAST);footer.add(totalBox);surface.add(footer,BorderLayout.SOUTH);
        page.add(surface);return page;
    }

    private static UiKit.RoundPanel financialCard(String label,String value,Color color,Color fill,String icon){UiKit.RoundPanel card=new UiKit.RoundPanel(15).fill(fill).line(new Color(color.getRed(),color.getGreen(),color.getBlue(),105)).shadow(false);card.setLayout(new BorderLayout(14,0));card.setBorder(new EmptyBorder(17,22,17,22));JLabel glyph=new JLabel(UiKit.vectorIcon(icon,48,color));glyph.setBorder(new EmptyBorder(0,0,0,18));card.add(glyph,BorderLayout.WEST);JPanel text=new JPanel();text.setOpaque(false);text.setLayout(new BoxLayout(text,BoxLayout.Y_AXIS));text.add(LivroCaixaApp.title(label,20));text.add(Box.createVerticalStrut(8));JLabel amount=LivroCaixaApp.title(value,28);amount.setForeground(color);text.add(amount);card.add(text);return card;}
    private static GridBagConstraints constraints(){GridBagConstraints gc=new GridBagConstraints();gc.gridy=0;gc.weighty=1;gc.fill=GridBagConstraints.HORIZONTAL;gc.anchor=GridBagConstraints.WEST;return gc;}
    private static void addFilter(JPanel target,GridBagConstraints base,int x,String label,JComponent field,double weight){GridBagConstraints gc=(GridBagConstraints)base.clone();gc.gridx=x;gc.weightx=weight;gc.insets=new Insets(0,x==0?0:8,0,0);JPanel box=new JPanel(new BorderLayout(0,4));box.setOpaque(false);JLabel l=new JLabel(label);l.setFont(new Font("Segoe UI",Font.BOLD,15));box.add(l,BorderLayout.NORTH);field.setPreferredSize(new Dimension(150,39));box.add(field);target.add(box,gc);}
    private static DefaultTableModel readOnlyModel(String[] columns){return new DefaultTableModel(columns,0){@Override public boolean isCellEditable(int r,int c){return false;}};}
    private static LocalDate parseDateOr(String value,LocalDate fallback){try{return LivroCaixaApp.parseDate(value);}catch(Exception e){return fallback;}}
    private static String installmentStatus(LivroCaixaApp.Installment x){if(x.cancelled)return "Cancelada";if(x.paid.compareTo(x.value)>=0)return "Pago";return x.due.isBefore(REFERENCE_DATE)?"Vencido":"Pendente";}

    private static final class DualActionRenderer implements TableCellRenderer {
        private final String first,second;private final boolean primaryFirst;
        DualActionRenderer(String first,String second,boolean primaryFirst){this.first=first;this.second=second;this.primaryFirst=primaryFirst;}
        @Override public Component getTableCellRendererComponent(JTable table,Object value,boolean selected,boolean focus,int row,int column){JPanel p=new JPanel(new FlowLayout(FlowLayout.CENTER,7,4));p.setOpaque(true);p.setBackground(selected?table.getSelectionBackground():(row&1)==0?Color.WHITE:new Color(249,251,255));boolean paid=primaryFirst&&table.getColumnCount()>5&&"Pago".equals(String.valueOf(table.getValueAt(row,5)));String firstLabel=paid?"Ver pagamentos":first;JButton a=primaryFirst&&!paid?UiKit.primary(firstLabel,"wallet"):UiKit.secondary(firstLabel,primaryFirst?"wallet":"edit");JButton b=UiKit.secondary(second,primaryFirst?"document":"trash");a.setFont(new Font("Segoe UI",Font.BOLD,12));b.setFont(new Font("Segoe UI",Font.BOLD,12));a.setBorder(new EmptyBorder(5,9,5,9));b.setBorder(new EmptyBorder(5,9,5,9));if(!primaryFirst)b.setForeground(new Color(205,23,31));p.add(a);p.add(b);return p;}
    }
}
