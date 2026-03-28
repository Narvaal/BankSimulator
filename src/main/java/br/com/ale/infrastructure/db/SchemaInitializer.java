package br.com.ale.infrastructure.db;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

public class SchemaInitializer implements ApplicationRunner {

    private final ConnectionProvider connectionProvider;
    private final boolean autoCreate;

    public SchemaInitializer(
            ConnectionProvider connectionProvider,
            boolean autoCreate
    ) {
        this.connectionProvider = connectionProvider;
        this.autoCreate = autoCreate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!autoCreate) {
            return;
        }

        try (Connection conn = connectionProvider.getConnection()) {
            if (tableExists(conn, "client")) {
                return;
            }
            executeSchema(conn);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }

    private boolean tableExists(Connection conn, String tableName) throws Exception {
        DatabaseMetaData metaData = conn.getMetaData();
        String productName = metaData.getDatabaseProductName();
        boolean isH2 = productName != null && productName.toLowerCase().contains("h2");
        String schema = isH2 ? "PUBLIC" : "public";
        String normalizedTable = isH2 ? tableName.toUpperCase() : tableName;

        try (ResultSet rs = metaData.getTables(
                null,
                schema,
                normalizedTable,
                new String[]{"TABLE"}
        )) {
            return rs.next();
        }
    }

    private void executeSchema(Connection conn) throws Exception {
        String schemaSql = loadSchemaSql();
        String[] statements = schemaSql.split(";");
        try (Statement stmt = conn.createStatement()) {
            for (String statement : statements) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
        }
    }

    private String loadSchemaSql() throws Exception {
        try (InputStream schema =
                     getClass().getClassLoader().getResourceAsStream("schema.sql")) {
            if (schema == null) {
                throw new RuntimeException("schema.sql not found on classpath");
            }
            return new String(schema.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
