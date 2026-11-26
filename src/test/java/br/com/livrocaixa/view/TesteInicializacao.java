package br.com.livrocaixa.view;

import br.com.livrocaixa.view.dados.Appointment;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

/** Verificação rápida de construção de todas as telas, sem interação do usuário. */
public final class TesteInicializacao {
    public static void main(String[] args) throws Exception {
        Path isolatedHome = Files.createTempDirectory("livro-caixa-smoke-");
        System.setProperty("user.home", isolatedHome.toString());
        AtomicReference<Throwable> uncaught = new AtomicReference<>();
        Thread.setDefaultUncaughtExceptionHandler(
                (thread, error) -> uncaught.compareAndSet(null, error));

        SwingUtilities.invokeAndWait(TesteInicializacao::runVisualFlow);

        if (uncaught.get() != null) {
            throw new AssertionError("Exceção não capturada na EDT", uncaught.get());
        }
        System.out.println("SMOKE_TEST_OK");
        System.exit(0);
    }

    private static void runVisualFlow() {
        CoordenadorTelas controller = new CoordenadorTelas();
        TelaLivroCaixa.JanelaLogin login = new TelaLivroCaixa.JanelaLogin(controller);
        login.setVisible(true);
        render(login, "00-login");
        login.setSize(980, 580);
        render(login, "00b-login-minimo");
        login.dispose();

        TelaLivroCaixa.JanelaPrincipal frame = new TelaLivroCaixa.JanelaPrincipal(controller, "Ana Souza");
        frame.setExtendedState(JFrame.NORMAL);
        frame.setSize(1500, 850);
        frame.setVisible(true);

        verifySampleData(frame);
        exerciseMainRecords(frame);
        exerciseBilling(frame);
        exercisePayments(frame);
        exerciseExpenses(frame);
        exerciseReportsAndSettings(frame);
        exerciseLogout(frame);
    }

    private static void verifySampleData(TelaLivroCaixa.JanelaPrincipal frame) {
        expect(frame.store.clients.size() == 5, "amostra deve ter 5 clientes");
        expect(frame.store.appointments.size() == 3, "amostra deve ter 3 consultas");
        expect(frame.store.expenses.size() == 7, "amostra deve ter 7 saídas");
        expectMoney(frame.store.income(), "12680.00", "entradas");
        expectMoney(frame.store.expenseTotal(), "4260.00", "saídas");
        expectMoney(frame.store.balance(), "8420.00", "saldo");
        expectMoney(frame.store.pending(), "3180.00", "a receber");
    }

    private static void exerciseMainRecords(TelaLivroCaixa.JanelaPrincipal frame) {
        frame.showPage("Painel");
        render(frame, "01-painel");

        frame.showPage("Clientes");
        render(frame, "02-clientes-lista");
        exerciseFilter(frame, "Buscar", "Limpar", 5);
        frame.clientDialog(null);
        render(frame, "03-cliente-cadastro");
        frame.setSize(1180, 700);
        render(frame, "03b-cliente-cadastro-minimo");
        frame.setSize(1500, 850);
        renderConfirmation(frame, "Excluir o cliente \"Carlos Mendes\"?", "17-excluir-cliente");
        exerciseButton(frame, "Cancelar");

        frame.showPage("Consultas");
        render(frame, "04-consultas-lista");
        exerciseButton(frame, "Aplicar filtros");
        exerciseButton(frame, "Limpar");
        frame.appointmentDialog(null);
        render(frame, "05-consulta-nova");
        renderQuickEntry(frame, "Novo cliente", "05b-consulta-novo-cliente-popup");
        renderQuickEntry(frame, "Novo tipo", "05c-consulta-novo-tipo-popup");
        exerciseButton(frame, "Cancelar");
    }

