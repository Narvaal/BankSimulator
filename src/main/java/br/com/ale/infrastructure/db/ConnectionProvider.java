package br.com.ale.infrastructure.db;

import java.sql.Connection;

public interface ConnectionProvider {
    Connection getConnection() throws Exception;
}
