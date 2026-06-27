package br.com.ale.service.marketplace;

import br.com.ale.domain.account.Account;
import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.domain.artifact.Artifact;
import br.com.ale.domain.artifact.ArtifactListing;
import br.com.ale.domain.artifact.ArtifactListingStatus;
import br.com.ale.domain.artifact.ArtifactUnit;
import br.com.ale.domain.artifact.ReasonType;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.client.Provider;
import br.com.ale.dto.*;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.ClientService;
import br.com.ale.service.artifact.ArtifactListingService;
import br.com.ale.service.artifact.ArtifactService;
import br.com.ale.service.artifact.ArtifactUnitService;
import br.com.ale.service.crypto.InMemoryPrivateKeyStorage;
import br.com.ale.service.crypto.PrivateKeyStorage;
import br.com.ale.service.webhook.ArtifactWebhookNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ArtifactPriceHistoryServiceTest {

    private TestConnectionProvider provider;
    private ClientService clientService;
    private AccountService accountService;
    private ArtifactService assetService;
    private ArtifactUnitService artifactUnitService;
    private ArtifactListingService artifactListingService;
    private ArtifactPriceHistoryService artifactPriceHistoryService;
    private ArtifactWebhookNotifier webhookNotifier;

    @BeforeEach
    void setup() {
        provider = new TestConnectionProvider();
        webhookNotifier = new ArtifactWebhookNotifier("", false);
        PrivateKeyStorage privateKeyStorage = new InMemoryPrivateKeyStorage();
        clientService = new ClientService(provider);
        accountService = new AccountService(provider, privateKeyStorage);
        assetService = new ArtifactService(provider);
        artifactUnitService = new ArtifactUnitService(provider, webhookNotifier);
        artifactListingService = new ArtifactListingService(provider);
        artifactPriceHistoryService = new ArtifactPriceHistoryService(provider);
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
    void shouldRegisterPriceChangeSuccessfully() {

        ArtifactListing listing = createListing(new BigDecimal("100.00"));
        Account admin = createAccount();

        assertDoesNotThrow(() ->
                artifactPriceHistoryService.registerPriceChange(
                        listing.getId(),
                        new BigDecimal("120.00"),
                        admin.getId(),
                        ReasonType.MANUAL_ADJUSTMENT
                )
        );
    }

    @Test
    void shouldFailWhenListingDoesNotExist() {

        Account admin = createAccount();

        RuntimeException ex =
                assertThrows(
                        RuntimeException.class,
                        () -> artifactPriceHistoryService.registerPriceChange(
                                9999L,
                                new BigDecimal("120.00"),
                                admin.getId(),
                                ReasonType.MANUAL_ADJUSTMENT
                        )
                );

        assertNotNull(ex.getCause());
        assertTrue(
                ex.getCause().getMessage().contains("ArtifactListing not found"),
                ex.getCause().getMessage()
        );
    }

    private Account createAccount() {

        var client =
                clientService.createClient(
                        new CreateClientRequest(
                                "Client " + System.nanoTime(),
                                String.valueOf(System.nanoTime()),
                                "pass",
                                Provider.LOCAL,
                                null,
                                false,
                                null
                        )
                );

        return accountService.createAccount(
                new CreateAccountRequest(
                        client.getId(),
                        "ACC-" + System.nanoTime(),
                        AccountType.DEFAULT,
                        AccountStatus.ACTIVE
                )
        );
    }

    private ArtifactListing createListing(BigDecimal price) {

        Account seller = createAccount();

        Artifact artifact =
                assetService.createAsset(
                        new CreateArtifactRequest(
                                "Artifact " + System.nanoTime(),
                                1
                        )
                );

        ArtifactUnit unity =
                artifactUnitService.createArtifactUnit(
                        new CreateArtifactUnitRequest(
                                artifact.getId(),
                                seller.getId()
                        )
                );

        return artifactListingService.createArtifactOffer(
                new CreateArtifactListingRequest(
                        unity.getId(),
                        seller.getId(),
                        price,
                        ArtifactListingStatus.ACTIVE
                )
        );
    }
}
