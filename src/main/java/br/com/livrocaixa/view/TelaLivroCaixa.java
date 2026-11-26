package br.com.livrocaixa.view;

import br.com.livrocaixa.view.dados.*;
import br.com.livrocaixa.exception.AcessoNegadoException;
import br.com.livrocaixa.exception.PersistenciaException;
import br.com.livrocaixa.exception.ValidacaoException;
import br.com.livrocaixa.model.Funcionario;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Consumer;

/**
 * Camada View do protótipo visual Livro Caixa.
 *
 * <p>O projeto demonstra a navegação e o comportamento das telas em Java Swing.
 * Os dados usados na interface são amostras mantidas apenas em memória; não há
 * banco de dados nem persistência de cadastro nesta versão.</p>
 */
public final class TelaLivroCaixa {
    static final Color NAVY = new Color(5, 46, 78), BLUE = new Color(0, 91, 234), PALE = new Color(244, 248, 255);
    static final Color GREEN = new Color(18, 150, 73), RED = new Color(220, 38, 38), ORANGE = new Color(245, 145, 0);
    static final Font NORMAL = new Font("Segoe UI", Font.PLAIN, 17), BOLD = new Font("Segoe UI", Font.BOLD, 17);
    static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    static final Locale BRAZILIAN_PORTUGUESE = new Locale.Builder()
        .setLanguage("pt")
        .setRegion("BR")
        .build();
    static final NumberFormat BRL = NumberFormat.getCurrencyInstance(BRAZILIAN_PORTUGUESE);

    /**
     * Configura a interface e abre a tela de acesso.
     * O Controller entrega o cenário demonstrativo que será exibido pela View.
     */
    public static void start(CoordenadorTelas controller) {
        Objects.requireNonNull(controller, "controller");
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        ComponentesVisuais.installDefaults();
        UIManager.put("Label.font", NORMAL); UIManager.put("Button.font", BOLD); UIManager.put("TextField.font", NORMAL);
        UIManager.put("PasswordField.font", NORMAL); UIManager.put("ComboBox.font", NORMAL); UIManager.put("Table.font", NORMAL); UIManager.put("TableHeader.font", BOLD);
        UIManager.put("TabbedPane.font", BOLD); UIManager.put("OptionPane.messageFont", NORMAL); UIManager.put("OptionPane.buttonFont", BOLD);
        new JanelaLogin(controller).setVisible(true);
    }

    /** Janela inicial demonstrativa. A autenticação apenas valida campos preenchidos. */
    static final class JanelaLogin extends JFrame {
        private final CoordenadorTelas controller;

        JanelaLogin(CoordenadorTelas controller) {
            super("Livro Caixa");
            setIconImage(appIcon());
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setSize(1200, 675);
            setMinimumSize(new Dimension(980,580));
            setLocationRelativeTo(null);
            setResizable(true);
            this.controller = Objects.requireNonNull(controller, "controller");
            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(new Color(246,249,255));

            ComponentesVisuais.GradientPanel brand = new ComponentesVisuais.GradientPanel(
                    new Color(3,39,68), new Color(9,75,115), false);
            brand.setPreferredSize(new Dimension(340, 0));
            brand.setLayout(new GridBagLayout());
            JPanel brandContent = new JPanel();
            brandContent.setOpaque(false);
            brandContent.setLayout(new BoxLayout(brandContent,BoxLayout.Y_AXIS));
            JLabel mark = new JLabel(ComponentesVisuais.vectorIcon("wallet",66,Color.WHITE));
            mark.setAlignmentX(.5f);
            brandContent.add(mark);
            brandContent.add(Box.createVerticalStrut(20));
            JLabel logo = new JLabel("Livro Caixa");
            logo.setForeground(Color.WHITE);
            logo.setFont(new Font("Segoe UI", Font.BOLD, 32));
            logo.setAlignmentX(.5f);
            brandContent.add(logo);
            JLabel brandSub = new JLabel("CONTROLE FINANCEIRO");
            brandSub.setForeground(new Color(190,220,240));
            brandSub.setFont(new Font("Segoe UI",Font.PLAIN,12));
            brandSub.setAlignmentX(.5f);
            brandContent.add(Box.createVerticalStrut(9));
            brandContent.add(brandSub);
            brand.add(brandContent);

            JPanel form = new JPanel(new GridBagLayout());
            form.setOpaque(false);
            ComponentesVisuais.RoundPanel card = new ComponentesVisuais.RoundPanel(18).fill(Color.WHITE);
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(new EmptyBorder(32,40,26,40));
            card.setPreferredSize(new Dimension(600, 500));
            JLabel title = title("Acesso ao sistema", 32);
            title.setForeground(new Color(13,30,58));
            title.setAlignmentX(.5f);
            card.add(title);
            JLabel sub = new JLabel("Informe seus dados para continuar");
            sub.setAlignmentX(.5f);
            sub.setForeground(new Color(100,112,132));
            card.add(Box.createVerticalStrut(4));
            card.add(sub);
            card.add(Box.createVerticalStrut(26));

            JTextField user = modernField("Ana Souza");
            JPasswordField pass = modernPassword("123456");
            JPanel userGroup = labeled("Nome do funcionário",user);
            JPanel passGroup = labeled("Senha",pass);
            Dimension loginFieldSize = new Dimension(520,82);
            userGroup.setPreferredSize(loginFieldSize);
            userGroup.setMinimumSize(loginFieldSize);
            userGroup.setMaximumSize(loginFieldSize);
            userGroup.setAlignmentX(.5f);
            passGroup.setPreferredSize(loginFieldSize);
            passGroup.setMinimumSize(loginFieldSize);
            passGroup.setMaximumSize(loginFieldSize);
            passGroup.setAlignmentX(.5f);
            card.add(userGroup);
            card.add(Box.createVerticalStrut(8));
            card.add(passGroup);

            JCheckBox show = new JCheckBox("Mostrar senha");
            show.setOpaque(false);
            show.setForeground(new Color(70,82,105));
            show.addActionListener(e -> pass.setEchoChar(show.isSelected() ? (char)0 : '•'));
            JPanel checkRow = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
            checkRow.setOpaque(false);
            Dimension checkRowSize = new Dimension(520,30);
            checkRow.setPreferredSize(checkRowSize);
            checkRow.setMinimumSize(checkRowSize);
            checkRow.setMaximumSize(checkRowSize);
            checkRow.setAlignmentX(.5f);
            checkRow.add(show);
            card.add(checkRow);

            JButton enter = new ComponentesVisuais.RoundButton(
                    "Entrar",ComponentesVisuais.vectorIcon("login",22,Color.WHITE),BLUE,Color.WHITE);
            enter.setAlignmentX(.5f);
            Dimension loginButtonSize = new Dimension(520,54);
            enter.setMaximumSize(loginButtonSize);
            enter.setPreferredSize(loginButtonSize);
            enter.addActionListener(e -> {
                if (user.getText().isBlank() || pass.getPassword().length == 0) {
                    error(this,"Preencha usuário e senha.");
                } else {
                    try {
                        Funcionario funcionario = controller.autenticar(
                                user.getText().trim(), new String(pass.getPassword()));
                        dispose();
                        new JanelaPrincipal(controller, funcionario.getNome()).setVisible(true);
                    } catch (ValidacaoException exception) {
                        error(this, exception.getMessage());
                    } catch (AcessoNegadoException exception) {
                        error(this, "Você não possui permissão para acessar o sistema.");
                    } catch (PersistenciaException exception) {
                        error(this, "Não foi possível acessar o banco de dados. Verifique a conexão com o servidor.");
                    }
                }
            });
            card.add(Box.createVerticalStrut(16));
            card.add(enter);
            card.add(Box.createVerticalStrut(18));
            JSeparator line = new JSeparator();
            line.setMaximumSize(new Dimension(Integer.MAX_VALUE,1));
            card.add(line);
            card.add(Box.createVerticalStrut(10));
            JLabel foot = new JLabel("Controle financeiro");
            foot.setForeground(new Color(135,145,160));
            foot.setFont(new Font("Segoe UI",Font.PLAIN,12));
            foot.setAlignmentX(.5f);
            card.add(foot);

            form.add(card);
            root.add(brand, BorderLayout.WEST);
            root.add(form);
            setContentPane(root);
            getRootPane().setDefaultButton(enter);
        }
    }

