package br.com.livrocaixa.view.dados;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/** Parcela demonstrativa vinculada a uma consulta faturada. */
public final class Installment implements Serializable {
    public int id;
    public int appointmentId;
    public String client;
    public String label;
    public LocalDate due;
    public BigDecimal value;
    public BigDecimal paid;
    public boolean cancelled;

    public Installment(int id, int appointmentId, String client, String label, LocalDate due,
                       BigDecimal value, BigDecimal paid, boolean cancelled) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.client = client;
        this.label = label;
        this.due = due;
        this.value = value;
        this.paid = paid;
        this.cancelled = cancelled;
    }

    /** Valor que ainda pode ser recebido, sem permitir saldo negativo na interface. */
    public BigDecimal remaining() {
        return value.subtract(paid).max(BigDecimal.ZERO);
    }

    public boolean isPaid() {
        return !cancelled && paid.compareTo(value) >= 0;
    }

    /** Situação calculada na data de referência informada pela tela ou relatório. */
    public String status(LocalDate referenceDate) {
        Objects.requireNonNull(referenceDate, "referenceDate");
        if (cancelled) return "Cancelada";
        if (isPaid()) return "Pago";
        return due.isBefore(referenceDate) ? "Vencida" : "Pendente";
    }
}
