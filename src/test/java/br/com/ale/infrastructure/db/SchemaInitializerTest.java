package br.com.ale.infrastructure.db;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.Test;

class SchemaInitializerTest {

  private ConnectionProvider freshDb(String name) {
    return () ->
        DriverManager.getConnection(
            "jdbc:h2:mem:" + name + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "");
  }

  private boolean clientTableExists(ConnectionProvider provider) throws Exception {
    try (Connection conn = provider.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs =
            stmt.executeQuery(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'client'"
                    + " OR table_name = 'CLIENT'")) {
      rs.next();
      return rs.getInt(1) > 0;
    }
  }

  @Test
  void shouldCreateSchemaOnEmptyDatabaseAndSkipWhenPresent() throws Exception {
    ConnectionProvider provider = freshDb("schemainit_" + System.nanoTime());
    SchemaInitializer initializer = new SchemaInitializer(provider, true);

    initializer.run(null);
    assertTrue(clientTableExists(provider));

    // segunda execução: tabela existe, deve retornar sem erro
    initializer.run(null);
    assertTrue(clientTableExists(provider));
  }

  @Test
  void shouldDoNothingWhenAutoCreateDisabled() throws Exception {
    ConnectionProvider provider = freshDb("schemainit_off_" + System.nanoTime());

    new SchemaInitializer(provider, false).run(null);

    assertFalse(clientTableExists(provider));
  }

  @Test
  void shouldWrapConnectionFailures() {
    ConnectionProvider broken =
        () -> {
          throw new RuntimeException("db down");
        };

    assertThrows(RuntimeException.class, () -> new SchemaInitializer(broken, true).run(null));
  }
}
