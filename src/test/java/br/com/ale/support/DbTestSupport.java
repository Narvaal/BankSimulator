package br.com.ale.support;

import br.com.ale.dao.AccountDAO;
import br.com.ale.dao.ClientDAO;
import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.domain.client.Provider;
import br.com.ale.dto.CreateAccountRequest;
import br.com.ale.dto.CreateClientRequest;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import java.sql.Connection;
import java.sql.Statement;
import org.junit.jupiter.api.BeforeEach;

/** Base para testes de integração H2: schema real + helpers de seed. */
public abstract class DbTestSupport {

  protected TestConnectionProvider provider;
  protected final ClientDAO clientDAO = new ClientDAO();
  protected final AccountDAO accountDAO = new AccountDAO();

  @BeforeEach
  void initDatabase() {
    provider = new TestConnectionProvider();
    cleanDatabase();
  }

  protected void cleanDatabase() {
    try (Connection conn = provider.getConnection();
        Statement stmt = conn.createStatement()) {

      stmt.execute("DELETE FROM artifact_price_history");
      stmt.execute("DELETE FROM artifact_transfer");
      stmt.execute("DELETE FROM artifact_listing");
      stmt.execute("DELETE FROM artifact_unit");
      stmt.execute("DELETE FROM artifact_bundle_item");
      stmt.execute("DELETE FROM artifact_bundle");
      stmt.execute("DELETE FROM artifact");
      stmt.execute("DELETE FROM transactions");
      stmt.execute("DELETE FROM email_verification");
      stmt.execute("DELETE FROM account");
      stmt.execute("DELETE FROM credential");
      stmt.execute("DELETE FROM client");

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected Connection open() {
    try {
      return provider.getConnection();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected long insertClient(String name, String email) {
    try (Connection conn = provider.getConnection()) {
      return clientDAO.insert(
          conn, new CreateClientRequest(name, email, "pass", Provider.LOCAL, null, false, null));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected long insertAccount(long clientId, String accountNumber) {
    try (Connection conn = provider.getConnection()) {
      return accountDAO.insert(
          conn,
          new CreateAccountRequest(
              clientId, accountNumber, AccountType.DEFAULT, AccountStatus.ACTIVE),
          "test-public-key");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
