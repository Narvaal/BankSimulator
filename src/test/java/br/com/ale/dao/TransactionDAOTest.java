package br.com.ale.dao;

import static org.junit.jupiter.api.Assertions.*;

import br.com.ale.domain.transaction.Transaction;
import br.com.ale.domain.transaction.TransactionStatus;
import br.com.ale.domain.transaction.TransactionType;
import br.com.ale.dto.CreateTransactionRequest;
import br.com.ale.dto.UpdateTransactionRequest;
import br.com.ale.support.DbTestSupport;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import org.junit.jupiter.api.Test;

class TransactionDAOTest extends DbTestSupport {

  private final TransactionDAO dao = new TransactionDAO();

  private long insertTransfer(Long fromId, Long toId) {
    try (Connection conn = open()) {
      return dao.insert(
          conn,
          new CreateTransactionRequest(
              fromId,
              fromId != null ? "111-000-001" : null,
              toId,
              toId != null ? "111-000-002" : null,
              new BigDecimal("25.00"),
              TransactionType.TRANSFERENCE,
              TransactionStatus.PENDING,
              "signature-base64"));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void shouldInsertAndSelectByFromAccount() throws Exception {
    long id = insertTransfer(1L, 2L);
    assertTrue(id > 0);

    try (Connection conn = open()) {
      List<Transaction> txs = dao.selectFromAccountId(conn, 1L);
      assertEquals(1, txs.size());

      Transaction tx = txs.get(0);
      assertEquals(1L, tx.getFromAccountId());
      assertEquals(2L, tx.getToAccountId());
      assertEquals(0, tx.getAmount().compareTo(new BigDecimal("25.00")));
      assertEquals(TransactionType.TRANSFERENCE, tx.getType());
      assertEquals(TransactionStatus.PENDING, tx.getStatus());
      assertEquals("signature-base64", tx.getSignature());
    }
  }

  @Test
  void shouldSelectByToAccount() throws Exception {
    insertTransfer(1L, 2L);
    insertTransfer(3L, 2L);

    try (Connection conn = open()) {
      assertEquals(2, dao.selectToAccountId(conn, 2L).size());
      assertEquals(0, dao.selectToAccountId(conn, 9L).size());
    }
  }

  @Test
  void shouldSelectByAccountIdInEitherDirection() throws Exception {
    insertTransfer(1L, 2L);
    insertTransfer(2L, 3L);
    insertTransfer(4L, 5L);

    try (Connection conn = open()) {
      assertEquals(2, dao.selectByAccountId(conn, 2L).size());
    }
  }

  @Test
  void shouldInsertDepositWithoutFromAccount() throws Exception {
    long id = insertTransfer(null, 2L);
    assertTrue(id > 0);

    try (Connection conn = open()) {
      Transaction tx = dao.selectToAccountId(conn, 2L).get(0);
      assertNull(tx.getFromAccountId());
      assertNull(tx.getFromAccountNumber());
    }
  }

  @Test
  void shouldUpdateStatus() throws Exception {
    long id = insertTransfer(1L, 2L);

    try (Connection conn = open()) {
      int rows = dao.update(conn, new UpdateTransactionRequest(id, TransactionStatus.COMPLETE));
      assertEquals(1, rows);
      assertEquals(
          TransactionStatus.COMPLETE, dao.selectFromAccountId(conn, 1L).get(0).getStatus());

      assertEquals(
          0, dao.update(conn, new UpdateTransactionRequest(9999L, TransactionStatus.COMPLETE)));
    }
  }
}
