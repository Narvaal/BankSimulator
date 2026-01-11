package br.com.ale.infrastructure.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DefaultConnectionProvider implements ConnectionProvider {

    private static final String URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "ale";
    private static final String PASSWORD = "root";

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
