package br.com.ale.service;

import static org.junit.jupiter.api.Assertions.*;

import br.com.ale.domain.account.Account;
import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.client.Provider;
import br.com.ale.dto.CreateAccountRequest;
import br.com.ale.dto.CreateClientRequest;
import br.com.ale.dto.UpdateAccountRequest;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.crypto.InMemoryPrivateKeyStorage;
import br.com.ale.service.crypto.SpyPrivateKeyStorage;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccountServiceTest {

  private static final String VALID_NAME = "John Doe";
  private static final String VALID_DOCUMENT = "123456789";

  private static final String ACCOUNT_NUMBER = "999-999-999";
  private static final String UPDATED_ACCOUNT_NUMBER = "777-777-777";
  private static final AccountType ACCOUNT_TYPE = AccountType.DEFAULT;
  private static final AccountType UPDATED_ACCOUNT_TYPE = AccountType.DEV;
  private static final AccountStatus STATUS = AccountStatus.ACTIVE;
  private static final AccountStatus UPDATED_STATUS = AccountStatus.INACTIVE;

  private TestConnectionProvider provider;
  private ClientService clientService;
  private AccountService accountService;

  @BeforeEach
  void setup() {
    provider = new TestConnectionProvider();

    clientService = new ClientService(provider);
    accountService = new AccountService(provider, new InMemoryPrivateKeyStorage());

    cleanDatabase();
  }

  private void cleanDatabase() {
    try (var conn = provider.getConnection();
        var stmt = conn.createStatement()) {

      stmt.execute("DELETE FROM artifact_price_history");
      stmt.execute("DELETE FROM artifact_transfer");
      stmt.execute("DELETE FROM artifact_listing");
      stmt.execute("DELETE FROM artifact_unit");
      stmt.execute("DELETE FROM artifact");
      stmt.execute("DELETE FROM transactions");
      stmt.execute("DELETE FROM account");
      stmt.execute("DELETE FROM credential");
      stmt.execute("DELETE FROM client");

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void shouldCreateAnAccount() {
    Account account = accountService.createAccount(validAccount());

    assertEquals(ACCOUNT_NUMBER, account.getAccountNumber());
    assertEquals(ACCOUNT_TYPE, account.getAccountType());
    assertEquals(STATUS, account.getStatus());
  }

  @Test
  void shouldSelectAccountByNumber() {
    accountService.createAccount(validAccount());

    Account account = accountService.getAccountByNumber(ACCOUNT_NUMBER);

    assertEquals(ACCOUNT_NUMBER, account.getAccountNumber());
    assertEquals(ACCOUNT_TYPE, account.getAccountType());
    assertEquals(STATUS, account.getStatus());
  }

  @Test
  void shouldUpdateAccount() {
    Account account = accountService.createAccount(validAccount());

    accountService.updateAccount(validUpdate(account));

    Account updatedAccount = accountService.getAccountByNumber(UPDATED_ACCOUNT_NUMBER);

    assertEquals(UPDATED_ACCOUNT_NUMBER, updatedAccount.getAccountNumber());
    assertEquals(UPDATED_ACCOUNT_TYPE, updatedAccount.getAccountType());
    assertEquals(UPDATED_STATUS, updatedAccount.getStatus());
  }

  @Test
  void shouldCreateAccountAndReturnId() {

    clientService.createClient(
        new CreateClientRequest(
            VALID_NAME, VALID_DOCUMENT, "pass", Provider.LOCAL, null, false, null));

    Client client = clientService.getClientByEmail(VALID_DOCUMENT);

    CreateAccountRequest request =
        new CreateAccountRequest(client.getId(), ACCOUNT_NUMBER, ACCOUNT_TYPE, STATUS);

    Account account = accountService.createAccount(request);
    assertNotNull(account);
    assertTrue(account.getId() > 0);
    assertEquals(client.getId(), account.getClientId());
    assertEquals(ACCOUNT_NUMBER, account.getAccountNumber());
    assertEquals(ACCOUNT_TYPE, account.getAccountType());
    assertEquals(STATUS, account.getStatus());
  }

  @Test
  void shouldGenerateAndStorePrivateKey() {

    SpyPrivateKeyStorage spyStorage = new SpyPrivateKeyStorage();

    clientService = new ClientService(provider);
    accountService = new AccountService(provider, spyStorage);

    cleanDatabase();

    clientService.createClient(
        new CreateClientRequest(
            VALID_NAME, VALID_DOCUMENT, "pass", Provider.LOCAL, null, false, null));

    Client client = clientService.getClientByEmail(VALID_DOCUMENT);

    CreateAccountRequest request =
        new CreateAccountRequest(client.getId(), ACCOUNT_NUMBER, ACCOUNT_TYPE, STATUS);

    Account account = accountService.createAccount(request);

    assertTrue(spyStorage.wasSaveCalled());
    assertEquals(account.getId(), spyStorage.getSavedAccountId());
    assertNotNull(spyStorage.getSavedPrivateKey());
    assertTrue(spyStorage.getSavedPrivateKey().length > 0);
  }

  @Test
  void shouldPersistPublicKeyInDatabase() {

    clientService.createClient(
        new CreateClientRequest(
            VALID_NAME, VALID_DOCUMENT, "pass", Provider.LOCAL, null, false, null));

    Client client = clientService.getClientByEmail(VALID_DOCUMENT);

    accountService.createAccount(
        new CreateAccountRequest(client.getId(), ACCOUNT_NUMBER, ACCOUNT_TYPE, STATUS));

    Account persisted = accountService.getAccountByNumber(ACCOUNT_NUMBER);

    assertNotNull(persisted.getPublicKey());
    assertTrue(
        persisted.getPublicKey().startsWith("-----BEGIN PUBLIC KEY-----")
            || persisted.getPublicKey().length() > 100);
  }

  @Test
  void shouldTransferAmountBetweenAccounts() {

    Account from = accountService.createAccount(validAccount());
    Account to =
        accountService.createAccount(
            new CreateAccountRequest(from.getClientId(), "888-888-888", ACCOUNT_TYPE, STATUS));

    accountService.credit(from.getAccountNumber(), new BigDecimal("100.00"));

    accountService.transfer(from.getId(), to.getId(), new BigDecimal("40.00"));

    Account updatedFrom = accountService.getAccountByNumber(from.getAccountNumber());
    Account updatedTo = accountService.getAccountByNumber(to.getAccountNumber());

    assertEquals(0, updatedFrom.getBalance().compareTo(new BigDecimal("60.00")));
    assertEquals(0, updatedTo.getBalance().compareTo(new BigDecimal("40.00")));
  }

  @Test
  void shouldNotAllowTransferToSameAccount() {

    Account account = accountService.createAccount(validAccount());

    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () ->
                accountService.transfer(account.getId(), account.getId(), new BigDecimal("10.00")));

    assertTrue(
        exception.getMessage().contains("Not allowed transfer to the same account"),
        exception.getMessage());
  }

  @Test
  void shouldFailTransferWhenInsufficientBalance() {

    Account from = accountService.createAccount(validAccount());
    Account to =
        accountService.createAccount(
            new CreateAccountRequest(from.getClientId(), "888-888-888", ACCOUNT_TYPE, STATUS));

    RuntimeException ex =
        assertThrows(
            RuntimeException.class,
            () -> accountService.transfer(from.getId(), to.getId(), new BigDecimal("50.00")));

    assertNotNull(ex.getCause());
    assertTrue(
        ex.getCause().getMessage().contains("Insufficient balance"),
        "Expected insufficient balance exception");
  }

  @Test
  void shouldCreditAccount() {

    Account account = accountService.createAccount(validAccount());

    accountService.credit(account.getAccountNumber(), new BigDecimal("100.00"));

    Account updated = accountService.getAccountByNumber(account.getAccountNumber());

    assertEquals(0, updated.getBalance().compareTo(new BigDecimal("100.00")));
  }

  @Test
  void shouldDebitAccount() {

    Account account = accountService.createAccount(validAccount());
    accountService.credit(account.getAccountNumber(), new BigDecimal("100.00"));

    accountService.debit(account.getAccountNumber(), new BigDecimal("30.00"));

    Account updated = accountService.getAccountByNumber(account.getAccountNumber());

    assertEquals(0, updated.getBalance().compareTo(new BigDecimal("70.00")));
  }

  @Test
  void shouldFailDebitWhenInsufficientBalance() {

    Account account = accountService.createAccount(validAccount());

    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> accountService.debit(account.getAccountNumber(), new BigDecimal("10.00")));

    assertNotNull(exception.getCause());
    assertTrue(
        exception.getCause().getMessage().contains("Insufficient balance"),
        "Expected insufficient balance exception");
  }

  private CreateAccountRequest validAccount() {
    clientService.createClient(
        new CreateClientRequest(
            VALID_NAME, VALID_DOCUMENT, "pass", Provider.LOCAL, null, false, null));

    Client client = clientService.getClientByEmail(VALID_DOCUMENT);

    return new CreateAccountRequest(client.getId(), ACCOUNT_NUMBER, ACCOUNT_TYPE, STATUS);
  }

  private UpdateAccountRequest validUpdate(Account account) {
    return new UpdateAccountRequest(
        account.getId(), UPDATED_ACCOUNT_NUMBER, UPDATED_ACCOUNT_TYPE, UPDATED_STATUS);
  }
}
