package livrocaixa.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

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
}