    private static void exerciseBilling(TelaLivroCaixa.JanelaPrincipal frame) {
        Appointment invoiceAppointment = frame.store.appointments.get(0);
        int paymentCount = frame.store.payments.size();
        int installmentCount = frame.store.installments.size();
        int nextPayment = frame.store.nextPayment;
        int nextInstallment = frame.store.nextInstallment;
        boolean wasBilled = invoiceAppointment.billed;

        frame.billingDialog(invoiceAppointment);
        render(frame, "06a-fatura-a-vista");
        exerciseButton(frame, "À prazo");
        exerciseButton(frame, "Gerar parcelas");
        render(frame, "06b-fatura-a-prazo");

        JTable installmentPreview = findVisible(frame, JTable.class);
        expect(installmentPreview != null, "prévia de parcelas deve estar visível");
        expect(
                installmentPreview.getRowCount() == 6,
                "entrada mais 5 parcelas devem ser geradas");
        exerciseButton(frame, "Personalizados");
        String[] customValues = {
                "R$ 220,00", "R$ 230,00", "R$ 240,00", "R$ 250,00", "R$ 260,00"
        };
        for (int row = 1; row < installmentPreview.getRowCount(); row++) {
            installmentPreview.setValueAt(customValues[row - 1], row, 3);
        }
        render(frame, "06c-fatura-parcelas-personalizadas");

        exerciseButton(frame, "Salvar fatura");
        render(frame, "07-fatura-salva-sucesso");
        expect(invoiceAppointment.billed, "consulta deve ser marcada como faturada");
        expect(
                frame.store.paymentsForInvoice(invoiceAppointment.id).size() == 1,
                "entrada deve gerar um pagamento");
        expect(
                frame.store.installmentsForInvoice(invoiceAppointment.id).size() == 5,
                "devem ser salvas 5 parcelas personalizadas");
        BigDecimal installmentTotal = frame.store.installmentsForInvoice(invoiceAppointment.id)
                .stream()
                .map(item -> item.value)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        expectMoney(installmentTotal, "1200.00", "total das parcelas personalizadas");

        frame.store.payments.subList(paymentCount, frame.store.payments.size()).clear();
        frame.store.installments.subList(installmentCount, frame.store.installments.size()).clear();
        frame.store.nextPayment = nextPayment;
        frame.store.nextInstallment = nextInstallment;
        invoiceAppointment.billed = wasBilled;
        exerciseButton(frame, "OK");
    }

    private static void exercisePayments(TelaLivroCaixa.JanelaPrincipal frame) {
        frame.showPage("Contas a receber");
        render(frame, "08-contas-a-receber");
        exerciseButton(frame, "Aplicar filtros");
        exerciseButton(frame, "Limpar");

        frame.paymentDialog(frame.store.installments.get(0));
        render(frame, "09-pagamento-registrar");
        exerciseButton(frame, "%");
        render(frame, "09b-pagamento-saldo-negativo");
        exerciseButton(frame, "R$");
        exerciseButton(frame, "Confirmar pagamento");
        render(frame, "10-pagamento-confirmado");
        exerciseButton(frame, "Ver fatura");
        render(frame, "10b-fatura-detalhes");
        exerciseButton(frame, "Voltar para contas a receber");

        frame.store.payments.remove(frame.store.payments.size() - 1);
        frame.store.installments.get(0).paid = new BigDecimal("100.00");
    }

    private static void exerciseExpenses(TelaLivroCaixa.JanelaPrincipal frame) {
        frame.showPage("Saídas");
        render(frame, "11-saidas-lista");
        exerciseButton(frame, "Aplicar filtros");
        exerciseButton(frame, "Limpar");
        frame.expenseDialog(null);
        render(frame, "12-saida-nova");
        renderQuickEntry(frame, "Novo tipo", "12b-tipo-saida-popup");
        render(frame, "12c-saida-nova-retorno");
        exerciseButton(frame, "Cancelar");
        frame.expenseDialog(frame.store.expenses.get(0));
        render(frame, "12d-saida-editar");
        exerciseButton(frame, "Cancelar");
    }

