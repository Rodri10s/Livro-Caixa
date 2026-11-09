package livrocaixa.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Pagamento demonstrativo realizado sobre uma parcela ou cobrança à vista. */
public final class Payment implements Serializable {
    public int id;
    public int appointmentId;
    public String client;
    public String label;
    public String method;
    public LocalDate date;
    public BigDecimal value;
    public BigDecimal discount;
    public boolean reversed;

    public Payment(int id, int appointmentId, String client, String label, LocalDate date,
                   BigDecimal value, String method, BigDecimal discount, boolean reversed) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.client = client;
        this.label = label;
        this.date = date;
        this.value = value;
        this.method = method;
        this.discount = discount;
        this.reversed = reversed;
    }
}
