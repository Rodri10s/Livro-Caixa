package livrocaixa.model;

import java.io.Serializable;

/** Dados de um cliente. A classe não depende de nenhum componente Swing. */
public final class Client implements Serializable {
    public int id;
    public String name;
    public String cpf;
    public String phone;
    public String email;
    public String address;

    public Client(int id, String name, String cpf, String phone, String email, String address) {
        this.id = id;
        this.name = name;
        this.cpf = cpf;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }
}