    private static void exerciseReportsAndSettings(TelaLivroCaixa.JanelaPrincipal frame) {
        frame.showPage("Relatórios");
        render(frame, "13a-relatorios-visao-geral");
        exerciseReportTab(frame, "Saídas");
        render(frame, "13b-relatorio-saidas");
        exerciseReportTab(frame, "Consultas");
        render(frame, "13c-relatorio-consultas");
        exerciseReportTab(frame, "Recebimentos");
        render(frame, "13d-relatorio-recebimentos");

        frame.showPage("Configurações");
        render(frame, "14-configuracoes");
        frame.setSize(1180, 700);
        render(frame, "14b-configuracoes-minimo");
        frame.setSize(1500, 850);
        clickNthButton(frame, "Gerenciar", 0);
        render(frame, "15-funcionarios-perfis");
        frame.setSize(1180, 700);
        render(frame, "15b-funcionarios-perfis-minimo");
        frame.setSize(1500, 850);
        exerciseButton(frame, "Voltar");
        frame.showPage("Configurações");
        clickNthButton(frame, "Gerenciar", 2);
        render(frame, "16-cadastros-auxiliares");
        exerciseButton(frame, "Voltar para configurações");
        render(frame, "16b-cadastros-auxiliares-voltar");
    }

    private static void exerciseLogout(TelaLivroCaixa.JanelaPrincipal frame) {
        exerciseButton(frame, "Funcionário: Ana Souza");
        JPopupMenu menu = selectedPopupMenu();
        expect(menu != null && menu.isShowing(), "menu da conta deve ser exibido");
        render(frame, "17-menu-da-conta");

        JButton logout = find(menu, JButton.class);
        expect(logout != null && "Sair da conta".equals(logout.getText()),
                "menu deve oferecer a opção de sair da conta");

        AtomicReference<Throwable> captureError = new AtomicReference<>();
        Timer timer = new Timer(250, event -> {
            JDialog dialog = findShowingDialog();
            try {
                expect(dialog != null, "confirmação de saída deve ser exibida");
                renderWindow(dialog, "18-confirmar-saida-da-conta");
                exerciseButton(dialog, "Sair da conta");
            } catch (Throwable error) {
                captureError.set(error);
                if (dialog != null) {
                    dialog.dispose();
                }
            }
        });
        timer.setRepeats(false);
        timer.start();
        logout.doClick();

        if (captureError.get() != null) {
            throw new AssertionError("Falha ao testar saída da conta", captureError.get());
        }
        expect(!frame.isDisplayable(), "janela principal deve ser encerrada após sair");

        TelaLivroCaixa.JanelaLogin login = null;
        for (Window window : Window.getWindows()) {
            if (window instanceof TelaLivroCaixa.JanelaLogin candidate && candidate.isShowing()) {
                login = candidate;
                break;
            }
        }
        expect(login != null, "tela de login deve reaparecer após sair da conta");
        render(login, "19-login-apos-sair");
        login.dispose();
    }

    private static JPopupMenu selectedPopupMenu() {
        for (MenuElement element : MenuSelectionManager.defaultManager().getSelectedPath()) {
            if (element instanceof JPopupMenu popup) {
                return popup;
            }
        }
        return null;
    }

    private static void exerciseFilter(
            JFrame frame,
            String apply,
            String clear,
            int expectedRows) {
        JTextField field = find(frame, JTextField.class);
        JTable table = find(frame, JTable.class);
        if (field == null || table == null) {
            throw new AssertionError("Filtro ou tabela ausente");
        }
        field.setText("Carlos");
        exerciseButton(frame, apply);
        expect(table.getRowCount() == 1, "filtro por Carlos deve exibir 1 linha");
        exerciseButton(frame, clear);
        expect(
                table.getRowCount() == expectedRows,
                "limpar filtro deve restaurar " + expectedRows + " linhas");
    }

    private static void exerciseButton(Container root, String text) {
        AbstractButton button = findAbstractButton(root, text);
        if (button == null) {
            throw new AssertionError("Botão não encontrado: " + text);
        }
        button.doClick();
    }

    private static void clickNthButton(Container root, String text, int index) {
        java.util.List<JButton> buttons = new java.util.ArrayList<>();
        collectButtons(root, text, buttons);
        if (index < 0 || index >= buttons.size()) {
            throw new AssertionError(
                    "Botão " + text + " #" + index + " ausente; encontrados=" + buttons.size());
        }
        buttons.get(index).doClick();
    }

    private static void collectButtons(
            Container root,
            String text,
            java.util.List<JButton> out) {
        for (Component component : root.getComponents()) {
            if (component instanceof JButton button && text.equals(button.getText())) {
                out.add(button);
            }
            if (component instanceof Container nested) {
                collectButtons(nested, text, out);
            }
        }
    }