    /**
     * Moldura principal compartilhada por todas as telas.
     * Mantém o menu lateral e o cabeçalho fixos; somente {@code content} é trocado
     * quando o usuário navega pelo sistema.
     */
    static final class JanelaPrincipal extends JFrame {
        final CoordenadorTelas controller;
        final LivroCaixaModel store;
        final String employee;
        final boolean administrator;
        final JPanel content = new JPanel(new BorderLayout());
        final JLabel pageTitle = title("Painel", 30);
        final Map<String,JButton> nav = new LinkedHashMap<>();

        JanelaPrincipal(CoordenadorTelas controller, String employee) {
            super("Livro Caixa");
            setIconImage(appIcon());
            this.controller = Objects.requireNonNull(controller, "controller");
            this.store = controller.getModel();
            this.employee = employee;
            this.administrator = controller.getContext().autenticacao.getUsuarioLogado().isAdministrador();
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setMinimumSize(new Dimension(1180,700));
            setSize(1500,850);
            setLocationRelativeTo(null);
            setExtendedState(JFrame.MAXIMIZED_BOTH);

            JPanel shell = new JPanel(new BorderLayout());
            ComponentesVisuais.GradientPanel side = new ComponentesVisuais.GradientPanel(
                    new Color(4,38,67),new Color(9,71,108),false);
            side.setPreferredSize(new Dimension(280,0));
            side.setLayout(new BoxLayout(side,BoxLayout.Y_AXIS));
            JLabel logo = new JLabel("Livro Caixa");
            logo.setIcon(ComponentesVisuais.vectorIcon("wallet",38,Color.WHITE));
            logo.setIconTextGap(13);
            logo.setForeground(Color.WHITE);
            logo.setFont(new Font("Segoe UI",Font.BOLD,31));
            logo.setBorder(new EmptyBorder(26,24,36,10));
            logo.setAlignmentX(LEFT_ALIGNMENT);
            side.add(logo);
            if (administrator) {
                addNav(side,"Painel","home");
                addNav(side,"Clientes","user");
                addNav(side,"Consultas","calendar");
                addNav(side,"Contas a receber","coins");
                addNav(side,"Saídas","swap");
                addNav(side,"Relatórios","chart");
                addNav(side,"Configurações","gear");
            } else {
                addNav(side,"Saídas","swap");
                addNav(side,"Tipos de saída","gear");
            }

            JPanel right = new JPanel(new BorderLayout());
            ComponentesVisuais.GradientPanel top = new ComponentesVisuais.GradientPanel(
                    new Color(8,104,215),new Color(0,74,215),true);
            top.setPreferredSize(new Dimension(0,84));
            top.setLayout(new BorderLayout());
            top.setBorder(new EmptyBorder(15,24,15,30));
            pageTitle.setForeground(Color.WHITE);
            pageTitle.setFont(new Font("Segoe UI",Font.BOLD,32));
            top.add(pageTitle,BorderLayout.WEST);
            top.add(createAccountButton(),BorderLayout.EAST);

            content.setBackground(PALE);
            right.add(top,BorderLayout.NORTH);
            right.add(content,BorderLayout.CENTER);
            shell.add(side,BorderLayout.WEST);
            shell.add(right);
            setContentPane(shell);
            showPage(administrator ? "Painel" : "Saídas");
        }

        @Override
        protected void processWindowEvent(WindowEvent event) {
            if (event.getID() == WindowEvent.WINDOW_CLOSING) {
                store.save();
            }
            super.processWindowEvent(event);
        }

        /** Cria o menu da conta exibido no canto direito do cabeçalho. */
        private JButton createAccountButton() {
            Icon chevron = ComponentesVisuais.vectorIcon("chevronDown",18,Color.WHITE);
            JButton button = new JButton(
                    "Funcionário: " + employee,
                    ComponentesVisuais.vectorIcon("user",28,Color.WHITE)) {
                @Override protected void paintComponent(Graphics graphics) {
                    if (getModel().isRollover() || getModel().isPressed()) {
                        Graphics2D g = (Graphics2D) graphics.create();
                        ComponentesVisuais.antialias(g);
                        int alpha = getModel().isPressed() ? 42 : 26;
                        g.setColor(new Color(255,255,255,alpha));
                        g.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                        g.dispose();
                    }
                    super.paintComponent(graphics);
                    chevron.paintIcon(
                            this,
                            graphics,
                            getWidth()-chevron.getIconWidth()-10,
                            (getHeight()-chevron.getIconHeight())/2);
                }
            };
            button.setName("accountMenuButton");
            button.setFont(new Font("Segoe UI",Font.PLAIN,17));
            button.setForeground(Color.WHITE);
            button.setIconTextGap(12);
            button.setBorder(new EmptyBorder(10,12,10,38));
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.setFocusPainted(false);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.setToolTipText("Abrir opções da conta");

            JPopupMenu menu = createAccountMenu();
            button.addActionListener(e -> {
                menu.setPopupSize(button.getWidth(),62);
                menu.show(button,0,button.getHeight()+5);
            });
            return button;
        }

