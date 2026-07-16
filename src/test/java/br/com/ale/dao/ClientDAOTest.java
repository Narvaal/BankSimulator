package br.com.ale.dao;

import static org.junit.jupiter.api.Assertions.*;

import br.com.ale.domain.client.Client;
import br.com.ale.domain.client.Provider;
import br.com.ale.dto.CreateClientRequest;
import br.com.ale.dto.UpdateClientRequest;
import br.com.ale.support.DbTestSupport;
import java.sql.Connection;
import org.junit.jupiter.api.Test;

class ClientDAOTest extends DbTestSupport {

  @Test
  void shouldInsertAndSelectById() throws Exception {
    long id = insertClient("John Doe", "john@test.com");

    try (Connection conn = open()) {
      Client client = clientDAO.selectById(conn, id).orElseThrow();
      assertEquals("John Doe", client.getName());
      assertEquals("john@test.com", client.getEmail());
      assertEquals(Provider.LOCAL, client.getProvider());
      assertFalse(client.isEmailVerified());
    }
  }

  @Test
  void shouldReturnEmptyForUnknownClient() throws Exception {
    try (Connection conn = open()) {
      assertTrue(clientDAO.selectById(conn, 9999L).isEmpty());
      assertTrue(clientDAO.selectByEmail(conn, "ghost@test.com").isEmpty());
      assertTrue(clientDAO.selectByProviderAndId(conn, Provider.GOOGLE, "ghost").isEmpty());
    }
  }

  @Test
  void shouldSelectByEmail() throws Exception {
    insertClient("Jane", "jane@test.com");

    try (Connection conn = open()) {
      Client client = clientDAO.selectByEmail(conn, "jane@test.com").orElseThrow();
      assertEquals("Jane", client.getName());
    }
  }

  @Test
  void shouldSelectByProviderAndId() throws Exception {
    try (Connection conn = open()) {
      clientDAO.insert(
          conn,
          new CreateClientRequest(
              "Google User", "guser@test.com", null, Provider.GOOGLE, "google-123", true, "pic"));

      Client client =
          clientDAO.selectByProviderAndId(conn, Provider.GOOGLE, "google-123").orElseThrow();
      assertEquals("Google User", client.getName());
      assertTrue(client.isEmailVerified());
    }
  }

  @Test
  void shouldUpdatePassword() throws Exception {
    long id = insertClient("John", "john@test.com");

    try (Connection conn = open()) {
      int rows = clientDAO.update(conn, new UpdateClientRequest(id, "new-hash"));
      assertEquals(1, rows);
      assertEquals("new-hash", clientDAO.selectById(conn, id).orElseThrow().getPassword());
    }
  }

  @Test
  void activateShouldVerifyEmailOnlyOnce() throws Exception {
    long id = insertClient("John", "john@test.com");

    try (Connection conn = open()) {
      assertEquals(1, clientDAO.activate(conn, id));
      assertTrue(clientDAO.selectById(conn, id).orElseThrow().isEmailVerified());
      assertEquals(0, clientDAO.activate(conn, id));
    }
  }

  @Test
  void shouldDeleteClient() throws Exception {
    long id = insertClient("Temp", "temp@test.com");

    try (Connection conn = open()) {
      assertEquals(1, clientDAO.deleteById(conn, id));
      assertTrue(clientDAO.selectById(conn, id).isEmpty());
      assertEquals(0, clientDAO.deleteById(conn, id));
    }
  }

  @Test
  void shouldListAccountsOfClient() throws Exception {
    long clientId = insertClient("Multi", "multi@test.com");
    insertAccount(clientId, "111-000-001");
    insertAccount(clientId, "111-000-002");

    try (Connection conn = open()) {
      var accounts = clientDAO.selectAccountsByClientId(conn, clientId);
      assertEquals(2, accounts.size());
    }
  }

  @Test
  void shouldRejectDuplicateEmail() {
    insertClient("Original", "dup@test.com");
    assertThrows(RuntimeException.class, () -> insertClient("Copy", "dup@test.com"));
  }
}