    private static AbstractButton findAbstractButton(Container root, String text) {
        for (Component component : root.getComponents()) {
            if (component instanceof AbstractButton button && text.equals(button.getText())) {
                return button;
            }
            if (component instanceof Container nested) {
                AbstractButton found = findAbstractButton(nested, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static void exerciseReportTab(Container root, String text) {
        AbstractButton button = findByClassAndText(root, "ReportTabButton", text);
        if (button == null) {
            throw new AssertionError("Aba de relatório ausente: " + text);
        }
        button.doClick();
    }

    private static AbstractButton findByClassAndText(
            Container root,
            String simpleClass,
            String text) {
        for (Component component : root.getComponents()) {
            if (component instanceof AbstractButton button
                    && simpleClass.equals(component.getClass().getSimpleName())
                    && text.equals(button.getText())) {
                return button;
            }
            if (component instanceof Container nested) {
                AbstractButton found = findByClassAndText(nested, simpleClass, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static <T extends Component> T find(Container root, Class<T> type) {
        for (Component component : root.getComponents()) {
            if (type.isInstance(component)) {
                return type.cast(component);
            }
            if (component instanceof Container nested) {
                T found = find(nested, type);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static <T extends Component> T findVisible(Container root, Class<T> type) {
        for (Component component : root.getComponents()) {
            if (type.isInstance(component) && component.isShowing()) {
                return type.cast(component);
            }
            if (component instanceof Container nested) {
                T found = findVisible(nested, type);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static void expectMoney(BigDecimal actual, String expected, String label) {
        expect(
                actual.compareTo(new BigDecimal(expected)) == 0,
                label + ": esperado " + expected + ", obtido " + actual);
    }

    private static void expect(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void renderConfirmation(JFrame frame, String message, String name) {
        AtomicReference<Throwable> captureError = new AtomicReference<>();
        Timer timer = new Timer(250, event -> {
            try {
                JDialog dialog = findShowingDialog();
                if (dialog == null) {
                    throw new AssertionError("Diálogo de confirmação não foi exibido");
                }
                renderWindow(dialog, name);
                dialog.dispose();
            } catch (Throwable error) {
                captureError.set(error);
            }
        });
        timer.setRepeats(false);
        timer.start();

        boolean confirmed = TelaLivroCaixa.confirm(frame, message);
        expect(!confirmed, "o teste visual não deve confirmar a exclusão");
        if (captureError.get() != null) {
            throw new AssertionError("Falha ao capturar diálogo", captureError.get());
        }
    }

    private static void renderQuickEntry(JFrame frame, String buttonText, String name) {
        AtomicReference<Throwable> captureError = new AtomicReference<>();
        Timer timer = new Timer(250, event -> {
            try {
                JDialog dialog = findShowingDialog();
                if (dialog == null) {
                    throw new AssertionError("Popup de cadastro não foi exibido");
                }
                renderWindow(dialog, name);
                dialog.dispose();
            } catch (Throwable error) {
                captureError.set(error);
            }
        });
        timer.setRepeats(false);
        timer.start();
        exerciseButton(frame, buttonText);
        if (captureError.get() != null) {
            throw new AssertionError("Falha ao capturar popup de cadastro", captureError.get());
        }
    }

    private static JDialog findShowingDialog() {
        for (Window window : Window.getWindows()) {
            if (window instanceof JDialog dialog && dialog.isShowing()) {
                return dialog;
            }
        }
        return null;
    }

    private static void render(JFrame frame, String name) {
        try {
            frame.validate();
            BufferedImage image = new BufferedImage(
                    frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            frame.printAll(graphics);
            graphics.dispose();
            Path directory = Path.of("tmp", "qa");
            Files.createDirectories(directory);
            ImageIO.write(image, "png", directory.resolve(name + ".png").toFile());
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private static void renderWindow(Window window, String name) {
        try {
            window.validate();
            BufferedImage image = new BufferedImage(
                    window.getWidth(), window.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            window.printAll(graphics);
            graphics.dispose();
            Path directory = Path.of("tmp", "qa");
            Files.createDirectories(directory);
            ImageIO.write(image, "png", directory.resolve(name + ".png").toFile());
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
