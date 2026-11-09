package livrocaixa.view;

import livrocaixa.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.Arrays;

/**
 * Biblioteca visual compartilhada pelas telas do protótipo.
 * Centraliza cores, fontes, cartões, botões, tabelas, gráficos e ícones vetoriais
 * para manter o mesmo padrão visual sem bibliotecas externas.
 */
final class UiKit {
    static final Color NAVY = new Color(5, 48, 80);
    static final Color NAVY_2 = new Color(10, 61, 94);
    static final Color BLUE = new Color(0, 91, 234);
    static final Color BLUE_2 = new Color(0, 66, 206);
    static final Color CYAN = new Color(0, 190, 184);
    static final Color RED = new Color(235, 35, 43);
    static final Color ORANGE = new Color(255, 145, 0);
    static final Color GREEN = new Color(21, 164, 79);
    static final Color INK = new Color(8, 15, 42);
    static final Color MUTED = new Color(83, 99, 125);
    static final Color BG = new Color(246, 249, 255);
    static final Color LINE = new Color(210, 222, 239);
    static final Font REGULAR = new Font("Segoe UI", Font.PLAIN, 17);
    static final Font SEMIBOLD = new Font("Segoe UI", Font.BOLD, 17);

    private UiKit() {}

    static void installDefaults() {
        // Valores globais usados pelos componentes Swing que não recebem estilo próprio.
        UIManager.put("Panel.background", BG);
        UIManager.put("Label.font", REGULAR);
        UIManager.put("Button.font", SEMIBOLD);
        UIManager.put("TextField.font", REGULAR);
        UIManager.put("PasswordField.font", REGULAR);
        UIManager.put("ComboBox.font", REGULAR);
        UIManager.put("Table.font", REGULAR);
        UIManager.put("TableHeader.font", SEMIBOLD);
        UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
        UIManager.put("OptionPane.messageFont", REGULAR);
        UIManager.put("OptionPane.buttonFont", SEMIBOLD);
    }

    static void antialias(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    /** Painel de fundo usado no menu lateral, cabeçalho e área de marca. */
    static final class GradientPanel extends JPanel {
        private final Color from, to;
        private final boolean horizontal;
        GradientPanel(Color from, Color to, boolean horizontal) {
            this.from = from; this.to = to; this.horizontal = horizontal; setOpaque(false);
        }
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create(); antialias(g);
            Paint paint = horizontal
                    ? new GradientPaint(0, 0, from, getWidth(), 0, to)
                    : new GradientPaint(0, 0, from, 0, getHeight(), to);
            g.setPaint(paint); g.fillRect(0, 0, getWidth(), getHeight()); g.dispose();
            super.paintComponent(graphics);
        }
    }

