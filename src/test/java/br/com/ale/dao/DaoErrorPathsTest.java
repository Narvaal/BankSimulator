package br.com.ale.dao;

import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.ale.dao.artifact.ArtifactBundleDAO;
import br.com.ale.dao.artifact.ArtifactBundleItemDAO;
import br.com.ale.dao.artifact.ArtifactDAO;
import br.com.ale.dao.artifact.ArtifactListingDAO;
import br.com.ale.dao.artifact.ArtifactPriceHistoryDAO;
import br.com.ale.dao.artifact.ArtifactTransferDAO;
import br.com.ale.dao.artifact.ArtifactUnitDAO;
import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.domain.artifact.ArtifactListingStatus;
import br.com.ale.domain.artifact.ReasonType;
import br.com.ale.domain.client.Provider;
import br.com.ale.domain.emailVerification.EmailVerificationType;
import br.com.ale.domain.transaction.TransactionStatus;
import br.com.ale.domain.transaction.TransactionType;
import br.com.ale.dto.*;
import br.com.ale.support.DbTestSupport;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Toda operação de DAO deve embrulhar SQLException em RuntimeException (conexão fechada). */
class DaoErrorPathsTest extends DbTestSupport {

  private Connection closed;

  @BeforeEach
  void closeConnection() throws Exception {
    closed = open();
    closed.close();
  }

  @Test
  void accountDaoShouldWrapSqlExceptions() {
    AccountDAO dao = new AccountDAO();
    var create = new CreateAccountRequest(1L, "1", AccountType.DEFAULT, AccountStatus.ACTIVE);
    var update = new UpdateAccountRequest(1L, "1", AccountType.DEFAULT, AccountStatus.ACTIVE);
    var balance = new CreateBalanceOperationRequest("1", BigDecimal.TEN);

    assertThrows(RuntimeException.class, () -> dao.insert(closed, create, "pk"));
    assertThrows(RuntimeException.class, () -> dao.update(closed, update));
    assertThrows(RuntimeException.class, () -> dao.selectById(closed, 1L));
    assertThrows(RuntimeException.class, () -> dao.selectByClientId(closed, 1L));
    assertThrows(RuntimeException.class, () -> dao.selectDetailsById(closed, 1L));
    assertThrows(RuntimeException.class, () -> dao.selectByNumber(closed, "1"));
    assertThrows(RuntimeException.class, () -> dao.debit(closed, balance));
    assertThrows(RuntimeException.class, () -> dao.credit(closed, balance));
    assertThrows(RuntimeException.class, () -> dao.tryClaimArtifactUnit(closed, "1"));
    assertThrows(RuntimeException.class, () -> dao.selectNextClaimById(closed, "1"));
    assertThrows(RuntimeException.class, () -> dao.selectPublicProfileById(closed, 1L));
    assertThrows(RuntimeException.class, () -> dao.searchByName(closed, "q", 0, 10));
    assertThrows(RuntimeException.class, () -> dao.countByName(closed, "q"));
  }

  @Test
  void clientDaoShouldWrapSqlExceptions() {
    ClientDAO dao = new ClientDAO();
    var create = new CreateClientRequest("n", "e", "p", Provider.LOCAL, null, false, null);

    assertThrows(RuntimeException.class, () -> dao.insert(closed, create));
    assertThrows(
        RuntimeException.class, () -> dao.update(closed, new UpdateClientRequest(1L, "p")));
    assertThrows(RuntimeException.class, () -> dao.activate(closed, 1L));
    assertThrows(RuntimeException.class, () -> dao.deleteById(closed, 1L));
    assertThrows(RuntimeException.class, () -> dao.selectById(closed, 1L));
    assertThrows(RuntimeException.class, () -> dao.selectByEmail(closed, "e"));
    assertThrows(
        RuntimeException.class, () -> dao.selectByProviderAndId(closed, Provider.LOCAL, "x"));
    assertThrows(RuntimeException.class, () -> dao.selectAccountsByClientId(closed, 1L));
  }

  @Test
  void emailVerificationDaoShouldWrapSqlExceptions() {
    EmailVerificationDAO dao = new EmailVerificationDAO();
    var create =
        new CreateEmailVerificationRequest(
            1L, "t", EmailVerificationType.EMAIL_VERIFICATION, Instant.now(), null);

    assertThrows(RuntimeException.class, () -> dao.insert(closed, create));
    assertThrows(
        RuntimeException.class,
        () -> dao.findValidByToken(closed, "t", EmailVerificationType.EMAIL_VERIFICATION));
    assertThrows(
        RuntimeException.class,
        () -> dao.findActiveByClientId(closed, 1L, EmailVerificationType.EMAIL_VERIFICATION));
    assertThrows(RuntimeException.class, () -> dao.markVerified(closed, 1L));
    assertThrows(
        RuntimeException.class,
        () -> dao.invalidatePreviousTokens(closed, 1L, EmailVerificationType.EMAIL_VERIFICATION));
  }