        /** Mantém as ações da conta reunidas em um menu curto e acessível. */
        private JPopupMenu createAccountMenu() {
            JPopupMenu menu = new JPopupMenu();
            menu.setLayout(new BorderLayout());
            menu.setBackground(Color.WHITE);
            menu.setBorder(new CompoundBorder(
                    new LineBorder(new Color(205,216,235)),
                    new EmptyBorder(7,7,7,7)));

            JButton logout = new ComponentesVisuais.RoundButton(
                    "Sair da conta",
                    ComponentesVisuais.vectorIcon("logout",21,ComponentesVisuais.INK),
                    Color.WHITE,
                    ComponentesVisuais.INK);
            logout.setName("logoutMenuItem");
            logout.setFont(new Font("Segoe UI",Font.BOLD,15));
            logout.setHorizontalAlignment(SwingConstants.LEFT);
            logout.setIconTextGap(10);
            logout.setBorder(new EmptyBorder(10,14,10,14));
            logout.setPreferredSize(new Dimension(240,46));
            logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            logout.addActionListener(e -> {
                menu.setVisible(false);
                requestLogout();
            });
            menu.add(logout,BorderLayout.CENTER);
            return menu;
        }

        private void requestLogout() {
            if (confirm(this,"Sair da conta de " + employee + "?")) {
                try {
                    controller.encerrarSessao();
                } catch (RuntimeException exception) {
                    error(this, exception.getMessage());
                    return;
                }
                returnToLogin();
            }
        }

        /** Salva o estado atual e retorna ao acesso usando a mesma sessão de dados. */
        private void returnToLogin() {
            store.save();
            JanelaLogin login = new JanelaLogin(controller);
            login.setLocationRelativeTo(this);
            dispose();
            login.setVisible(true);
        }

        /** Registra uma opção no menu e direciona o clique para {@link #showPage(String)}. */
        void addNav(JPanel side,String name,String icon) {
            JButton button = new NavButton(name,ComponentesVisuais.vectorIcon(icon,28,Color.WHITE));
            button.setMaximumSize(new Dimension(258,58));
            button.setPreferredSize(new Dimension(258,58));
            button.setAlignmentX(LEFT_ALIGNMENT);
            button.addActionListener(e -> showPage(name));
            side.add(button);
            side.add(Box.createVerticalStrut(4));
            nav.put(name,button);
        }

        /** Seleciona uma tela principal e destaca a opção correspondente no menu lateral. */
        void showPage(String name) {
            pageTitle.setText(name);
            content.removeAll();
            nav.forEach((navName,button) -> {
                button.setBackground(navName.equals(name) ? new Color(0,94,245) : new Color(0,0,0,0));
                button.repaint();
            });
            JComponent page = switch(name) {
                case "Clientes" -> clients();
                case "Consultas" -> appointments();
                case "Contas a receber" -> receivables();
                case "Saídas" -> expenses();
                case "Relatórios" -> reports();
                case "Configurações" -> settings();
                case "Tipos de saída" -> TelasGerenciamento.auxiliary(this, 1);
                default -> dashboard();
            };
            ComponentesVisuais.styleChoices(page);
            content.add(page);
            content.revalidate();
            content.repaint();
        }

        /** Exibe formulários e etapas internas sem recriar a janela ou o menu principal. */
        void showCustom(String titleText,JComponent panel) {
            pageTitle.setText(titleText);
            content.removeAll();
            nav.forEach((name,button) -> {
                button.setBackground(new Color(0,0,0,0));
                button.repaint();
            });
            ComponentesVisuais.styleChoices(panel);
            content.add(panel);
            content.revalidate();
            content.repaint();
        }
        void showSuccessPage(String message,String backPage){
            JPanel root=page();
            root.setLayout(new GridBagLayout());
            ComponentesVisuais.RoundPanel box=new ComponentesVisuais.RoundPanel(20).fill(Color.WHITE);
            box.setPreferredSize(new Dimension(520,330));
            box.setLayout(new BoxLayout(box,BoxLayout.Y_AXIS));
            box.setBorder(new EmptyBorder(30,45,30,45));

            Color success=new Color(8,139,38);
            ComponentesVisuais.RoundPanel iconBox=new ComponentesVisuais.RoundPanel(22).fill(new Color(218,247,224)).line(null).shadow(false);
            iconBox.setLayout(new GridBagLayout());
            iconBox.setAlignmentX(.5f);
            iconBox.setPreferredSize(new Dimension(86,86));
            iconBox.setMaximumSize(new Dimension(86,86));
            iconBox.add(new JLabel(ComponentesVisuais.vectorIcon("coins",46,success)));
            box.add(iconBox);
            box.add(Box.createVerticalStrut(18));

            JLabel heading=title(message,26);
            heading.setAlignmentX(.5f);
            box.add(heading);
            box.add(Box.createVerticalStrut(8));
            JLabel sub=new JLabel("Os dados foram registrados com sucesso.");
            sub.setForeground(ComponentesVisuais.MUTED);
            sub.setAlignmentX(.5f);
            box.add(sub);
            box.add(Box.createVerticalStrut(26));
            JButton ok=primary("OK");
            ok.setAlignmentX(.5f);
            ok.setMaximumSize(new Dimension(240,48));
            ok.addActionListener(e->showPage(backPage));
            box.add(ok);
            root.add(box);
            showCustom("Confirmação",root);
        }

