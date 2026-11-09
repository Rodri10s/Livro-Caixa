package livrocaixa.view;

import livrocaixa.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Fluxos internos de faturamento e pagamento fiéis aos protótipos 06a, 06b e 09. */
final class PaymentScreens {
    private PaymentScreens() {}

    static void showBilling(LivroCaixaView.MainFrame frame, Appointment appointment) {
        JPanel page = LivroCaixaView.page();
        UiKit.RoundPanel surface = new UiKit.RoundPanel(16).fill(Color.WHITE);
        surface.setLayout(new BorderLayout(0, 13));
        surface.setBorder(new EmptyBorder(15, 18, 15, 18));

        JPanel header = new JPanel(new BorderLayout(0, 11)); header.setOpaque(false);
        header.add(LivroCaixaView.title("Faturamento da consulta", 29), BorderLayout.NORTH);
        UiKit.RoundPanel banner = new UiKit.RoundPanel(12).fill(new Color(232, 241, 255)).shadow(false);
        banner.setBorder(new EmptyBorder(11, 15, 11, 15)); banner.setLayout(new GridLayout(1, 5));
        banner.add(LivroCaixaView.title("Consulta nº " + appointment.id, 17));
        banner.add(bannerValue("Cliente:", appointment.client)); banner.add(bannerValue("Tipo:", appointment.type));
        banner.add(bannerValue("Data:", LivroCaixaView.fmt(appointment.date)));
        banner.add(bannerValue("Valor total:", LivroCaixaView.money(appointment.value)));
        header.add(banner); surface.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(16, 0)); center.setOpaque(false);
        JPanel left = new JPanel(new BorderLayout(0, 10)); left.setOpaque(false);
        JLabel paymentTitle = LivroCaixaView.title("Tipo de pagamento", 19); left.add(paymentTitle, BorderLayout.NORTH);
        JPanel leftBody = new JPanel(new BorderLayout(0, 10)); leftBody.setOpaque(false);
        JPanel segmented = new JPanel(new GridLayout(1, 2, 0, 0)); segmented.setOpaque(false);
        SegmentToggle cash = new SegmentToggle("À vista"), term = new SegmentToggle("À prazo");
        ButtonGroup modes = new ButtonGroup(); modes.add(cash); modes.add(term); cash.setSelected(true); segmented.add(cash); segmented.add(term);
        leftBody.add(segmented, BorderLayout.NORTH);