  @Test
  void transactionDaoShouldWrapSqlExceptions() {
    TransactionDAO dao = new TransactionDAO();
    var create =
        new CreateTransactionRequest(
            1L,
            "1",
            2L,
            "2",
            BigDecimal.TEN,
            TransactionType.TRANSFERENCE,
            TransactionStatus.PENDING,
            "s");

    assertThrows(RuntimeException.class, () -> dao.insert(closed, create));
    assertThrows(
        RuntimeException.class,
        () -> dao.update(closed, new UpdateTransactionRequest(1L, TransactionStatus.COMPLETE)));
    assertThrows(RuntimeException.class, () -> dao.selectFromAccountId(closed, 1L));
    assertThrows(RuntimeException.class, () -> dao.selectToAccountId(closed, 1L));
    assertThrows(RuntimeException.class, () -> dao.selectByAccountId(closed, 1L));
  }

  @Test
  void artifactDaosShouldWrapSqlExceptions() {
    ArtifactDAO artifactDAO = new ArtifactDAO();
    assertThrows(
        RuntimeException.class,
        () -> artifactDAO.insert(closed, new CreateArtifactRequest(Map.of(), 1)));
    assertThrows(RuntimeException.class, () -> artifactDAO.selectById(closed, 1L));
    assertThrows(RuntimeException.class, () -> artifactDAO.selectAllSummaries(closed));
    assertThrows(RuntimeException.class, () -> artifactDAO.updateTotalSupply(closed, 1L, 1));

    ArtifactBundleDAO bundleDAO = new ArtifactBundleDAO();
    assertThrows(RuntimeException.class, () -> bundleDAO.insert(closed, "id"));
    assertThrows(RuntimeException.class, () -> bundleDAO.selectAll(closed, 0, 10));

    ArtifactBundleItemDAO bundleItemDAO = new ArtifactBundleItemDAO();
    assertThrows(RuntimeException.class, () -> bundleItemDAO.insertItems(closed, 1L, List.of(1L)));
    assertThrows(
        RuntimeException.class, () -> bundleItemDAO.selectItemsByBundleId(closed, 1L, 0, 10));

    ArtifactUnitDAO unitDAO = new ArtifactUnitDAO();
    assertThrows(
        RuntimeException.class,
        () -> unitDAO.insert(closed, new CreateArtifactUnitRequest(1L, 1L)));
    assertThrows(RuntimeException.class, () -> unitDAO.selectById(closed, 1L));
    assertThrows(RuntimeException.class, () -> unitDAO.updateOwner(closed, 1L, 1L));
    assertThrows(RuntimeException.class, () -> unitDAO.updateStatus(closed, 1L));
    assertThrows(RuntimeException.class, () -> unitDAO.selectByIdForUpdate(closed, 1L));
    assertThrows(RuntimeException.class, () -> unitDAO.tryUpdateToMarket(closed, 1L, 1L));
    assertThrows(RuntimeException.class, () -> unitDAO.tryTransferOwnership(closed, 1L, 1L, 2L));
    assertThrows(RuntimeException.class, () -> unitDAO.selectByOwnerAccount(closed, 1L, 0, 10));

    ArtifactListingDAO listingDAO = new ArtifactListingDAO();
    assertThrows(
        RuntimeException.class,
        () ->
            listingDAO.insert(
                closed,
                new CreateArtifactListingRequest(
                    1L, 1L, BigDecimal.TEN, ArtifactListingStatus.ACTIVE)));
    assertThrows(RuntimeException.class, () -> listingDAO.selectById(closed, 1L));
    assertThrows(
        RuntimeException.class,
        () -> listingDAO.selectByStatus(closed, ArtifactListingStatus.ACTIVE));
    assertThrows(
        RuntimeException.class,
        () -> listingDAO.updateStatus(closed, 1L, ArtifactListingStatus.SOLD));
    assertThrows(RuntimeException.class, () -> listingDAO.updatePrice(closed, 1L, BigDecimal.TEN));
    assertThrows(
        RuntimeException.class,
        () ->
            listingDAO.selectActiveByActiveStatus(
                closed, 1L, ArtifactListingFilter.empty(), 0, 10));
    assertThrows(RuntimeException.class, () -> listingDAO.selectByIdForUpdate(closed, 1L));
    assertThrows(RuntimeException.class, () -> listingDAO.selectByArtifactUnitId(closed, 1L));
    assertThrows(RuntimeException.class, () -> listingDAO.selectByOwnerAccount(closed, 1L, 0, 10));

    ArtifactTransferDAO transferDAO = new ArtifactTransferDAO();
    assertThrows(
        RuntimeException.class,
        () -> transferDAO.insert(closed, new CreateArtifactTransferRequest(1L, 1L, 2L)));
    assertThrows(RuntimeException.class, () -> transferDAO.selectById(closed, 1L));
    assertThrows(RuntimeException.class, () -> transferDAO.selectPublicFeed(closed, null, 0, 10));
    assertThrows(RuntimeException.class, () -> transferDAO.selectByUnitId(closed, 1L));

    ArtifactPriceHistoryDAO priceDAO = new ArtifactPriceHistoryDAO();
    assertThrows(
        RuntimeException.class,
        () ->
            priceDAO.insert(
                closed,
                new CreatePriceHistoryRequest(
                    1L, 1L, null, BigDecimal.TEN, 1L, ReasonType.CREATED)));
    assertThrows(RuntimeException.class, () -> priceDAO.selectByArtifactListingId(closed, 1L));
    assertThrows(RuntimeException.class, () -> priceDAO.selectLatestByArtifactUnitId(closed, 1L));
    assertThrows(RuntimeException.class, () -> priceDAO.selectByArtifactUnitId(closed, 1L));
  }
}