        JComponent dashboard() {
            JPanel root = page();
            JPanel metrics = new JPanel(new GridLayout(1,4,14,0));
            metrics.setOpaque(false);
            metrics.setPreferredSize(new Dimension(0,142));
            metrics.add(metric("Saldo atual",money(store.balance()),BLUE,"wallet"));
            metrics.add(metric("Entradas no mês",money(store.income()),new Color(0,177,183),"arrowUp"));
            metrics.add(metric("Saídas no mês",money(store.expenseTotal()),new Color(230,35,43),"arrowDown"));
            metrics.add(metric("A receber",money(store.pending()),ORANGE,"coins"));
            root.add(metrics,BorderLayout.NORTH);

            JPanel center = new JPanel(new GridLayout(1,2,14,0));
            center.setOpaque(false);
            center.setBorder(new EmptyBorder(16,0,16,0));
            ComponentesVisuais.RoundPanel chartCard = new ComponentesVisuais.RoundPanel(16).fill(Color.WHITE);
            chartCard.setLayout(new BorderLayout());
            chartCard.setBorder(new EmptyBorder(16,18,14,18));
            chartCard.add(title("Fluxo de caixa",23),BorderLayout.NORTH);
            chartCard.add(cashFlowChart(store.income(),store.expenseTotal()));
            center.add(chartCard);

            ComponentesVisuais.RoundPanel dueCard = new ComponentesVisuais.RoundPanel(16).fill(Color.WHITE);
            dueCard.setLayout(new BorderLayout());
            dueCard.setBorder(new EmptyBorder(16,12,16,12));
            JLabel dueTitle = title("Próximos vencimentos",23);
            dueTitle.setBorder(new EmptyBorder(0,4,12,4));
            dueCard.add(dueTitle,BorderLayout.NORTH);
            JTable dueTable = styledTable(ComponentesVisuais.readOnlyModel(
                    upcomingRows(),"Cliente","Parcela","Vencimento","Valor","Status"));
            dueTable.setRowHeight(38);
            dueCard.add(new JScrollPane(dueTable));
            center.add(dueCard);
            root.add(center);

            JPanel quick = new JPanel(new GridLayout(1,4,12,0));
            quick.setOpaque(false);
            quick.setPreferredSize(new Dimension(0,62));
            quick.add(quickButton("Novo cliente","user",e -> clientDialog(null)));
            quick.add(quickButton("Nova consulta","calendar",e -> appointmentDialog(null)));
            quick.add(quickButton("Registrar pagamento","coins",e -> showPage("Contas a receber")));
            quick.add(quickButton("Nova saída","logout",e -> expenseDialog(null)));
            root.add(quick,BorderLayout.SOUTH);
            return root;
        }

        private Object[][] upcomingRows() {
            return store.upcomingInstallments().stream()
                    .filter(item -> item.label.equals("1/4")
                            || item.label.equals("1/3")
                            || item.label.equals("3/5"))
                    .sorted(Comparator.comparingInt(item -> item.client.equals("Carlos Mendes") ? 0
                            : item.client.equals("Mariana Silva") ? 1 : 2))
                    .map(item -> new Object[]{
                            item.client,
                            item.client.equals("Carlos Mendes") ? "2/4" : item.label,
                            fmt(item.due),
                            money(item.client.equals("Carlos Mendes") ? item.value : item.remaining()),
                            item.due.isBefore(LivroCaixaModel.DEMO_DATE) ? "Vencido" : "Pendente"
                    })
                    .toArray(Object[][]::new);
        }

