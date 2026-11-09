package livrocaixa.view;

import livrocaixa.controller.LivroCaixaController;
import livrocaixa.model.*;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

/** Verificação rápida de construção de todas as telas, sem interação do usuário. */
public final class SmokeTest {
    public static void main(String[] args) throws Exception {
        Path isolatedHome=Files.createTempDirectory("livro-caixa-smoke-");
        System.setProperty("user.home",isolatedHome.toString());
        AtomicReference<Throwable> uncaught=new AtomicReference<>();
        Thread.setDefaultUncaughtExceptionHandler((thread,error)->uncaught.compareAndSet(null,error));
        SwingUtilities.invokeAndWait(() -> {
            LivroCaixaController controller = new LivroCaixaController();
            LivroCaixaView.LoginFrame login=new LivroCaixaView.LoginFrame(controller);login.setVisible(true);render(login,"00-login");login.dispose();
            LivroCaixaView.MainFrame frame = new LivroCaixaView.MainFrame(controller, "Ana Souza");
            frame.setSize(1500, 850);
            frame.setVisible(true);
            expect(frame.store.clients.size()==5,"amostra deve ter 5 clientes");
            expect(frame.store.appointments.size()==3,"amostra deve ter 3 consultas");
            expect(frame.store.expenses.size()==7,"amostra deve ter 7 saídas");
            expectMoney(frame.store.income(),"12680.00","entradas");
            expectMoney(frame.store.expenseTotal(),"4260.00","saídas");
            expectMoney(frame.store.balance(),"8420.00","saldo");
            expectMoney(frame.store.pending(),"3180.00","a receber");
            frame.showPage("Painel"); render(frame,"01-painel");
            frame.showPage("Clientes"); render(frame,"02-clientes-lista"); exerciseFilter(frame,"Buscar","Limpar",5);
            frame.clientDialog(null); render(frame,"03-cliente-cadastro"); exerciseButton(frame,"Cancelar");
            frame.showPage("Consultas"); render(frame,"04-consultas-lista"); exerciseButton(frame,"Aplicar filtros"); exerciseButton(frame,"Limpar");
            frame.appointmentDialog(null); render(frame,"05-consulta-nova"); exerciseButton(frame,"Cancelar");
            frame.billingDialog(frame.store.appointments.get(0)); render(frame,"06a-fatura-a-vista");
            exerciseButton(frame,"À prazo");exerciseButton(frame,"Gerar parcelas");render(frame,"06b-fatura-a-prazo");exerciseButton(frame,"Voltar");
            frame.showSuccessPage("Fatura salva com sucesso","Consultas");render(frame,"07-fatura-salva-popup");exerciseButton(frame,"OK");
            frame.showPage("Contas a receber"); render(frame,"08-contas-a-receber"); exerciseButton(frame,"Aplicar filtros"); exerciseButton(frame,"Limpar");
            frame.paymentDialog(frame.store.installments.get(0)); render(frame,"09-pagamento-registrar");
            exerciseButton(frame,"Confirmar pagamento"); render(frame,"10-pagamento-confirmado");
            exerciseButton(frame,"Voltar para contas a receber");
            frame.store.payments.remove(frame.store.payments.size()-1);frame.store.installments.get(0).paid=new BigDecimal("100.00");
            frame.showPage("Saídas"); render(frame,"11-saidas-lista"); exerciseButton(frame,"Aplicar filtros"); exerciseButton(frame,"Limpar");
            frame.expenseDialog(null); render(frame,"12-saida-nova"); exerciseButton(frame,"Cancelar");
            frame.showPage("Relatórios");render(frame,"13a-relatorios-visao-geral");exerciseReportTab(frame,"Saídas");render(frame,"13b-relatorio-saidas");exerciseReportTab(frame,"Consultas");render(frame,"13c-relatorio-consultas");exerciseReportTab(frame,"Recebimentos");render(frame,"13d-relatorio-recebimentos");
            frame.showPage("Configurações");render(frame,"14-configuracoes");clickNthButton(frame,"Gerenciar",0);render(frame,"15-funcionarios-perfis");exerciseButton(frame,"Voltar");frame.showPage("Configurações");clickNthButton(frame,"Gerenciar",2);render(frame,"16-cadastros-auxiliares");
            frame.dispose();
        });
        if(uncaught.get()!=null)throw new AssertionError("Exceção não capturada na EDT",uncaught.get());
        System.out.println("SMOKE_TEST_OK");
        System.exit(0);
    }

