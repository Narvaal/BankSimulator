package br.com.ale.dao;

import static org.junit.jupiter.api.Assertions.*;

import br.com.ale.domain.account.Account;
import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.dto.CreateBalanceOperationRequest;
import br.com.ale.dto.UpdateAccountRequest;
import br.com.ale.support.DbTestSupport;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class AccountDAOTest extends DbTestSupport {

  private static final String NUMBER = "111-222-333";

  private long seedAccount() {
    long clientId = insertClient("John Doe", "john@test.com");
    return insertAccount(clientId, NUMBER);
  }

  @Test
  void shouldInsertAndSelectById() throws Exception {
    long accountId = seedAccount();

    try (Connection conn = open()) {
      Account account = accountDAO.selectById(conn, accountId).orElseThrow();
      assertEquals(NUMBER, account.getAccountNumber());
      assertEquals(AccountType.DEFAULT, account.getAccountType());
      assertEquals(0, account.getBalance().compareTo(BigDecimal.ZERO));
    }
  }

  @Test
  void shouldReturnEmptyWhenAccountDoesNotExist() throws Exception {
    try (Connection conn = open()) {
      assertTrue(accountDAO.selectById(conn, 9999L).isEmpty());
      assertTrue(accountDAO.selectByNumber(conn, "000-000-000").isEmpty());
      assertTrue(accountDAO.selectByClientId(conn, 9999L).isEmpty());
      assertTrue(accountDAO.selectDetailsById(conn, 9999L).isEmpty());
      assertTrue(accountDAO.selectPublicProfileById(conn, 9999L).isEmpty());
    }
  }

  @Test
  void shouldSelectByClientIdAndByNumber() throws Exception {
    long clientId = insertClient("Jane", "jane@test.com");
    long accountId = insertAccount(clientId, NUMBER);

    try (Connection conn = open()) {
      assertEquals(accountId, accountDAO.selectByClientId(conn, clientId).orElseThrow().getId());
      assertEquals(accountId, accountDAO.selectByNumber(conn, NUMBER).orElseThrow().getId());
    }
  }

  @Test
  void shouldUpdateAccount() throws Exception {
    long accountId = seedAccount();

    try (Connection conn = open()) {
      int rows =
          accountDAO.update(
              conn,
              new UpdateAccountRequest(
                  accountId, "999-888-777", AccountType.DEV, AccountStatus.INACTIVE));
      assertEquals(1, rows);

      Account updated = accountDAO.selectById(conn, accountId).orElseThrow();
      assertEquals("999-888-777", updated.getAccountNumber());
      assertEquals(AccountType.DEV, updated.getAccountType());
      assertEquals(AccountStatus.INACTIVE, updated.getStatus());
    }
  }

  @Test
  void shouldSelectDetailsWithClientData() throws Exception {
    long clientId = insertClient("John Doe", "john@test.com");
    long accountId = insertAccount(clientId, NUMBER);

    try (Connection conn = open()) {
      var details = accountDAO.selectDetailsById(conn, accountId).orElseThrow();
      assertEquals("John Doe", details.name());
      assertEquals(NUMBER, details.accountNumber());
      assertFalse(details.emailVerified());
    }
  }

  @Test
  void creditShouldIncreaseBalance() throws Exception {
    seedAccount();

    try (Connection conn = open()) {
      int rows =
          accountDAO.credit(
              conn, new CreateBalanceOperationRequest(NUMBER, new BigDecimal("50.00")));
      assertEquals(1, rows);

      Account account = accountDAO.selectByNumber(conn, NUMBER).orElseThrow();
      assertEquals(0, account.getBalance().compareTo(new BigDecimal("50.00")));
    }
  }

  @Test
  void debitShouldFailWhenInsufficientBalance() throws Exception {
    seedAccount();

    try (Connection conn = open()) {
      int rows =
          accountDAO.debit(
              conn, new CreateBalanceOperationRequest(NUMBER, new BigDecimal("10.00")));
      assertEquals(0, rows);
    }
  }

  @Test
  void debitShouldSucceedWhenBalanceIsEnough() throws Exception {
    seedAccount();

    try (Connection conn = open()) {
      accountDAO.credit(conn, new CreateBalanceOperationRequest(NUMBER, new BigDecimal("100.00")));
      int rows =
          accountDAO.debit(
              conn, new CreateBalanceOperationRequest(NUMBER, new BigDecimal("40.00")));
      assertEquals(1, rows);

      Account account = accountDAO.selectByNumber(conn, NUMBER).orElseThrow();
      assertEquals(0, account.getBalance().compareTo(new BigDecimal("60.00")));
    }
  }

  @Test
  void tryClaimShouldSetCooldownWhenAvailable() throws Exception {
    seedAccount();

    try (Connection conn = open()) {
      var next = accountDAO.tryClaimArtifactUnit(conn, NUMBER);
      assertTrue(next.isPresent());
      assertTrue(next.get().isAfter(Instant.now()));
    }
  }

  @Test
  void tryClaimShouldReturnEmptyDuringCooldown() throws Exception {
    seedAccount();

    try (Connection conn = open()) {
      assertTrue(accountDAO.tryClaimArtifactUnit(conn, NUMBER).isPresent());
      assertTrue(accountDAO.tryClaimArtifactUnit(conn, NUMBER).isEmpty());
    }
  }

  @Test
  void tryClaimShouldReturnEmptyForUnknownAccount() throws Exception {
    try (Connection conn = open()) {
      assertTrue(accountDAO.tryClaimArtifactUnit(conn, "000-000-000").isEmpty());
    }
  }

  @Test
  void selectNextClaimShouldReturnInstantOrNull() throws Exception {
    seedAccount();

    try (Connection conn = open()) {
      assertNotNull(accountDAO.selectNextClaimById(conn, NUMBER));
      assertNull(accountDAO.selectNextClaimById(conn, "000-000-000"));
    }
  }

  @Test
  void shouldSelectPublicProfile() throws Exception {
    long clientId = insertClient("John Doe", "john@test.com");
    long accountId = insertAccount(clientId, NUMBER);

    try (Connection conn = open()) {
      var profile = accountDAO.selectPublicProfileById(conn, accountId).orElseThrow();
      assertEquals("John Doe", profile.name());
      assertEquals(NUMBER, profile.accountNumber());
    }
  }

  @Test
  void searchByNameShouldMatchCaseInsensitiveAndPaginate() throws Exception {
    for (int i = 0; i < 3; i++) {
      long clientId = insertClient("Search Target " + i, "target" + i + "@test.com");
      insertAccount(clientId, "100-000-00" + i);
    }
    long otherId = insertClient("Unrelated", "other@test.com");
    insertAccount(otherId, "200-000-000");

    try (Connection conn = open()) {
      var page0 = accountDAO.searchByName(conn, "search target", 0, 2);
      assertEquals(2, page0.size());

      var page1 = accountDAO.searchByName(conn, "SEARCH", 1, 2);
      assertEquals(1, page1.size());

      assertEquals(3, accountDAO.countByName(conn, "search target"));
      assertEquals(0, accountDAO.countByName(conn, "nobody"));
    }
  }

  @Test
  void searchByNameShouldExcludeInactiveAccounts() throws Exception {
    long clientId = insertClient("Search Inactive", "inactive@test.com");
    long accountId = insertAccount(clientId, NUMBER);

    try (Connection conn = open()) {
      accountDAO.update(
          conn,
          new UpdateAccountRequest(accountId, NUMBER, AccountType.DEFAULT, AccountStatus.INACTIVE));

      assertEquals(0, accountDAO.searchByName(conn, "Search Inactive", 0, 10).size());
      assertEquals(0, accountDAO.countByName(conn, "Search Inactive"));
    }
  }
}
