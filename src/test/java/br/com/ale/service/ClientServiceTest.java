package br.com.ale.service;

import br.com.ale.domain.client.Client;
import br.com.ale.dto.CreateClientRequest;
import br.com.ale.dto.UpdateClientRequest;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientServiceTest {

    private static final String VALID_NAME = "John Doe";
    private static final String UPDATED_NAME = "John";
    private static final String VALID_DOCUMENT = "123456789";

    private ClientService clientService;

    @BeforeEach
    void setup() {
        clientService = new ClientService(new TestConnectionProvider());
        cleanDatabase();
    }

    private void cleanDatabase() {
        try (var conn = new TestConnectionProvider().getConnection();
             var stmt = conn.createStatement()) {

            stmt.execute("DELETE FROM asset_price_history");
            stmt.execute("DELETE FROM asset_transfer");
            stmt.execute("DELETE FROM asset_listing");
            stmt.execute("DELETE FROM asset_unit");
            stmt.execute("DELETE FROM asset");
            stmt.execute("DELETE FROM transactions");
            stmt.execute("DELETE FROM account");
            stmt.execute("DELETE FROM credential");
            stmt.execute("DELETE FROM client");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldCreateClient() {
        Client client = clientService.createClient(validClient());

        assertEquals(VALID_NAME, client.getName());
        assertEquals(VALID_DOCUMENT, client.getEmail());
    }

    @Test
    void shouldSelectClientByEmail() {
        clientService.createClient(validClient());

        Client client =
                clientService.getClientByEmail(VALID_DOCUMENT);

        assertEquals(VALID_NAME, client.getName());
        assertEquals(VALID_DOCUMENT, client.getEmail());
    }

    @Test
    void shouldUpdateClient() {
        Client client = clientService.createClient(validClient());

        clientService.updateClient(validUpdate(client));

        Client updatedClient =
                clientService.getClientByEmail(VALID_DOCUMENT);

        assertEquals(UPDATED_NAME, updatedClient.getName());
        assertEquals(VALID_DOCUMENT, updatedClient.getEmail());
    }

    @Test
    void shouldDeleteClient() {
        Client client = clientService.createClient(validClient());

        assertDoesNotThrow(() ->
                clientService.deleteClient(client.getId())
        );
    }

    @Test
    void shouldThrowWhenDeletingNonExistingClient() {
        assertThrows(
                RuntimeException.class,
                () -> clientService.deleteClient(9999L)
        );
    }

    @Test
    void shouldCreateClientAndReturnGeneratedId() {

        Client client = clientService.createClient(
                new CreateClientRequest(VALID_NAME, VALID_DOCUMENT)
        );

        assertNotNull(client);
        assertTrue(client.getId() > 0);
        assertEquals(VALID_NAME, client.getName());
        assertEquals(VALID_DOCUMENT, client.getEmail());
    }


    private CreateClientRequest validClient() {
        return new CreateClientRequest(
                VALID_NAME,
                VALID_DOCUMENT
        );
    }

    private UpdateClientRequest validUpdate(Client client) {
        return new UpdateClientRequest(
                client.getId(),
                UPDATED_NAME
        );
    }
}