        CardLayout modeLayout = new CardLayout(); JPanel modeCards = new JPanel(modeLayout); modeCards.setOpaque(false);
        DefaultTableModel cashModel = readOnlyModel(new String[]{"Forma de pagamento", "Valor", "Ação"});
        cashModel.addRow(new Object[]{"PIX", LivroCaixaView.money(new BigDecimal("900.00")), "Remover"});
        cashModel.addRow(new Object[]{"Dinheiro", LivroCaixaView.money(appointment.value.subtract(new BigDecimal("900.00"))), "Remover"});
        JTable cashTable = UiKit.modernTable(cashModel); cashTable.setRowHeight(43);
        cashTable.getColumnModel().getColumn(2).setMaxWidth(150); cashTable.getColumnModel().getColumn(2).setCellRenderer(new RemoveRenderer());
        JPanel cashPanel = new JPanel(new BorderLayout(0, 10)); cashPanel.setOpaque(false);
        cashPanel.add(sectionTitle("Formas de pagamento"), BorderLayout.NORTH);
        JScrollPane cashScroll = new JScrollPane(cashTable); cashScroll.setBorder(new LineBorder(UiKit.LINE)); cashScroll.setPreferredSize(new Dimension(0, 145)); cashPanel.add(cashScroll);
        JButton addMethod = UiKit.primary("Adicionar forma de pagamento", "plus");
        JPanel addMethodRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); addMethodRow.setOpaque(false); addMethodRow.add(addMethod); cashPanel.add(addMethodRow, BorderLayout.SOUTH);
        modeCards.add(cashPanel, "cash");

        JPanel termPanel = new JPanel(new BorderLayout(0, 9)); termPanel.setOpaque(false);
        JPanel fields = new JPanel(new GridLayout(2, 3, 10, 8)); fields.setOpaque(false);
        JTextField entry = LivroCaixaView.modernField("200,00"), entryDate = LivroCaixaView.modernField(LivroCaixaView.fmt(appointment.date));
        JTextField firstDue = LivroCaixaView.modernField(LivroCaixaView.fmt(appointment.date.plusDays(30))), count = LivroCaixaView.modernField("4");
        JComboBox<String> periodicity = new JComboBox<>(new String[]{"Mensal", "Quinzenal", "Semanal"});
        fields.add(fieldBox("Entrada", entry)); fields.add(fieldBox("Data da entrada", entryDate)); fields.add(fieldBox("Primeiro vencimento", firstDue));
        fields.add(fieldBox("Número de parcelas", count)); fields.add(fieldBox("Periodicidade", periodicity));
        JPanel valuesMode = new JPanel(new GridLayout(1, 2)); valuesMode.setOpaque(false); SegmentToggle equal = new SegmentToggle("Iguais"), custom = new SegmentToggle("Personalizados"); ButtonGroup valueModes = new ButtonGroup(); valueModes.add(equal); valueModes.add(custom); equal.setSelected(true); valuesMode.add(equal); valuesMode.add(custom); fields.add(fieldBox("Valores das parcelas", valuesMode));
        termPanel.add(fields, BorderLayout.NORTH);
        DefaultTableModel previewModel = new DefaultTableModel(new String[]{"Parcela", "Tipo", "Vencimento", "Valor", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 3 && custom.isSelected() && !"Entrada".equals(getValueAt(row, 0)); }
        };
        JTable preview = UiKit.modernTable(previewModel); preview.setRowHeight(27);
        JPanel previewWrap = new JPanel(new BorderLayout(0, 5)); previewWrap.setOpaque(false); previewWrap.add(sectionTitle("Prévia das parcelas"), BorderLayout.NORTH);
        JScrollPane previewScroll = new JScrollPane(preview); previewScroll.setBorder(new LineBorder(UiKit.LINE)); previewScroll.setPreferredSize(new Dimension(0, 185)); previewWrap.add(previewScroll);
        JPanel generateRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); generateRow.setOpaque(false); JButton generate = UiKit.primary("Gerar parcelas", "chart"); generateRow.add(generate); previewWrap.add(generateRow, BorderLayout.SOUTH);
        termPanel.add(previewWrap); modeCards.add(termPanel, "term");
        leftBody.add(modeCards); left.add(leftBody); center.add(left);

        JLabel informedValue = summaryValue(LivroCaixaView.money(appointment.value), UiKit.INK);
        JLabel remainingValue = summaryValue(LivroCaixaView.money(BigDecimal.ZERO), new Color(8, 130, 31));
        UiKit.RoundPanel summary = summaryPanel("Resumo", new String[]{"Valor da consulta", "Total informado", "Saldo restante"},
                new JLabel[]{summaryValue(LivroCaixaView.money(appointment.value), UiKit.INK), informedValue, remainingValue});
        summary.setPreferredSize(new Dimension(360, 0)); center.add(summary, BorderLayout.EAST); surface.add(center);

        Runnable recalcCash = () -> {
            BigDecimal total = BigDecimal.ZERO;
            for (int i = 0; i < cashModel.getRowCount(); i++) try { total = total.add(LivroCaixaView.parseMoney(String.valueOf(cashModel.getValueAt(i, 1)))); } catch (Exception ignored) {}
            informedValue.setText(LivroCaixaView.money(total)); BigDecimal rest = appointment.value.subtract(total); remainingValue.setText(LivroCaixaView.money(rest)); remainingValue.setForeground(rest.signum() == 0 ? new Color(8, 130, 31) : UiKit.RED);
        };
        Runnable generateTerms = () -> generateTermRows(appointment, entry, entryDate, firstDue, count, periodicity, previewModel, informedValue, remainingValue);
        cashModel.addTableModelListener(e -> recalcCash.run()); previewModel.addTableModelListener(e -> recalcTermSummary(appointment.value, previewModel, informedValue, remainingValue));
        addMethod.addActionListener(e -> addPaymentMethod(frame, cashModel));
        cashTable.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { int row=cashTable.rowAtPoint(e.getPoint()), col=cashTable.columnAtPoint(e.getPoint()); if(row>=0&&col==2){cashModel.removeRow(cashTable.convertRowIndexToModel(row));recalcCash.run();}}});
        generate.addActionListener(e -> generateTerms.run()); equal.addActionListener(e -> { preview.repaint(); generateTerms.run(); }); custom.addActionListener(e -> preview.repaint());
        cash.addActionListener(e -> { modeLayout.show(modeCards, "cash"); recalcCash.run(); }); term.addActionListener(e -> { modeLayout.show(modeCards, "term"); generateTerms.run(); });
        generateTerms.run(); recalcCash.run();

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); actions.setOpaque(false); JButton back = UiKit.secondary("Voltar", "swap"), save = UiKit.primary("Salvar fatura", "save");
        back.addActionListener(e -> frame.showPage("Consultas"));
        save.addActionListener(e -> {
            try {
                if (cash.isSelected()) saveCashInvoice(frame, appointment, cashModel); else saveTermInvoice(frame, appointment, entry, entryDate, previewModel);
                appointment.billed = true; frame.store.save(); frame.showSuccessPage("Fatura salva com sucesso", "Consultas");
            } catch (Exception ex) { LivroCaixaView.error(frame, ex.getMessage() == null ? "Revise os valores da fatura." : ex.getMessage()); }
        });
        actions.add(back); actions.add(save); surface.add(actions, BorderLayout.SOUTH); page.add(surface); frame.showCustom("Consultas", page);
    }

    static void showPayment(LivroCaixaView.MainFrame frame, Installment item) {
        if (item.cancelled) { LivroCaixaView.error(frame, "Parcela cancelada."); return; }
        if (item.paid.compareTo(item.value) >= 0) { frame.paymentConfirmed(item); return; }
        BigDecimal originalBalance = item.value.subtract(item.paid);
        JPanel page = LivroCaixaView.page(); UiKit.RoundPanel surface = new UiKit.RoundPanel(16).fill(Color.WHITE);
        surface.setLayout(new BorderLayout(0, 13)); surface.setBorder(new EmptyBorder(15, 18, 15, 18));
        surface.add(LivroCaixaView.title("Registrar pagamento", 29), BorderLayout.NORTH);
        JPanel main = new JPanel(new BorderLayout(0, 13)); main.setOpaque(false);
        UiKit.RoundPanel banner = new UiKit.RoundPanel(13).fill(new Color(231, 240, 255)).shadow(false); banner.setLayout(new GridLayout(1, 5)); banner.setBorder(new EmptyBorder(13, 16, 13, 16));
        banner.add(bannerStack("Cliente:", item.client)); banner.add(bannerStack("Fatura:", String.valueOf(item.appointmentId))); banner.add(bannerStack("Parcela:", item.label)); banner.add(bannerStack("Vencimento:", LivroCaixaView.fmt(item.due))); banner.add(bannerStack("Valor da parcela:", LivroCaixaView.money(item.value))); main.add(banner, BorderLayout.NORTH);

        JPanel columns = new JPanel(new GridLayout(1, 2, 15, 0)); columns.setOpaque(false);
        JPanel left = new JPanel(new BorderLayout(0, 11)); left.setOpaque(false);
        UiKit.RoundPanel form = new UiKit.RoundPanel(14).fill(new Color(248, 250, 255)).shadow(false); form.setLayout(new GridBagLayout()); form.setBorder(new EmptyBorder(12, 15, 12, 15));
        JComboBox<String> method = new JComboBox<>(frame.store.paymentMethods.toArray(String[]::new)); method.setSelectedItem("Cartão de crédito");
        BigDecimal suggestedDiscount = originalBalance.min(new BigDecimal("20.00"));
        JTextField received = LivroCaixaView.modernField(originalBalance.subtract(suggestedDiscount).toPlainString()), date = LivroCaixaView.modernField("05/11/2026"), discount = LivroCaixaView.modernField(suggestedDiscount.toPlainString());
        SegmentToggle money = new SegmentToggle("R$"), percent = new SegmentToggle("%"); ButtonGroup discountModes = new ButtonGroup(); discountModes.add(money); discountModes.add(percent); money.setSelected(true); JPanel discountMode = new JPanel(new GridLayout(1,2));discountMode.setOpaque(false);discountMode.add(money);discountMode.add(percent);
        GridBagConstraints gc = new GridBagConstraints(); gc.fill=GridBagConstraints.HORIZONTAL;gc.weightx=1;gc.gridwidth=2;gc.gridx=0;gc.gridy=0;gc.insets=new Insets(0,0,8,0);form.add(fieldBox("Forma de pagamento",method),gc);gc.gridy++;form.add(fieldBox("Valor recebido",received),gc);gc.gridy++;gc.gridwidth=1;gc.weightx=.55;gc.insets=new Insets(0,0,0,9);form.add(fieldBox("Data",date),gc);gc.gridx=1;gc.weightx=.45;gc.insets=new Insets(0,0,0,0);JPanel discountLine=new JPanel(new BorderLayout(8,0));discountLine.setOpaque(false);discountLine.add(discountMode,BorderLayout.WEST);discountLine.add(discount);form.add(fieldBox("Desconto",discountLine),gc);left.add(form,BorderLayout.NORTH);
        Object[][] history = frame.store.payments.stream().filter(p -> p.appointmentId==item.appointmentId && p.label.equals(item.label) && !p.reversed).map(p -> new Object[]{LivroCaixaView.fmt(p.date),p.method,LivroCaixaView.money(p.value)}).toArray(Object[][]::new);
        JPanel historyPanel = tablePanel("Pagamentos já realizados", new String[]{"Data","Forma de pagamento","Valor"}, history); left.add(historyPanel); columns.add(left);

        JLabel discountValue=summaryValue(LivroCaixaView.money(BigDecimal.ZERO),new Color(21,45,120)),currentValue=summaryValue(LivroCaixaView.money(BigDecimal.ZERO),new Color(21,45,120)),balanceValue=summaryValue(LivroCaixaView.money(originalBalance),new Color(8,130,31));
        UiKit.RoundPanel summary=summaryPanel("Resumo do recebimento",new String[]{"Valor da parcela","Já recebido","Desconto","Pagamento atual","Saldo restante"},new JLabel[]{summaryValue(LivroCaixaView.money(item.value),new Color(21,45,120)),summaryValue(LivroCaixaView.money(item.paid),new Color(21,45,120)),discountValue,currentValue,balanceValue});columns.add(summary);main.add(columns);surface.add(main);
        Runnable recalc=()->{try{BigDecimal payment=LivroCaixaView.parseMoney(received.getText()),rawDiscount=LivroCaixaView.parseMoney(discount.getText()),actualDiscount=percent.isSelected()?originalBalance.multiply(rawDiscount).divide(BigDecimal.valueOf(100),2,RoundingMode.HALF_UP):rawDiscount;BigDecimal balance=originalBalance.subtract(payment).subtract(actualDiscount);discountValue.setText(LivroCaixaView.money(actualDiscount));currentValue.setText(LivroCaixaView.money(payment));balanceValue.setText(LivroCaixaView.money(balance));balanceValue.setForeground(balance.signum()<0?UiKit.RED:new Color(8,130,31));}catch(Exception ignored){balanceValue.setText("Revise os valores");balanceValue.setForeground(UiKit.RED);}};
        attach(received,recalc);attach(discount,recalc);money.addActionListener(e->recalc.run());percent.addActionListener(e->recalc.run());recalc.run();
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));actions.setOpaque(false);JButton back=UiKit.secondary("Voltar","swap"),cancel=UiKit.danger("Cancelar parcela"),confirm=UiKit.primary("Confirmar pagamento","wallet");
        back.addActionListener(e->frame.showPage("Contas a receber"));cancel.addActionListener(e->{if(LivroCaixaView.confirm(frame,"Cancelar esta parcela?")){item.cancelled=true;frame.showPage("Contas a receber");}});
        confirm.addActionListener(e->{try{BigDecimal payment=LivroCaixaView.parseMoney(received.getText()),rawDiscount=LivroCaixaView.parseMoney(discount.getText()),actualDiscount=percent.isSelected()?originalBalance.multiply(rawDiscount).divide(BigDecimal.valueOf(100),2,RoundingMode.HALF_UP):rawDiscount;if(payment.signum()<=0||actualDiscount.signum()<0||payment.add(actualDiscount).compareTo(originalBalance)>0)throw new IllegalArgumentException();item.paid=item.paid.add(payment).add(actualDiscount);frame.store.payments.add(new Payment(frame.store.nextPayment++,item.appointmentId,item.client,item.label,LivroCaixaView.parseDate(date.getText()),payment,String.valueOf(method.getSelectedItem()),actualDiscount,false));frame.store.save();frame.paymentConfirmed(item);}catch(Exception ex){LivroCaixaView.error(frame,"Revise o pagamento, o desconto e a data.");}});
        actions.add(back);actions.add(cancel);actions.add(confirm);surface.add(actions,BorderLayout.SOUTH);page.add(surface);frame.showCustom("Contas a receber",page);
    }

    private static JPanel bannerValue(String label,String value){JPanel p=new JPanel(new FlowLayout(FlowLayout.CENTER,5,0));p.setOpaque(false);JLabel a=new JLabel(label);a.setFont(new Font("Segoe UI",Font.BOLD,15));p.add(a);p.add(new JLabel(value));return p;}
    private static JPanel bannerStack(String label,String value){JPanel p=new JPanel();p.setOpaque(false);p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));JLabel a=new JLabel(label);a.setFont(new Font("Segoe UI",Font.BOLD,15));JLabel b=LivroCaixaView.title(value,21);b.setForeground(new Color(23,58,145));p.add(a);p.add(Box.createVerticalStrut(7));p.add(b);return p;}
    private static JLabel sectionTitle(String text){return LivroCaixaView.title(text,18);}
    private static JPanel fieldBox(String label,JComponent component){JPanel p=new JPanel(new BorderLayout(0,4));p.setOpaque(false);JLabel l=new JLabel(label);l.setFont(new Font("Segoe UI",Font.BOLD,15));p.add(l,BorderLayout.NORTH);component.setPreferredSize(new Dimension(130,38));p.add(component);return p;}
    private static DefaultTableModel readOnlyModel(String[] columns){return new DefaultTableModel(columns,0){@Override public boolean isCellEditable(int r,int c){return false;}};}
    private static JLabel summaryValue(String text,Color color){JLabel label=LivroCaixaView.title(text,19);label.setForeground(color);label.setHorizontalAlignment(SwingConstants.RIGHT);return label;}
    private static UiKit.RoundPanel summaryPanel(String heading,String[] labels,JLabel[] values){UiKit.RoundPanel p=new UiKit.RoundPanel(14).fill(new Color(247,249,255)).shadow(false);p.setLayout(new BorderLayout(0,10));p.setBorder(new EmptyBorder(14,16,14,16));p.add(LivroCaixaView.title(heading,21),BorderLayout.NORTH);JPanel rows=new JPanel(new GridLayout(labels.length,2,8,0));rows.setOpaque(false);for(int i=0;i<labels.length;i++){JLabel label=new JLabel(labels[i]);label.setBorder(new EmptyBorder(8,0,8,0));values[i].setBorder(new EmptyBorder(8,0,8,0));rows.add(label);rows.add(values[i]);}p.add(rows);return p;}
    private static JPanel tablePanel(String heading,String[] columns,Object[][] rows){UiKit.RoundPanel p=new UiKit.RoundPanel(13).fill(new Color(247,249,255)).shadow(false);p.setLayout(new BorderLayout(0,6));p.setBorder(new EmptyBorder(9,9,9,9));p.add(sectionTitle(heading),BorderLayout.NORTH);DefaultTableModel model=new DefaultTableModel(rows,columns){@Override public boolean isCellEditable(int r,int c){return false;}};JTable table=UiKit.modernTable(model);table.setRowHeight(35);JScrollPane scroll=new JScrollPane(table);scroll.setBorder(new LineBorder(UiKit.LINE));p.add(scroll);return p;}
    private static void addPaymentMethod(Component parent,DefaultTableModel model){JComboBox<String> method=new JComboBox<>(new String[]{"PIX","Dinheiro","Cartão de crédito","Cartão de débito"});JTextField value=LivroCaixaView.modernField("0,00");JPanel form=new JPanel(new GridLayout(2,1,0,8));form.add(fieldBox("Forma de pagamento",method));form.add(fieldBox("Valor",value));if(JOptionPane.showConfirmDialog(parent,form,"Adicionar forma de pagamento",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION){try{BigDecimal amount=LivroCaixaView.parseMoney(value.getText());if(amount.signum()<=0)throw new IllegalArgumentException();model.addRow(new Object[]{method.getSelectedItem(),LivroCaixaView.money(amount),"Remover"});}catch(Exception e){LivroCaixaView.error(parent,"Informe um valor válido.");}}}
    private static void generateTermRows(Appointment appointment,JTextField entry,JTextField entryDate,JTextField firstDue,JTextField count,JComboBox<String> periodicity,DefaultTableModel model,JLabel informed,JLabel remaining){try{BigDecimal down=LivroCaixaView.parseMoney(entry.getText());int n=Integer.parseInt(count.getText().trim());if(down.signum()<0||down.compareTo(appointment.value)>0||n<1)throw new IllegalArgumentException();LocalDate first=LivroCaixaView.parseDate(firstDue.getText());model.setRowCount(0);if(down.signum()>0)model.addRow(new Object[]{"Entrada","Entrada",entryDate.getText(),LivroCaixaView.money(down),"Pendente"});BigDecimal rest=appointment.value.subtract(down),each=rest.divide(BigDecimal.valueOf(n),2,RoundingMode.HALF_UP);String period=String.valueOf(periodicity.getSelectedItem());for(int i=1;i<=n;i++){BigDecimal amount=i==n?rest.subtract(each.multiply(BigDecimal.valueOf(n-1))):each;LocalDate due="Quinzenal".equals(period)?first.plusDays(15L*(i-1)):"Semanal".equals(period)?first.plusWeeks(i-1):first.plusMonths(i-1);model.addRow(new Object[]{i+"/"+n,period,LivroCaixaView.fmt(due),LivroCaixaView.money(amount),"Pendente"});}recalcTermSummary(appointment.value,model,informed,remaining);}catch(Exception e){model.setRowCount(0);informed.setText("Revise os dados");remaining.setText("—");remaining.setForeground(UiKit.RED);}}
    private static void recalcTermSummary(BigDecimal total,DefaultTableModel model,JLabel informed,JLabel remaining){BigDecimal sum=BigDecimal.ZERO;for(int i=0;i<model.getRowCount();i++)try{sum=sum.add(LivroCaixaView.parseMoney(String.valueOf(model.getValueAt(i,3))));}catch(Exception ignored){}informed.setText(LivroCaixaView.money(sum));BigDecimal rest=total.subtract(sum);remaining.setText(LivroCaixaView.money(rest));remaining.setForeground(rest.signum()==0?new Color(8,130,31):UiKit.RED);}
    private static void saveCashInvoice(LivroCaixaView.MainFrame frame,Appointment appointment,DefaultTableModel model){if(model.getRowCount()==0)throw new IllegalArgumentException("Adicione ao menos uma forma de pagamento.");BigDecimal total=BigDecimal.ZERO;List<Payment> payments=new ArrayList<>();for(int i=0;i<model.getRowCount();i++){BigDecimal amount=LivroCaixaView.parseMoney(String.valueOf(model.getValueAt(i,1)));total=total.add(amount);payments.add(new Payment(frame.store.nextPayment++,appointment.id,appointment.client,"À vista",appointment.date,amount,String.valueOf(model.getValueAt(i,0)),BigDecimal.ZERO,false));}if(total.compareTo(appointment.value)>0)throw new IllegalArgumentException("O total informado excede o valor da consulta.");frame.store.payments.addAll(payments);BigDecimal remaining=appointment.value.subtract(total);if(remaining.signum()>0)frame.store.installments.add(new Installment(frame.store.nextInstallment++,appointment.id,appointment.client,"Saldo",appointment.date,remaining,BigDecimal.ZERO,false));}
    private static void saveTermInvoice(LivroCaixaView.MainFrame frame,Appointment appointment,JTextField entry,JTextField entryDate,DefaultTableModel model){if(model.getRowCount()==0)throw new IllegalArgumentException("Gere as parcelas antes de salvar.");BigDecimal total=BigDecimal.ZERO;for(int i=0;i<model.getRowCount();i++)total=total.add(LivroCaixaView.parseMoney(String.valueOf(model.getValueAt(i,3))));if(total.compareTo(appointment.value)!=0)throw new IllegalArgumentException("O total das parcelas deve ser igual ao valor da consulta.");for(int i=0;i<model.getRowCount();i++){String label=String.valueOf(model.getValueAt(i,0));BigDecimal amount=LivroCaixaView.parseMoney(String.valueOf(model.getValueAt(i,3)));LocalDate date=LivroCaixaView.parseDate(String.valueOf(model.getValueAt(i,2)));if("Entrada".equals(label))frame.store.payments.add(new Payment(frame.store.nextPayment++,appointment.id,appointment.client,"Entrada",LivroCaixaView.parseDate(entryDate.getText()),amount,"PIX",BigDecimal.ZERO,false));else frame.store.installments.add(new Installment(frame.store.nextInstallment++,appointment.id,appointment.client,label,date,amount,BigDecimal.ZERO,false));}}
    private static void attach(JTextField field,Runnable runnable){field.getDocument().addDocumentListener(new DocumentListener(){public void insertUpdate(DocumentEvent e){runnable.run();}public void removeUpdate(DocumentEvent e){runnable.run();}public void changedUpdate(DocumentEvent e){runnable.run();}});}

    private static final class SegmentToggle extends JToggleButton {
        SegmentToggle(String text){super(text);setFont(new Font("Segoe UI",Font.BOLD,16));setFocusPainted(false);setBorderPainted(false);setContentAreaFilled(false);setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));setBorder(new EmptyBorder(8,16,8,16));addActionListener(e->repaint());}
        @Override protected void paintComponent(Graphics graphics){Graphics2D g=(Graphics2D)graphics.create();UiKit.antialias(g);g.setColor(isSelected()?UiKit.BLUE:new Color(235,241,251));g.fillRoundRect(0,0,getWidth(),getHeight(),12,12);g.dispose();setForeground(isSelected()?Color.WHITE:UiKit.INK);super.paintComponent(graphics);}
    }
    private static final class RemoveRenderer extends DefaultTableCellRenderer {
        RemoveRenderer(){setHorizontalAlignment(CENTER);setFont(new Font("Segoe UI",Font.BOLD,13));}
        @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){super.getTableCellRendererComponent(t,v,s,f,r,c);setForeground(UiKit.RED);setText("Remover");return this;}
    }
}
