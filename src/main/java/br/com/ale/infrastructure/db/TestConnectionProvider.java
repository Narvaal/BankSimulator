package br.com.ale.infrastructure.db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class TestConnectionProvider implements ConnectionProvider {

    private static final String URL =
            "jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";

    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private static boolean schemaLoaded = false;

    @Override
    public Connection getConnection() throws Exception {

        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);

        if (!schemaLoaded) {
            loadSchema(conn);
            schemaLoaded = true;
        }

        return conn;
    }

    private void loadSchema(Connection conn) throws Exception {

        try (Statement stmt = conn.createStatement()) {

            String sql;

            try (InputStream schema =
                         getClass()
                                 .getClassLoader()
                                 .getResourceAsStream("schema.sql")) {

                if (schema == null) {
                    throw new RuntimeException("schema.sql not found in test resources");
                }

                sql = new String(schema.readAllBytes());
            }

            stmt.execute(sql);
        }
    }
}
