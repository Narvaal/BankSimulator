package br.com.ale.service;

import static org.junit.jupiter.api.Assertions.*;

import br.com.ale.domain.transaction.Transaction;
import br.com.ale.domain.transaction.TransactionStatus;
import br.com.ale.domain.transaction.TransactionType;
import br.com.ale.dto.CreateTransactionRequest;
import br.com.ale.dto.UpdateTransactionRequest;
import br.com.ale.support.DbTestSupport;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class TransactionServiceTest extends DbTestSupport {

  private TransactionService service() {
    return new TransactionService(provider);
  }

  private Transaction createTransfer() {
    return service()
        .createTransaction(
            new CreateTransactionRequest(
                1L,
                "111-000-001",
                2L,
                "111-000-002",
                new BigDecimal("30.00"),
                TransactionType.TRANSFERENCE,
                TransactionStatus.PENDING,
                "sig"));
  }

  @Test
  void shouldCreateTransaction() {
    Transaction tx = createTransfer();

    assertTrue(tx.getId() > 0);
    assertEquals(TransactionStatus.PENDING, tx.getStatus());
    assertEquals(0, tx.getAmount().compareTo(new BigDecimal("30.00")));
  }

  @Test
  void shouldUpdateStatus() {
    Transaction tx = createTransfer();

    service().updateStatus(new UpdateTransactionRequest(tx.getId(), TransactionStatus.COMPLETE));

    var listed = service().listTransfersByAccount(1L);
    assertEquals(1, listed.size());
    assertEquals(TransactionStatus.COMPLETE, listed.get(0).getStatus());
  }

  @Test
  void updateStatusShouldFailForUnknownTransaction() {
    RuntimeException ex =
        assertThrows(
            RuntimeException.class,
            () ->
                service()
                    .updateStatus(new UpdateTransactionRequest(9999L, TransactionStatus.COMPLETE)));
    assertTrue(ex.getMessage().contains("updating transaction"));
  }

  @Test
  void shouldListTransfersForBothDirections() {
    createTransfer();
    assertEquals(1, service().listTransfersByAccount(2L).size());
    assertEquals(0, service().listTransfersByAccount(9L).size());
  }
}
