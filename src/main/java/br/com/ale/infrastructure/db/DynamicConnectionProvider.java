package br.com.ale.infrastructure.db;

import br.com.ale.infrastructure.db.secrets.SecretsService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DynamicConnectionProvider implements ConnectionProvider {

    private final String url;
    private final String user;
    private final SecretsService secretsService;

    public DynamicConnectionProvider(
            String url,
            String user,
            SecretsService secretsService
    ) {
        this.url = url;
        this.user = user;
        this.secretsService = secretsService;
    }

    @Override
    public Connection getConnection() throws SQLException {
        String password = secretsService.getDbPassword();
        return DriverManager.getConnection(url, user, password);
    }
}