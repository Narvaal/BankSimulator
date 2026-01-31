package br.com.ale.infrastructure.db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

public class TestConnectionProvider implements ConnectionProvider {

    private static final String URL_KEY = "db.test.url";
    private static final String USER_KEY = "db.test.user";
    private static final String PASSWORD_KEY = "db.test.password";

    private static final String DEFAULT_URL =
            "jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
    private static final String DEFAULT_USER = "sa";
    private static final String DEFAULT_PASSWORD = "";

    private final String url;
    private final String user;
    private final String password;

    private static boolean schemaLoaded = false;
    private static boolean shutdownHookRegistered = false;

    public TestConnectionProvider() {
        Properties props = loadProperties();
        this.url = props.getProperty(URL_KEY, DEFAULT_URL);
        this.user = props.getProperty(USER_KEY, DEFAULT_USER);
        this.password = props.getProperty(PASSWORD_KEY, DEFAULT_PASSWORD);
        registerShutdownHook();
    }

    @Override
    public Connection getConnection() throws Exception {

        Connection conn = DriverManager.getConnection(url, user, password);

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

    private void registerShutdownHook() {
        if (shutdownHookRegistered) {
            return;
        }
        shutdownHookRegistered = true;
        Runtime.getRuntime().addShutdownHook(new Thread(this::dropSchema));
    }

    private void dropSchema() {
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP ALL OBJECTS");
        } catch (Exception ignored) {
            // Best-effort cleanup for test database.
        }
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input =
                     TestConnectionProvider.class
                             .getClassLoader()
                             .getResourceAsStream("application.properties")) {
            if (input != null) {
                props.load(input);
            }
        } catch (Exception ignored) {
            // Defaults apply when properties are not available.
        }
        return props;
    }
}
