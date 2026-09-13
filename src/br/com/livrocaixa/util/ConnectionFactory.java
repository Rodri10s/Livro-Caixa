package br.com.livrocaixa.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionFactory {

    // Instância única da conexão (Singleton)
    private static Connection conexao;

    // Construtor privado para impedir novas instâncias da classe
    private ConnectionFactory() {
    }

    // Método auxiliar para carregar o arquivo de configuração
    private static Properties carregarPropriedades() {
        Properties propriedades = new Properties();
        try (FileInputStream arquivo = new FileInputStream("banco.properties")) {
            propriedades.load(arquivo);
        } catch (IOException e) {
            throw new RuntimeException("Erro: Arquivo banco.properties não encontrado ou ilegível na raiz do projeto.",
                    e);
        }
        return propriedades;
    }

    // Método público que retorna a conexão ativa
    public static Connection getConexao() {
        try {
            // Verifica se a conexão está nula ou foi fechada antes de criar uma nova
            if (conexao == null || conexao.isClosed()) {

                Properties props = carregarPropriedades();

                String ip = props.getProperty("db.ip");
                String porta = props.getProperty("db.porta");
                String nomeBanco = props.getProperty("db.nome");
                String usuario = props.getProperty("db.usuario");
                String senha = props.getProperty("db.senha");

                // Monta a URL do JDBC dinamicamente
                String url = "jdbc:mysql://" + ip + ":" + porta + "/" + nomeBanco
                        + "?useTimezone=true&serverTimezone=UTC";

                // Registra o driver do MySQL e abre a conexão
                Class.forName("com.mysql.cj.jdbc.Driver");
                conexao = DriverManager.getConnection(url, usuario, senha);

                System.out.println("Conexão estabelecida com sucesso com o servidor: " + ip);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver do MySQL não encontrado. Verifique a pasta lib.", e);
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao conectar no banco de dados. Verifique as credenciais e se o MySQL está rodando.", e);
        }

        return conexao;
    }
}