        JComponent clients(){return TelasOperacionais.buildClients(this);}
        void clientDialog(Client edit){
            JPanel root=page();
            ComponentesVisuais.RoundPanel card=new ComponentesVisuais.RoundPanel(16).fill(Color.WHITE);
            card.setLayout(new BorderLayout(0,22));
            card.setBorder(new EmptyBorder(24,26,22,26));

            JPanel heading=new JPanel();
            heading.setOpaque(false);
            heading.setLayout(new BoxLayout(heading,BoxLayout.Y_AXIS));
            heading.add(title(edit==null?"Novo cliente":"Editar cliente",32));
            JLabel subtitle=new JLabel(edit==null?"Preencha os dados para cadastrar um novo cliente.":"Atualize os dados cadastrais do cliente.");
            subtitle.setFont(new Font("Segoe UI",Font.PLAIN,18));
            subtitle.setForeground(ComponentesVisuais.MUTED);
            heading.add(Box.createVerticalStrut(5));
            heading.add(subtitle);
            card.add(heading,BorderLayout.NORTH);

            JTextField name=modernField(edit==null?"":edit.name);
            JTextField cpf=modernField(edit==null?"":edit.cpf);
            name.setFont(new Font("Segoe UI",Font.PLAIN,19));
            cpf.setFont(new Font("Segoe UI",Font.PLAIN,19));

            ComponentesVisuais.RoundPanel formCard=new ComponentesVisuais.RoundPanel(14).fill(new Color(248,250,255)).shadow(false);
            formCard.setLayout(new BoxLayout(formCard,BoxLayout.Y_AXIS));
            formCard.setBorder(new EmptyBorder(22,24,22,24));
            formCard.add(taggedField("Nome completo","Obrigatório",name));
            formCard.add(Box.createVerticalStrut(17));
            formCard.add(taggedField("CPF","Obrigatório",cpf));
            JLabel help=new JLabel("Use o formato 000.000.000-00");
            help.setIcon(ComponentesVisuais.vectorIcon("info",16,ComponentesVisuais.MUTED));
            help.setIconTextGap(8);
            help.setForeground(ComponentesVisuais.MUTED);
            help.setFont(new Font("Segoe UI",Font.PLAIN,15));
            help.setAlignmentX(Component.LEFT_ALIGNMENT);
            formCard.add(Box.createVerticalStrut(8));
            formCard.add(help);

            JPanel body=new JPanel(new BorderLayout());
            body.setOpaque(false);
            body.add(formCard,BorderLayout.NORTH);
            JPanel bodyHolder=new JPanel(new BorderLayout());
            bodyHolder.setOpaque(false);
            bodyHolder.add(body,BorderLayout.NORTH);
            card.add(bodyHolder);

            JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,12,0));
            actions.setOpaque(false);
            actions.setBorder(new CompoundBorder(new MatteBorder(1,0,0,0,ComponentesVisuais.LINE),new EmptyBorder(16,0,0,0)));
            JButton cancel=new ComponentesVisuais.RoundButton("Cancelar",ComponentesVisuais.vectorIcon("close",19,ComponentesVisuais.INK),new Color(234,240,249),ComponentesVisuais.INK);
            JButton save=new ComponentesVisuais.RoundButton("Salvar cliente",ComponentesVisuais.vectorIcon("save",20,Color.WHITE),BLUE,Color.WHITE);
            cancel.setPreferredSize(new Dimension(150,50));
            save.setPreferredSize(new Dimension(190,50));
            cancel.addActionListener(e->showPage("Clientes"));
            save.addActionListener(e -> {
                try {
                    store.saveClient(edit, name.getText(), cpf.getText());
                    showPage("Clientes");
                } catch (IllegalArgumentException exception) {
                    error(this, exception.getMessage());
                }
            });
            actions.add(cancel);
            actions.add(save);
            card.add(actions,BorderLayout.SOUTH);
            root.add(card,BorderLayout.NORTH);
            showCustom("Clientes",root);
        }

        JComponent appointments(){return TelasOperacionais.buildAppointments(this);}
        void appointmentDialog(Appointment edit){TelasConsultas.showForm(this,edit);}

        void billingDialog(Appointment a){TelaFaturamento.show(this,a);}

        JComponent receivables(){return TelasFinanceiras.buildReceivables(this);}
        void paymentDialog(Installment x){TelasPagamentos.showPayment(this,x);}
        void paymentConfirmed(Installment x){TelaPagamentoConfirmado.show(this,x);}

        JComponent expenses(){return TelasFinanceiras.buildExpenses(this);}
        void expenseDialog(Expense edit) {
            JPanel root = page();
            ComponentesVisuais.RoundPanel card = new ComponentesVisuais.RoundPanel(17)
                    .fill(new Color(252,253,255))
                    .line(new Color(185,205,237)).shadow(false);
            card.setLayout(new BorderLayout(0,14));
            card.setBorder(new EmptyBorder(18,22,18,22));

            JPanel heading = new JPanel();
            heading.setOpaque(false);
            heading.setLayout(new BoxLayout(heading,BoxLayout.Y_AXIS));
            heading.add(title(edit == null ? "Nova saída" : "Editar saída",42));
            heading.add(Box.createVerticalStrut(3));
            JLabel sub = new JLabel("Registre uma despesa no livro caixa");
            sub.setFont(new Font("Segoe UI",Font.PLAIN,27));
            sub.setForeground(new Color(29,44,70));
            heading.add(sub);
            heading.add(Box.createVerticalStrut(9));
            JLabel hint = new JLabel("Cadastre uma nova opção sem sair desta tela.",
                    ComponentesVisuais.vectorIcon("info",23,new Color(24,67,164)),SwingConstants.LEFT);
            hint.setIconTextGap(9);
            hint.setFont(new Font("Segoe UI",Font.ITALIC,18));
            hint.setForeground(new Color(34,59,106));
            heading.add(hint);
            heading.setPreferredSize(new Dimension(0,138));
            card.add(heading,BorderLayout.NORTH);

            JPanel body = new JPanel(new GridBagLayout());
            body.setOpaque(false);
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridy=0;
            constraints.fill=GridBagConstraints.BOTH;
            constraints.weighty=1;
            constraints.gridx=0;
            constraints.weightx=2.2;
            constraints.insets=new Insets(0,0,0,24);

            JComboBox<String> type = new JComboBox<>(store.expenseTypes.toArray(String[]::new));
            JTextField value = modernField(edit == null ? "1.200,00" : edit.value.toString());
            JTextField date = modernField(fmt(edit == null ? LocalDate.of(2026,11,5) : edit.date));
            JComboBox<String> worker = new JComboBox<>(new String[]{employee,"Bruno Reis"});
            TelasConsultas.styleCombo(type);
            TelasConsultas.styleCombo(worker);
            value.setFont(new Font("Segoe UI",Font.PLAIN,21));
            date.setFont(new Font("Segoe UI",Font.PLAIN,21));
            JTextArea notes = new JTextArea(
                    edit == null ? "Pagamento do aluguel do consultório." : edit.notes,5,30);
            notes.setLineWrap(true);
            notes.setWrapStyleWord(true);
            notes.setFont(new Font("Segoe UI",Font.PLAIN,20));
            notes.setBorder(new EmptyBorder(12,14,12,14));
            if(edit != null) {
                type.setSelectedItem(edit.type);
                worker.setSelectedItem(edit.employee);
            }

            JButton newType = TelasConsultas.largeSecondary(
                    "Novo tipo","plus",190);
            newType.addActionListener(e -> quickEntry(
                    this,
                    "Novo tipo de saída",
                    "Crie uma categoria sem perder os dados da saída.",
                    new String[]{"Nome do tipo"},
                    values -> {
                        String name=values[0].trim();
                        if(name.isBlank()) {
                            throw new IllegalArgumentException("Informe o nome do novo tipo de saída.");
                        }
                        if(store.expenseTypes.stream().anyMatch(item -> item.equalsIgnoreCase(name))) {
                            throw new IllegalArgumentException("Este tipo de saída já está cadastrado.");
                        }
                        store.addExpenseType(name);
                        type.addItem(name);
                        type.setSelectedItem(name);
                    }));
            JPanel typeLine = TelasConsultas.lineWithButton(type,newType);
            JScrollPane notesScroll = new JScrollPane(notes);
            notesScroll.setBorder(new LineBorder(new Color(133,164,219),1,true));
            notesScroll.setPreferredSize(new Dimension(0,190));

            JPanel formArea = new JPanel(new GridBagLayout());
            formArea.setOpaque(false);
            GridBagConstraints field = new GridBagConstraints();
            field.fill=GridBagConstraints.BOTH;
            field.weightx=1;
            field.gridx=0; field.gridy=0; field.insets=new Insets(0,0,16,16);
            formArea.add(TelasConsultas.fieldBlock("Tipo de saída",typeLine),field);
            field.gridx=1; field.insets=new Insets(0,0,16,0);
            formArea.add(TelasConsultas.fieldBlock("Valor",value),field);
            field.gridx=0; field.gridy=1; field.insets=new Insets(0,0,16,16);
            formArea.add(TelasConsultas.fieldBlock("Data",date),field);
            field.gridx=1; field.insets=new Insets(0,0,16,0);
            formArea.add(TelasConsultas.fieldBlock("Funcionário",worker),field);
            field.gridx=0; field.gridy=2; field.gridwidth=2; field.weighty=1;
            field.insets=new Insets(0,0,0,0);
            formArea.add(TelasConsultas.fieldBlock("Observações",notesScroll),field);
            body.add(formArea,constraints);

            constraints.gridx=1;
            constraints.weightx=1;
            constraints.insets=new Insets(0,0,0,0);
            JLabel summaryType = TelasConsultas.summaryValue(
                    String.valueOf(type.getSelectedItem()));
            JLabel summaryValue = TelasConsultas.summaryValue("R$ "+value.getText());
            JLabel summaryDate = TelasConsultas.summaryValue(date.getText());
            JLabel summaryWorker = TelasConsultas.summaryValue(
                    String.valueOf(worker.getSelectedItem()));
            JComponent summary = TelasConsultas.summaryPanel(
                    "Resumo da saída",
                    new String[]{"Tipo de saída","Valor","Data","Funcionário"},
                    summaryType,summaryValue,summaryDate,summaryWorker);
            JPanel summarySlot = new JPanel(new BorderLayout());
            summarySlot.setOpaque(false);
            summarySlot.add(summary,BorderLayout.NORTH);

            Runnable refreshSummary = () -> {
                String amount = value.getText().trim();
                summaryValue.setText(amount.isBlank() ? "R$ 0,00" : "R$ "+amount);
                summaryType.setText(String.valueOf(type.getSelectedItem()));
                summaryDate.setText(date.getText().isBlank() ? "Não informada" : date.getText());
                summaryWorker.setText(String.valueOf(worker.getSelectedItem()));
            };
            ComponentesVisuais.onDocumentChange(value.getDocument(),refreshSummary);
            ComponentesVisuais.onDocumentChange(date.getDocument(),refreshSummary);
            type.addActionListener(e -> refreshSummary.run());
            worker.addActionListener(e -> refreshSummary.run());
            body.add(summarySlot,constraints);
            card.add(body);

            JPanel footer = new JPanel(new BorderLayout(0,12));
            footer.setOpaque(false);
            footer.add(new JSeparator(),BorderLayout.NORTH);
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT,12,0));
            actions.setOpaque(false);
            JButton cancel = TelasConsultas.actionButton(
                    "Cancelar","close",false,215);
            JButton save = TelasConsultas.actionButton(
                    "Salvar saída","save",true,250);
            cancel.addActionListener(e -> showPage("Saídas"));
            save.addActionListener(e -> {
                try {
                    BigDecimal amount = parseMoney(value.getText());
                    String selectedType = String.valueOf(type.getSelectedItem());
                    store.saveExpense(edit, selectedType, selectedType, amount,
                            parseDate(date.getText()), String.valueOf(worker.getSelectedItem()),
                            notes.getText());
                    showPage("Saídas");
                } catch(Exception ex) {
                    error(this,"Revise o valor e a data.");
                }
            });
            actions.add(cancel);
            actions.add(save);
            footer.add(actions);
            card.add(footer,BorderLayout.SOUTH);
            root.add(card);
            showCustom("Saídas",root);
        }

        JComponent reports(){return TelasGerenciamento.buildReports(this);}

        JComponent settings(){return TelasGerenciamento.buildSettings(this);}
    }

    static JPanel page() {
        JPanel panel = new JPanel(new BorderLayout(0,12));
        panel.setBackground(ComponentesVisuais.BG);
        panel.setBorder(new EmptyBorder(18,18,18,18));
        return panel;
    }

    static JLabel title(String text,int size) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI",Font.BOLD,size));
        label.setForeground(ComponentesVisuais.INK);
        return label;
    }

    static JTextField modernField(String text) {
        JTextField field = new JTextField(text);
        field.setFont(NORMAL);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE,46));
        field.setPreferredSize(new Dimension(250,46));
        field.setBackground(Color.WHITE);
        field.setForeground(ComponentesVisuais.INK);
        field.setCaretColor(BLUE);
        field.setBorder(new CompoundBorder(
                new LineBorder(new Color(163,181,211)),new EmptyBorder(8,10,8,10)));
        return field;
    }

    static JPasswordField modernPassword(String text) {
        JPasswordField field = new JPasswordField(text);
        field.setFont(NORMAL);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE,46));
        field.setPreferredSize(new Dimension(250,46));
        field.setBackground(Color.WHITE);
        field.setBorder(new CompoundBorder(
                new LineBorder(new Color(163,181,211)),new EmptyBorder(8,10,8,10)));
        return field;
    }

    static JPanel labeled(String name,Component component) {
        JPanel panel = new JPanel(new BorderLayout(0,5));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(
                Integer.MAX_VALUE,component instanceof JScrollPane ? 135 : 82));
        JLabel label = new JLabel(name);
        label.setFont(BOLD);
        label.setForeground(ComponentesVisuais.INK);
        panel.add(label,BorderLayout.NORTH);
        panel.add(component);
        return panel;
    }

    static JPanel taggedField(String name,String tag,Component component) {
        JPanel panel = new JPanel(new BorderLayout(0,8));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel heading = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
        heading.setOpaque(false);
        JLabel label = title(name,19);
        JLabel badge = new JLabel(tag);
        badge.setFont(new Font("Segoe UI",Font.BOLD,13));
        boolean required = "Obrigatório".equalsIgnoreCase(tag);
        badge.setForeground(required ? new Color(190,25,30) : new Color(70,76,88));
        badge.setOpaque(true);
        badge.setBackground(required ? new Color(255,220,220) : new Color(225,229,236));
        badge.setBorder(new CompoundBorder(
                new LineBorder(required ? new Color(255,145,145) : new Color(185,190,200)),
                new EmptyBorder(3,10,3,10)));
        heading.add(label);
        heading.add(badge);
        panel.add(heading,BorderLayout.NORTH);
        panel.add(component);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE,92));
        return panel;
    }

    static JButton primary(String text) {
        return new ComponentesVisuais.RoundButton(text,null,BLUE,Color.WHITE);
    }

    static JButton secondary(String text) {
        return new ComponentesVisuais.RoundButton(text,null,new Color(235,241,250),ComponentesVisuais.INK);
    }

    static JButton danger(String text) {
        return new ComponentesVisuais.RoundButton(
                text,ComponentesVisuais.vectorIcon("trash",19,RED),new Color(255,238,240),RED);
    }

    static JButton quickButton(String text,ActionListener listener) {
        return quickButton(text,"plus",listener);
    }

    static JButton quickButton(String text,String icon,ActionListener listener) {
        JButton button = new ComponentesVisuais.RoundButton(
                text,ComponentesVisuais.vectorIcon(icon,26,Color.WHITE),BLUE,Color.WHITE);
        button.setFont(new Font("Segoe UI",Font.BOLD,17));
        button.addActionListener(listener);
        return button;
    }

    static JPanel metric(String name,String value,Color color) {
        return metric(name,value,color,"wallet");
    }

    static JPanel metric(String name,String value,Color color,String icon) {
        ComponentesVisuais.RoundPanel panel = new ComponentesVisuais.RoundPanel(16).fill(Color.WHITE);
        panel.setLayout(new BorderLayout(14,0));
        panel.setBorder(new EmptyBorder(16,16,16,16));
        ComponentesVisuais.RoundPanel iconBox = new ComponentesVisuais.RoundPanel(13)
                .fill(new Color(color.getRed(),color.getGreen(),color.getBlue(),28))
                .line(null).shadow(false);
        iconBox.setPreferredSize(new Dimension(62,62));
        iconBox.setLayout(new GridBagLayout());
        iconBox.add(new JLabel(ComponentesVisuais.vectorIcon(icon,34,color)));
        panel.add(iconBox,BorderLayout.WEST);
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text,BoxLayout.Y_AXIS));
        JLabel nameLabel = title(name,17);
        JLabel valueLabel = title(value,27);
        valueLabel.setForeground(color);
        text.add(Box.createVerticalGlue());
        text.add(nameLabel);
        text.add(Box.createVerticalStrut(8));
        text.add(valueLabel);
        text.add(Box.createVerticalGlue());
        panel.add(text);
        return panel;
    }

    static JTable styledTable(DefaultTableModel model) {
        JTable table = ComponentesVisuais.modernTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return table;
    }

    static JPanel tablePanel(String heading,String[] columns,Object[][] rows) {
        ComponentesVisuais.RoundPanel panel = new ComponentesVisuais.RoundPanel(16).fill(Color.WHITE);
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(14,14,14,14));
        JLabel title = TelaLivroCaixa.title(heading,21);
        title.setBorder(new EmptyBorder(0,2,12,2));
        panel.add(title,BorderLayout.NORTH);
        DefaultTableModel model = ComponentesVisuais.readOnlyModel(rows,columns);
        JScrollPane scroll = new JScrollPane(styledTable(model));
        scroll.setBorder(new LineBorder(ComponentesVisuais.LINE));
        panel.add(scroll);
        return panel;
    }

    static ComponentesVisuais.CashFlowChart cashFlowChart(BigDecimal income,BigDecimal expense) {
        double incomeValue = income.max(BigDecimal.ONE).doubleValue();
        double expenseValue = expense.max(BigDecimal.ONE).doubleValue();
        double[] incomes = {
                incomeValue*.63,incomeValue*.71,incomeValue*.79,
                incomeValue*.90,incomeValue*.98,incomeValue
        };
        double[] expenses = {
                expenseValue*.80,expenseValue*.96,expenseValue,
                expenseValue*.89,expenseValue*1.02,expenseValue
        };
        return new ComponentesVisuais.CashFlowChart().values(
                new String[]{"Jun","Jul","Ago","Set","Out","Nov"},incomes,expenses);
    }

    static class NavButton extends JButton {
        NavButton(String text,Icon icon) {
            super(text,icon);
            setHorizontalAlignment(LEFT);
            setIconTextGap(16);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI",Font.BOLD,19));
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(12,24,12,12));
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D copy = (Graphics2D)graphics.create();
            ComponentesVisuais.antialias(copy);
            Color background = getBackground();
            copy.setColor(background.getAlpha() > 0
                    ? background
                    : getModel().isRollover()
                    ? new Color(255,255,255,24)
                    : new Color(0,0,0,0));
            copy.fillRoundRect(10,2,getWidth()-20,getHeight()-4,13,13);
            copy.dispose();
            super.paintComponent(graphics);
        }
    }

    static Image appIcon() {
        BufferedImage image = new BufferedImage(64,64,BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        ComponentesVisuais.antialias(graphics);
        graphics.setColor(BLUE);
        graphics.fillRoundRect(3,3,58,58,15,15);
        Icon wallet = ComponentesVisuais.vectorIcon("wallet",38,Color.WHITE);
        wallet.paintIcon(null,graphics,13,13);
        graphics.dispose();
        return image;
    }

    static String fmt(LocalDate date) {
        return date.format(DATE);
    }

    static LocalDate parseDate(String text) {
        return LocalDate.parse(text.trim(),DATE);
    }

    static String money(BigDecimal value) {
        return BRL.format(value);
    }

    static BigDecimal parseMoney(String text) {
        String normalized = text.replace("R$","")
                .replace("\u00A0","")
                .replace("\u202F","")
                .replaceAll("\\s+","")
                .trim();
        if(normalized.contains(",")) {
            normalized=normalized.replace(".","").replace(",",".");
        }
        return new BigDecimal(normalized).setScale(2,RoundingMode.HALF_UP);
    }

    static void info(Component component,String message) {
        JOptionPane.showMessageDialog(
                component,message,"Livro Caixa",JOptionPane.INFORMATION_MESSAGE);
    }

    static void error(Component component,String message) {
        JOptionPane.showMessageDialog(
                component,message,"Atenção",JOptionPane.ERROR_MESSAGE);
    }

    static void quickEntry(Component ownerComponent,String dialogTitle,String subtitle,
                           String[] labels,Consumer<String[]> onSave) {
        Window owner=ownerComponent instanceof Window window
                ? window:SwingUtilities.getWindowAncestor(ownerComponent);
        JDialog dialog=new JDialog(owner,Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        ComponentesVisuais.RoundPanel root=new ComponentesVisuais.RoundPanel(18).fill(Color.WHITE)
                .line(new Color(170,192,226)).shadow(true);
        root.setLayout(new BorderLayout(0,18));
        root.setBorder(new EmptyBorder(22,24,20,24));
        root.setPreferredSize(new Dimension(590,labels.length==1?315:420));

        JPanel header=new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel copy=new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy,BoxLayout.Y_AXIS));
        copy.add(title(dialogTitle,25));
        copy.add(Box.createVerticalStrut(4));
        JLabel description=new JLabel(subtitle);
        description.setFont(new Font("Segoe UI",Font.PLAIN,15));
        description.setForeground(ComponentesVisuais.MUTED);
        copy.add(description);
        header.add(copy);
        JButton close=new JButton(ComponentesVisuais.vectorIcon("close",18,ComponentesVisuais.MUTED));
        close.setBorder(new EmptyBorder(5,8,5,8));
        close.setContentAreaFilled(false);
        close.setFocusPainted(false);
        close.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        close.addActionListener(e->dialog.dispose());
        header.add(close,BorderLayout.EAST);
        root.add(header,BorderLayout.NORTH);

        JTextField[] fields=new JTextField[labels.length];
        JPanel form=new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form,BoxLayout.Y_AXIS));
        for(int index=0;index<labels.length;index++) {
            fields[index]=modernField("");
            fields[index].setFont(new Font("Segoe UI",Font.PLAIN,19));
            form.add(TelasConsultas.fieldBlock(labels[index],fields[index]));
            if(index<labels.length-1) form.add(Box.createVerticalStrut(12));
        }
        root.add(form);

        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        actions.setOpaque(false);
        actions.setBorder(new CompoundBorder(
                new MatteBorder(1,0,0,0,ComponentesVisuais.LINE),new EmptyBorder(14,0,0,0)));
        JButton cancel=TelasConsultas.actionButton("Cancelar","close",false,180);
        JButton save=TelasConsultas.actionButton("Cadastrar","save",true,175);
        cancel.addActionListener(e->dialog.dispose());
        save.addActionListener(e->{
            String[] values=Arrays.stream(fields).map(JTextField::getText).toArray(String[]::new);
            try {
                onSave.accept(values);
                dialog.dispose();
            } catch(IllegalArgumentException exception) {
                error(dialog,exception.getMessage());
            }
        });
        actions.add(cancel);
        actions.add(save);
        root.add(actions,BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        SwingUtilities.invokeLater(fields[0]::requestFocusInWindow);
        dialog.setVisible(true);
    }

    static boolean confirm(Component ownerComponent,String message){
        String normalized=message.toLowerCase(Locale.ROOT);
        String dialogTitle="Confirmar ação",actionLabel="Confirmar",detail="Revise os dados antes de continuar.",icon="warning";
        if(normalized.startsWith("excluir")){
            dialogTitle="Confirmar exclusão";
            actionLabel=normalized.contains("cliente")?"Excluir cliente":normalized.contains("saída")?"Excluir saída":normalized.contains("funcionário")?"Excluir funcionário":normalized.contains("cadastro")?"Excluir cadastro":"Excluir";
            detail="O registro será removido e esta ação não poderá ser desfeita.";
            icon="trash";
        }else if(normalized.startsWith("sair da conta")){
            dialogTitle="Sair da conta";
            actionLabel="Sair da conta";
            detail="Você voltará à tela de acesso e poderá entrar novamente.";
            icon="logout";
        }else if(normalized.startsWith("cancelar")){
            dialogTitle="Confirmar cancelamento";
            actionLabel="Cancelar parcela";
            detail="A parcela será marcada como cancelada. Confira antes de continuar.";
        }else if(normalized.startsWith("desfazer")){
            dialogTitle="Confirmar reversão";
            actionLabel="Desfazer baixa";
            detail="O pagamento e o desconto associados serão revertidos.";
            icon="refresh";
        }

        Window owner=ownerComponent instanceof Window window?window:SwingUtilities.getWindowAncestor(ownerComponent);
        JDialog confirmDialog=new JDialog(owner,Dialog.ModalityType.APPLICATION_MODAL);
        confirmDialog.setUndecorated(true);
        confirmDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        confirmDialog.setResizable(false);

        boolean[] accepted={false};
        ComponentesVisuais.RoundPanel root=new ComponentesVisuais.RoundPanel(18).fill(Color.WHITE).line(new Color(190,204,226)).shadow(true);
        root.setLayout(new BorderLayout(0,18));
        root.setBorder(new EmptyBorder(22,24,20,24));
        root.setPreferredSize(new Dimension(540,275));

        JPanel header=new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title(dialogTitle,22),BorderLayout.WEST);
        JButton close=new JButton(ComponentesVisuais.vectorIcon("close",18,ComponentesVisuais.MUTED));
        close.setBorder(new EmptyBorder(5,8,5,8));
        close.setContentAreaFilled(false);
        close.setFocusPainted(false);
        close.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        close.addActionListener(e->confirmDialog.dispose());
        header.add(close,BorderLayout.EAST);
        root.add(header,BorderLayout.NORTH);

        JPanel content=new JPanel(new BorderLayout(18,0));
        content.setOpaque(false);
        ComponentesVisuais.RoundPanel iconBox=new ComponentesVisuais.RoundPanel(14).fill(new Color(255,233,235)).line(new Color(255,193,198)).shadow(false);
        iconBox.setPreferredSize(new Dimension(68,68));
        iconBox.setLayout(new GridBagLayout());
        iconBox.add(new JLabel(ComponentesVisuais.vectorIcon(icon,32,ComponentesVisuais.RED)));
        content.add(iconBox,BorderLayout.WEST);
        JPanel copy=new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy,BoxLayout.Y_AXIS));
        JTextArea question=new JTextArea(message);
        question.setEditable(false);
        question.setFocusable(false);
        question.setOpaque(false);
        question.setLineWrap(true);
        question.setWrapStyleWord(true);
        question.setRows(2);
        question.setColumns(29);
        question.setFont(new Font("Segoe UI",Font.BOLD,18));
        question.setForeground(ComponentesVisuais.INK);
        question.setBorder(null);
        JTextArea explanation=new JTextArea(detail);
        explanation.setEditable(false);
        explanation.setFocusable(false);
        explanation.setOpaque(false);
        explanation.setLineWrap(true);
        explanation.setWrapStyleWord(true);
        explanation.setRows(2);
        explanation.setColumns(29);
        explanation.setFont(new Font("Segoe UI",Font.PLAIN,15));
        explanation.setForeground(ComponentesVisuais.MUTED);
        explanation.setBorder(null);
        copy.add(question);
        copy.add(Box.createVerticalStrut(5));
        copy.add(explanation);
        content.add(copy);
        root.add(content);

        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
        actions.setOpaque(false);
        actions.setBorder(new CompoundBorder(new MatteBorder(1,0,0,0,ComponentesVisuais.LINE),new EmptyBorder(15,0,0,0)));
        JButton back=new ComponentesVisuais.RoundButton("Voltar",ComponentesVisuais.vectorIcon("back",18,ComponentesVisuais.INK),new Color(235,241,250),ComponentesVisuais.INK);
        JButton proceed=new ComponentesVisuais.RoundButton(actionLabel,ComponentesVisuais.vectorIcon(icon,18,Color.WHITE),ComponentesVisuais.RED,Color.WHITE);
        back.setPreferredSize(new Dimension(140,48));
        proceed.setPreferredSize(new Dimension(190,48));
        back.addActionListener(e->confirmDialog.dispose());
        proceed.addActionListener(e->{accepted[0]=true;confirmDialog.dispose();});
        actions.add(back);
        actions.add(proceed);
        root.add(actions,BorderLayout.SOUTH);

        confirmDialog.setContentPane(root);
        confirmDialog.getRootPane().setDefaultButton(back);
        confirmDialog.getRootPane().registerKeyboardAction(e->confirmDialog.dispose(),KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),JComponent.WHEN_IN_FOCUSED_WINDOW);
        confirmDialog.pack();
        confirmDialog.setLocationRelativeTo(ownerComponent);
        confirmDialog.setVisible(true);
        return accepted[0];
    }
}
