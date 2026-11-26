package br.com.livrocaixa;

import br.com.livrocaixa.view.CoordenadorTelas;
import br.com.livrocaixa.view.dados.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Confirma que o front original encaminha suas operações aos Controllers reais. */
class IntegracaoFrontOriginalTest {
    @Test
    void deveConectarFluxoPrincipalSemTrocarAsViews() {
        CoordenadorTelas controller = new CoordenadorTelas();
        controller.autenticar("Ana Souza", "123456");
        LivroCaixaModel view = controller.getModel();

        assertEquals(2, view.clients.size());
        Client criado = view.saveClient(null, "Cliente Integração", "123.456.789-09");
        assertNotNull(criado.id);

        Appointment consulta = view.saveAppointment(null, criado.name, view.appointmentTypes.get(0),
                LocalDate.now(), "Ana Souza", new BigDecimal("300.00"), "Integração");
        List<Payment> pagamentos = new ArrayList<>(List.of(new Payment(
                view.nextPayment, consulta.id, criado.name, "À vista", LocalDate.now(),
                new BigDecimal("300.00"), "PIX", BigDecimal.ZERO, false)));
        List<Installment> parcelas = new ArrayList<>();
        view.recordInvoice(consulta, pagamentos, parcelas);

        assertTrue(consulta.billed);
        assertEquals(1, parcelas.size());
        assertTrue(pagamentos.isEmpty(), "Pagamento imediato vira parcela à vista no domínio real.");
        Payment baixa = view.registerInstallmentPayment(
                parcelas.get(0), LocalDate.now(), new BigDecimal("300.00"), "PIX", BigDecimal.ZERO);
        assertNotNull(baixa.id);
        assertEquals(0, parcelas.get(0).remaining().compareTo(BigDecimal.ZERO));

        Expense saida = view.saveExpense(null, view.expenseTypes.get(0), view.expenseTypes.get(0),
                new BigDecimal("50.00"), LocalDate.now(), "Ana Souza", "Integração");
        assertNotNull(saida.id);
        assertEquals(0, view.balance().compareTo(new BigDecimal("250.00")));
        controller.encerrarSessao();
    }
}
