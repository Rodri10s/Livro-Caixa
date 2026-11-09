package livrocaixa.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Dados de uma consulta utilizada pelo fluxo de atendimento e faturamento. */
public final class Appointment implements Serializable {
    public int id;
    public String client;
    public String type;
    public String employee;
    public String notes;
    public LocalDate date;
    public BigDecimal value;
    public boolean billed;

    public Appointment(int id, String client, String type, LocalDate date, String employee,
                       BigDecimal value, String notes, boolean billed) {
        this.id = id;
        this.client = client;
        this.type = type;
        this.date = date;
        this.employee = employee;
        this.value = value;
        this.notes = notes;
        this.billed = billed;
    }
}
