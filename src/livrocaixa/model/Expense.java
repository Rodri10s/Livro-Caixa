package livrocaixa.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Saída financeira demonstrativa exibida no painel e nos relatórios. */
public final class Expense implements Serializable {
    public int id;
    public String type;
    public String description;
    public String employee;
    public String notes;
    public BigDecimal value;
    public LocalDate date;

    public Expense(int id, String type, String description, BigDecimal value, LocalDate date,
                   String employee, String notes) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.value = value;
        this.date = date;
        this.employee = employee;
        this.notes = notes;
    }
}