    private static void exerciseFilter(JFrame frame,String apply,String clear,int expectedRows){
        JTextField field=find(frame,JTextField.class); JTable table=find(frame,JTable.class);
        if(field==null||table==null)throw new AssertionError("Filtro ou tabela ausente");
        field.setText("Carlos");exerciseButton(frame,apply);expect(table.getRowCount()==1,"filtro por Carlos deve exibir 1 linha");
        exerciseButton(frame,clear);expect(table.getRowCount()==expectedRows,"limpar filtro deve restaurar "+expectedRows+" linhas");
    }

    private static void exerciseButton(Container root,String text){AbstractButton button=findAbstractButton(root,text);if(button==null)throw new AssertionError("Botão não encontrado: "+text);button.doClick();}
    private static void clickNthButton(Container root,String text,int index){java.util.List<JButton> buttons=new java.util.ArrayList<>();collectButtons(root,text,buttons);if(index<0||index>=buttons.size())throw new AssertionError("Botão "+text+" #"+index+" ausente; encontrados="+buttons.size());buttons.get(index).doClick();}
    private static void collectButtons(Container root,String text,java.util.List<JButton> out){for(Component c:root.getComponents()){if(c instanceof JButton b&&text.equals(b.getText()))out.add(b);if(c instanceof Container nested)collectButtons(nested,text,out);}}
    private static JButton findButton(Container root,String text){for(Component c:root.getComponents()){if(c instanceof JButton b&&text.equals(b.getText()))return b;if(c instanceof Container nested){JButton found=findButton(nested,text);if(found!=null)return found;}}return null;}
    private static AbstractButton findAbstractButton(Container root,String text){for(Component c:root.getComponents()){if(c instanceof AbstractButton b&&text.equals(b.getText()))return b;if(c instanceof Container nested){AbstractButton found=findAbstractButton(nested,text);if(found!=null)return found;}}return null;}
    private static void exerciseReportTab(Container root,String text){AbstractButton button=findByClassAndText(root,"ReportTabButton",text);if(button==null)throw new AssertionError("Aba de relatório ausente: "+text);button.doClick();}
    private static AbstractButton findByClassAndText(Container root,String simpleClass,String text){for(Component c:root.getComponents()){if(c instanceof AbstractButton b&&simpleClass.equals(c.getClass().getSimpleName())&&text.equals(b.getText()))return b;if(c instanceof Container nested){AbstractButton found=findByClassAndText(nested,simpleClass,text);if(found!=null)return found;}}return null;}
    private static JComboBox<?> findComboWithItem(Container root,String item){for(Component c:root.getComponents()){if(c instanceof JComboBox<?> box){for(int i=0;i<box.getItemCount();i++)if(item.equals(String.valueOf(box.getItemAt(i))))return box;}if(c instanceof Container nested){JComboBox<?> found=findComboWithItem(nested,item);if(found!=null)return found;}}return null;}
    private static <T extends Component> T find(Container root,Class<T> type){for(Component c:root.getComponents()){if(type.isInstance(c))return type.cast(c);if(c instanceof Container nested){T found=find(nested,type);if(found!=null)return found;}}return null;}
    private static void expectMoney(BigDecimal actual,String expected,String label){expect(actual.compareTo(new BigDecimal(expected))==0,label+": esperado "+expected+", obtido "+actual);}
    private static void expect(boolean condition,String message){if(!condition)throw new AssertionError(message);}

    private static void render(JFrame frame,String name){
        try{
            frame.validate();
            BufferedImage image=new BufferedImage(frame.getWidth(),frame.getHeight(),BufferedImage.TYPE_INT_ARGB);
            Graphics2D g=image.createGraphics();frame.printAll(g);g.dispose();
            Path dir=Path.of("tmp","qa");Files.createDirectories(dir);ImageIO.write(image,"png",dir.resolve(name+".png").toFile());
        }catch(Exception e){throw new RuntimeException(e);}
    }
}