    /** Cartão reutilizável com cantos arredondados, borda e sombra opcionais. */
    static class RoundPanel extends JPanel {
        private int radius = 18;
        private Color fill = Color.WHITE;
        private Color line = LINE;
        private boolean shadow = true;
        RoundPanel() { setOpaque(false); }
        RoundPanel(int radius) { this(); this.radius = radius; }
        RoundPanel fill(Color value) { fill = value; return this; }
        RoundPanel line(Color value) { line = value; return this; }
        RoundPanel shadow(boolean value) { shadow = value; return this; }
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create(); antialias(g);
            int inset = shadow ? 5 : 1;
            if (shadow) {
                g.setColor(new Color(40, 75, 120, 20));
                g.fillRoundRect(3, 4, getWidth() - 7, getHeight() - 7, radius, radius);
            }
            g.setColor(fill); g.fillRoundRect(1, 1, getWidth() - inset, getHeight() - inset, radius, radius);
            if (line != null) { g.setColor(line); g.drawRoundRect(1, 1, getWidth() - inset - 1, getHeight() - inset - 1, radius, radius); }
            g.dispose(); super.paintComponent(graphics);
        }
    }

    /** Botão arredondado com estados visual de repouso, foco do mouse e clique. */
    static class RoundButton extends JButton {
        private final Color normal, hover, pressed;
        private boolean over;
        RoundButton(String text, Icon icon, Color normal, Color foreground) {
            super(text, icon); this.normal = normal; this.hover = brighten(normal, .08f); this.pressed = darken(normal, .08f);
            setForeground(foreground); setFont(new Font("Segoe UI", Font.BOLD, 17)); setOpaque(false);
            setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setIconTextGap(12); setBorder(new EmptyBorder(12, 18, 12, 18));
            addMouseListener(new MouseAdapter() { public void mouseEntered(MouseEvent e) { over = true; repaint(); } public void mouseExited(MouseEvent e) { over = false; repaint(); }});
        }
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create(); antialias(g);
            Color c = getModel().isPressed() ? pressed : over ? hover : normal;
            g.setColor(c); g.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14); g.dispose(); super.paintComponent(graphics);
        }
    }

    static JButton primary(String text, String icon) { return new RoundButton(text, vectorIcon(icon, 24, Color.WHITE), BLUE, Color.WHITE); }
    static JButton secondary(String text, String icon) { return new RoundButton(text, vectorIcon(icon, 22, INK), new Color(235, 241, 250), INK); }
    static JButton danger(String text) { return new RoundButton(text, vectorIcon("trash", 20, Color.WHITE), RED, Color.WHITE); }

    /** Cria tabelas com cabeçalho, seleção, ordenação e linhas alternadas padronizadas. */
    static JTable modernTable(TableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(38); table.setShowVerticalLines(true); table.setShowHorizontalLines(true);
        table.setGridColor(LINE); table.setSelectionBackground(new Color(224, 237, 255)); table.setSelectionForeground(INK);
        table.setForeground(INK); table.setBackground(Color.WHITE); table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(1, 1)); table.setAutoCreateRowSorter(true);
        JTableHeader h = table.getTableHeader(); h.setPreferredSize(new Dimension(0, 42)); h.setBackground(new Color(228, 237, 250)); h.setForeground(INK); h.setReorderingAllowed(false);
        h.setDefaultRenderer(new HeaderRenderer());
        table.setDefaultRenderer(Object.class, new CellRenderer());
        return table;
    }

    private static final class HeaderRenderer extends DefaultTableCellRenderer {
        HeaderRenderer() { setHorizontalAlignment(CENTER); setBorder(BorderFactory.createMatteBorder(0,0,1,1,LINE)); setFont(SEMIBOLD); }
        @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){super.getTableCellRendererComponent(t,v,s,f,r,c);setBackground(new Color(228,237,250));setForeground(INK);return this;}
    }
    private static final class CellRenderer extends DefaultTableCellRenderer {
        private boolean pill;
        private Color pillColor;
        CellRenderer(){setBorder(new EmptyBorder(0,10,0,10));}
        @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){
            super.getTableCellRendererComponent(t,v,s,f,r,c);String text=String.valueOf(v);pill=false;setHorizontalAlignment(LEFT);setFont(REGULAR);setOpaque(true);
            if(!s)setBackground((r&1)==0?Color.WHITE:new Color(249,251,255));setForeground(INK);
            if(text.startsWith("R$")){setForeground(BLUE_2);setFont(SEMIBOLD);setHorizontalAlignment(RIGHT);}
            String status=text.toLowerCase();
            if(status.matches("pendente|vencida|vencido|paga|pago|confirmada|estornada|cancelada|aberta|faturada")){
                pill=true;setOpaque(false);setHorizontalAlignment(CENTER);setFont(SEMIBOLD);
                if(status.startsWith("pendente")||status.startsWith("aberta")){pillColor=new Color(255,231,163);setForeground(new Color(100,70,0));}
                else if(status.startsWith("venc")||status.startsWith("cancel")||status.startsWith("estorn")){pillColor=new Color(255,210,210);setForeground(new Color(200,20,28));}
                else {pillColor=new Color(217,245,215);setForeground(new Color(4,120,28));}
            }
            return this;
        }
        @Override protected void paintComponent(Graphics graphics){
            if(pill){Graphics2D g=(Graphics2D)graphics.create();antialias(g);g.setColor(pillColor);g.fillRoundRect(6,5,getWidth()-12,getHeight()-10,16,16);g.dispose();}
            super.paintComponent(graphics);
        }
    }

    /** Gráfico desenhado diretamente com Java2D para evitar dependências de gráficos. */
    static final class CashFlowChart extends JComponent {
        private String[] labels = {"Jun", "Jul", "Ago", "Set", "Out", "Nov"};
        private double[] income = {8000, 9000, 10000, 11400, 12400, 12800};
        private double[] expense = {3400, 4100, 4300, 3800, 4300, 4400};
        CashFlowChart() { setOpaque(false); setPreferredSize(new Dimension(520, 285)); }
        CashFlowChart values(String[] labels, double[] income, double[] expense) {
            this.labels = labels.clone(); this.income = income.clone(); this.expense = expense.clone(); repaint(); return this;
        }
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create(); antialias(g);
            int left=64, right=20, top=45, bottom=42, w=getWidth()-left-right, h=getHeight()-top-bottom;
            double max = Math.max(1, Arrays.stream(income).max().orElse(1)); max = Math.max(max, Arrays.stream(expense).max().orElse(1));
            max = Math.ceil(max/2000d)*2000d;
            g.setFont(new Font("Segoe UI",Font.PLAIN,12));
            for(int i=0;i<=7;i++){int y=top+h-(h*i/7);g.setColor(new Color(220,228,240));g.drawLine(left,y,left+w,y);g.setColor(MUTED);String s=String.format("%,.0f",max*i/7).replace(',','.');g.drawString(s,4,y+4);}
            int groups=Math.max(1,labels.length), slot=w/groups, bar=Math.max(12,Math.min(28,slot/3));
            for(int i=0;i<groups;i++){int center=left+i*slot+slot/2;int hi=(int)(income[i]/max*h),he=(int)(expense[i]/max*h);paintBar(g,center-bar-2,top+h-hi,bar,hi,BLUE,BLUE_2);paintBar(g,center+2,top+h-he,bar,he,CYAN,new Color(0,154,158));g.setColor(INK);FontMetrics fm=g.getFontMetrics();g.drawString(labels[i],center-fm.stringWidth(labels[i])/2,top+h+23);}
            legend(g, getWidth()-190, 14, BLUE, "Entradas"); legend(g,getWidth()-100,14,CYAN,"Saídas"); g.dispose();
        }
        private void paintBar(Graphics2D g,int x,int y,int w,int h,Color a,Color b){g.setPaint(new GradientPaint(x,y,a,x+w,y,b));g.fillRoundRect(x,y,w,h,5,5);}
        private void legend(Graphics2D g,int x,int y,Color c,String s){g.setColor(c);g.fillRoundRect(x,y,15,15,4,4);g.setColor(INK);g.drawString(s,x+21,y+13);}
    }

    /** Retorna um ícone escalável desenhado por Java2D a partir de um nome semântico. */
    static Icon vectorIcon(String name, int size, Color color) { return new VectorIcon(name, size, color); }
    private static final class VectorIcon implements Icon {
        final String name; final int size; final Color color;
        VectorIcon(String name,int size,Color color){this.name=name;this.size=size;this.color=color;}
        public int getIconWidth(){return size;} public int getIconHeight(){return size;}
        public void paintIcon(Component c,Graphics graphics,int x,int y){Graphics2D g=(Graphics2D)graphics.create();antialias(g);g.translate(x,y);g.setColor(color);float k=size/24f;g.scale(k,k);g.setStroke(new BasicStroke(1.8f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            switch(name){
                case "home" -> {g.draw(new Path2D.Double(){ {moveTo(2,11);lineTo(12,2);lineTo(22,11);moveTo(5,9);lineTo(5,21);lineTo(10,21);lineTo(10,15);lineTo(14,15);lineTo(14,21);lineTo(19,21);lineTo(19,9);} });}
                case "user" -> {g.drawOval(8,2,8,8);g.fillRoundRect(4,12,16,10,8,8);}
                case "group" -> {g.drawOval(8,2,8,8);g.fillRoundRect(5,12,14,10,7,7);g.drawOval(1,6,6,6);g.drawOval(17,6,6,6);g.fillRoundRect(0,14,7,7,5,5);g.fillRoundRect(17,14,7,7,5,5);}
                case "shield" -> {g.draw(new Path2D.Double(){ {moveTo(12,2);lineTo(21,6);lineTo(20,14);curveTo(19,19,15,21,12,23);curveTo(9,21,5,19,4,14);lineTo(3,6);closePath();} });g.drawOval(9,7,6,6);g.drawArc(7,12,10,7,0,180);}
                case "calendar" -> {g.drawRoundRect(3,5,18,16,3,3);g.drawLine(3,10,21,10);g.drawLine(8,2,8,7);g.drawLine(16,2,16,7);}
                case "coins" -> {g.drawOval(3,3,14,6);g.drawOval(3,8,14,6);g.drawOval(3,13,14,6);g.drawOval(8,16,13,6);}
                case "card" -> {g.drawRoundRect(2,5,20,15,3,3);g.drawLine(2,10,22,10);g.drawRoundRect(5,14,7,2,1,1);}
                case "swap" -> {g.drawLine(3,7,20,7);g.drawLine(16,3,20,7);g.drawLine(16,11,20,7);g.drawLine(21,17,4,17);g.drawLine(8,13,4,17);g.drawLine(8,21,4,17);}
                case "chart" -> {g.fillRoundRect(3,12,4,9,2,2);g.fillRoundRect(10,7,4,14,2,2);g.fillRoundRect(17,2,4,19,2,2);}
                case "gear" -> {g.drawOval(7,7,10,10);g.drawOval(10,10,4,4);for(int i=0;i<8;i++){double a=i*Math.PI/4;g.drawLine((int)(12+7*Math.cos(a)),(int)(12+7*Math.sin(a)),(int)(12+10*Math.cos(a)),(int)(12+10*Math.sin(a)));}}
                case "wallet" -> {g.drawRoundRect(2,6,20,14,4,4);g.drawRoundRect(14,10,9,6,2,2);g.fillOval(17,12,2,2);g.drawLine(5,6,17,3);}
                case "plus" -> {g.drawLine(12,4,12,20);g.drawLine(4,12,20,12);}
                case "logout" -> {g.drawRoundRect(2,4,12,16,2,2);g.drawLine(10,12,22,12);g.drawLine(18,8,22,12);g.drawLine(18,16,22,12);}
                case "trash" -> {g.drawRoundRect(6,7,12,14,2,2);g.drawLine(4,7,20,7);g.drawLine(9,3,15,3);g.drawLine(10,10,10,18);g.drawLine(14,10,14,18);}
                case "edit" -> {g.draw(new Line2D.Double(4,20,8,19));g.draw(new Line2D.Double(6,17,17,6));g.draw(new Line2D.Double(9,20,20,9));g.draw(new Line2D.Double(17,6,20,9));}
                case "save" -> {g.drawRoundRect(3,3,18,18,2,2);g.drawRect(7,3,10,6);g.drawRect(7,14,10,7);}
                case "search" -> {g.drawOval(4,4,11,11);g.drawLine(14,14,21,21);}
                case "info" -> {g.drawOval(2,2,20,20);g.drawLine(12,10,12,18);g.fillOval(11,6,2,2);}
                case "warning" -> {Path2D p=new Path2D.Double();p.moveTo(12,2);p.lineTo(22,21);p.lineTo(2,21);p.closePath();g.draw(p);g.drawLine(12,8,12,14);g.fillOval(11,17,2,2);}
                case "check" -> {g.drawOval(2,2,20,20);g.drawLine(6,12,10,16);g.drawLine(10,16,18,7);}
                case "filter" -> {Path2D p=new Path2D.Double();p.moveTo(3,4);p.lineTo(21,4);p.lineTo(14,12);p.lineTo(14,20);p.lineTo(10,18);p.lineTo(10,12);p.closePath();g.draw(p);}
                case "refresh" -> {g.drawArc(3,3,18,18,35,290);g.drawLine(18,3,21,7);g.drawLine(18,3,14,4);}
                case "document" -> {g.drawRoundRect(5,2,14,20,2,2);g.drawLine(9,8,16,8);g.drawLine(9,12,16,12);g.drawLine(9,16,15,16);}
                case "close" -> {g.drawLine(5,5,19,19);g.drawLine(19,5,5,19);}
                case "chevronDown" -> {g.drawLine(5,8,12,15);g.drawLine(12,15,19,8);}
                case "arrowUp" -> {g.drawLine(12,21,12,3);g.drawLine(5,10,12,3);g.drawLine(19,10,12,3);}
                case "arrowDown" -> {g.drawLine(12,3,12,21);g.drawLine(5,14,12,21);g.drawLine(19,14,12,21);}
                default -> {g.drawRoundRect(3,3,18,18,4,4);g.drawLine(8,12,16,12);g.drawLine(12,8,12,16);}
            }
            g.dispose();}
    }

    private static Color brighten(Color c,float v){return mix(c,Color.WHITE,v);}
    private static Color darken(Color c,float v){return mix(c,Color.BLACK,v);}
    private static Color mix(Color a,Color b,float v){return new Color((int)(a.getRed()*(1-v)+b.getRed()*v),(int)(a.getGreen()*(1-v)+b.getGreen()*v),(int)(a.getBlue()*(1-v)+b.getBlue()*v),a.getAlpha());}
}
