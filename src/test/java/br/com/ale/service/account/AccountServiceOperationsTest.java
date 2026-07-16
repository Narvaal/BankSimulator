package br.com.ale.service.account;

import static org.junit.jupiter.api.Assertions.*;

import br.com.ale.dao.TransactionDAO;
import br.com.ale.domain.account.Account;
import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.domain.client.Provider;
import br.com.ale.domain.exception.AccountNotFoundException;
import br.com.ale.domain.transaction.TransactionStatus;
import br.com.ale.dto.CreateAccountRequest;
import br.com.ale.dto.CreateClientRequest;
import br.com.ale.service.ClientService;
import br.com.ale.service.crypto.InMemoryPrivateKeyStorage;
import br.com.ale.support.DbTestSupport;
import java.math.BigDecimal;
import java.sql.Connection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccountServiceOperationsTest extends DbTestSupport {

  private AccountService accountService;
  private ClientService clientService;

  @BeforeEach
  void setupServices() {
    clientService = new ClientService(provider);
    accountService = new AccountService(provider, new InMemoryPrivateKeyStorage());
  }

  private Account createAccount(String email, String number) {
    var client =
        clientService.createClient(
            new CreateClientRequest(
                "User " + number, email, "pass", Provider.LOCAL, null, false, null));
    return accountService.createAccount(
        new CreateAccountRequest(
            client.getId(), number, AccountType.DEFAULT, AccountStatus.ACTIVE));
  }

  @Test
  void creditShouldDepositAndRecordCompleteTransaction() throws Exception {
    Account account = createAccount("a@test.com", "300-000-001");

    accountService.credit("300-000-001", new BigDecimal("80.00"));

    Account updated = accountService.getAccountByNumber("300-000-001");
    assertEquals(0, updated.getBalance().compareTo(new BigDecimal("80.00")));

    try (Connection conn = open()) {
      var txs = new TransactionDAO().selectToAccountId(conn, account.getId());
      assertEquals(1, txs.size());
      assertEquals(TransactionStatus.COMPLETE, txs.get(0).getStatus());
      assertNull(txs.get(0).getFromAccountId());
    }
  }

  @Test
  void creditShouldFailForUnknownAccount() {
    assertThrows(
        RuntimeException.class, () -> accountService.credit("000-000-000", BigDecimal.TEN));
  }

  @Test
  void debitShouldWithdrawWhenBalanceIsEnough() {
    createAccount("a@test.com", "300-000-001");
    accountService.credit("300-000-001", new BigDecimal("50.00"));

    accountService.debit("300-000-001", new BigDecimal("20.00"));

    assertEquals(
        0,
        accountService
            .getAccountByNumber("300-000-001")
            .getBalance()
            .compareTo(new BigDecimal("30.00")));
  }

  @Test
  void debitShouldFailOnInsufficientBalance() {
    Account account = createAccount("a@test.com", "300-000-001");

    RuntimeException ex =
        assertThrows(
            RuntimeException.class,
            () -> accountService.debit("300-000-001", new BigDecimal("20.00")));
    assertTrue(ex.getMessage().contains("debiting"));

    assertEquals(
        0, accountService.getAccountById(account.getId()).getBalance().compareTo(BigDecimal.ZERO));
  }

  @Test
  void transferShouldMoveBalanceAndCompleteTransaction() throws Exception {
    Account from = createAccount("from@test.com", "300-000-001");
    Account to = createAccount("to@test.com", "300-000-002");
    accountService.credit("300-000-001", new BigDecimal("100.00"));

    accountService.transfer(from.getId(), to.getId(), new BigDecimal("60.00"));

    assertEquals(
        0,
        accountService
            .getAccountByNumber("300-000-001")
            .getBalance()
            .compareTo(new BigDecimal("40.00")));
    assertEquals(
        0,
        accountService
            .getAccountByNumber("300-000-002")
            .getBalance()
            .compareTo(new BigDecimal("60.00")));

    try (Connection conn = open()) {
      var txs = new TransactionDAO().selectFromAccountId(conn, from.getId());
      assertEquals(1, txs.size());
      assertEquals(TransactionStatus.COMPLETE, txs.get(0).getStatus());
      assertNotNull(txs.get(0).getSignature());
    }
  }

  @Test
  void transferShouldRejectSameAccount() {
    Account from = createAccount("from@test.com", "300-000-001");
    assertThrows(
        RuntimeException.class,
        () -> accountService.transfer(from.getId(), from.getId(), BigDecimal.TEN));
  }

  @Test
  void transferShouldFailWhenAccountsDoNotExist() {
    Account from = createAccount("from@test.com", "300-000-001");

    assertThrows(
        RuntimeException.class, () -> accountService.transfer(9999L, from.getId(), BigDecimal.TEN));
    assertThrows(
        RuntimeException.class, () -> accountService.transfer(from.getId(), 9999L, BigDecimal.TEN));
  }

  @Test
  void transferShouldRollbackOnInsufficientBalance() {
    Account from = createAccount("from@test.com", "300-000-001");
    Account to = createAccount("to@test.com", "300-000-002");

    assertThrows(
        RuntimeException.class,
        () -> accountService.transfer(from.getId(), to.getId(), new BigDecimal("5.00")));

    assertEquals(
        0, accountService.getAccountById(to.getId()).getBalance().compareTo(BigDecimal.ZERO));
  }

  @Test
  void shouldFetchAccountVariants() {
    Account account = createAccount("a@test.com", "300-000-001");

    assertEquals(account.getId(), accountService.getAccountById(account.getId()).getId());
    assertEquals(
        account.getId(),
        accountService.getAccountByClientId(account.getClientId()).orElseThrow().getId());

    var details = accountService.getAccountDetailsById(account.getId());
    assertEquals("300-000-001", details.accountNumber());

    assertThrows(RuntimeException.class, () -> accountService.getAccountById(9999L));
    assertThrows(RuntimeException.class, () -> accountService.getAccountByNumber("000"));
    assertThrows(RuntimeException.class, () -> accountService.getAccountDetailsById(9999L));
    assertTrue(accountService.getAccountByClientId(9999L).isEmpty());
  }

  @Test
  void publicProfileShouldExistOrThrowTyped() {
    Account account = createAccount("a@test.com", "300-000-001");

    assertEquals("300-000-001", accountService.getPublicProfile(account.getId()).accountNumber());
    assertThrows(AccountNotFoundException.class, () -> accountService.getPublicProfile(9999L));
  }

  @Test
  void searchByNameShouldPaginate() {
    createAccount("a@test.com", "300-000-001");
    createAccount("b@test.com", "300-000-002");

    var page = accountService.searchByName("User", 0, 1);
    assertEquals(1, page.items().size());
    assertEquals(2, page.totalItems());
    assertEquals(2, page.totalPages());
  }

  @Test
  void claimCooldownShouldBeEnforcedByService() {
    createAccount("a@test.com", "300-000-001");

    assertTrue(accountService.tryClaimArtifactUnit("300-000-001").isPresent());
    assertTrue(accountService.tryClaimArtifactUnit("300-000-001").isEmpty());
    assertNotNull(accountService.selectNextClaimById("300-000-001"));
  }
}
