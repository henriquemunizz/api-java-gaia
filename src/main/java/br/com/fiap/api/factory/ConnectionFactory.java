package br.com.fiap.api.factory;

import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

// Centraliza a obtenção de conexões, como na Apostila13.
// O DataSource da Apostila16 mantém as credenciais nas variáveis de ambiente.
@Component
public class ConnectionFactory {
    private final DataSource dataSource;

    public ConnectionFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
