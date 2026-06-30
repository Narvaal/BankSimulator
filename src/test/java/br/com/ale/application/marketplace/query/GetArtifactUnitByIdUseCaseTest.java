package br.com.ale.application.marketplace.query;

import br.com.ale.application.marketplace.command.PurchaseArtifactCommand;
import br.com.ale.application.marketplace.usecase.PurchaseArtifactUseCase;
import br.com.ale.domain.account.Account;
import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.domain.artifact.Artifact;
import br.com.ale.domain.artifact.ArtifactListing;
import br.com.ale.domain.artifact.ArtifactListingStatus;
import br.com.ale.domain.artifact.ArtifactUnit;
import br.com.ale.domain.client.Client;
import br.com.ale.domain.client.Provider;
import br.com.ale.domain.exception.ArtifactUnitNotFoundException;
import br.com.ale.dto.*;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import java.util.Map;
import br.com.ale.service.ClientService;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.artifact.ArtifactListingService;
import br.com.ale.service.artifact.ArtifactService;
import br.com.ale.service.artifact.ArtifactTransferService;
import br.com.ale.service.artifact.ArtifactUnitService;
import br.com.ale.service.auth.JwtService;
import br.com.ale.service.crypto.InMemoryPrivateKeyStorage;
import br.com.ale.service.marketplace.ArtifactPriceHistoryService;
import br.com.ale.service.marketplace.ArtifactPurchaseService;
import br.com.ale.service.webhook.ArtifactWebhookNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class GetArtifactUnitByIdUseCaseTest {

    private TestConnectionProvider provider;

    private ClientService clientService;
    private AccountService accountService;
    private ArtifactService artifactService;
    private ArtifactUnitService artifactUnitService;
    private ArtifactListingService artifactListingService;
    private ArtifactPriceHistoryService artifactPriceHistoryService;
    private ArtifactPurchaseService artifactPurchaseService;
    private ArtifactTransferService artifactTransferService;
    private JwtService jwtService;

    private PurchaseArtifactUseCase purchaseUseCase;
    private GetArtifactUnitByIdUseCase useCase;

    @BeforeEach
    void setup() {
        provider = new TestConnectionProvider();
        ArtifactWebhookNotifier webhookNotifier = new ArtifactWebhookNotifier("", false);
        jwtService = buildJwtService();

        clientService = new ClientService(provider);
        accountService = new AccountService(provider, new InMemoryPrivateKeyStorage());
        artifactService = new ArtifactService(provider);
        artifactUnitService = new ArtifactUnitService(provider, webhookNotifier);
        artifactListingService = new ArtifactListingService(provider);
        artifactPurchaseService = new ArtifactPurchaseService(provider, webhookNotifier);
        artifactPriceHistoryService = new ArtifactPriceHistoryService(provider);
        artifactTransferService = new ArtifactTransferService(provider, webhookNotifier);

        purchaseUseCase = new PurchaseArtifactUseCase(
                accountService,
                artifactListingService,
                artifactPurchaseService,
                artifactPriceHistoryService,
                jwtService
        );

        useCase = new GetArtifactUnitByIdUseCase(
                artifactUnitService,
                artifactService,
                artifactPriceHistoryService,
                artifactTransferService
        );

        cleanDatabase();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private JwtService buildJwtService() {
        JwtService service = new JwtService();
        String secret = Base64.getEncoder()
                .encodeToString("test-secret-key-for-unit-tests!!".getBytes(StandardCharsets.UTF_8));
        ReflectionTestUtils.setField(service, "secretKey", secret);
        ReflectionTestUtils.setField(service, "jwtExpiration", 3600000L);
        return service;
    }

    private void cleanDatabase() {
        try (var conn = provider.getConnection(); var stmt = conn.createStatement()) {
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

    private Client newClient() {
        return clientService.createClient(new CreateClientRequest(
                "User " + System.nanoTime(), "u-" + System.nanoTime() + "@test.com",
                "pass", Provider.LOCAL, null, false, null
        ));
    }

    private Account newAccount(Client client) {
        return accountService.createAccount(new CreateAccountRequest(
                client.getId(), "ACC-" + System.nanoTime(), AccountType.DEFAULT, AccountStatus.ACTIVE
        ));
    }

    private Account fundedAccount(Client client, BigDecimal balance) {
        Account account = newAccount(client);
        accountService.credit(account.getAccountNumber(), balance);
        return account;
    }

    private Artifact newArtifact(String text) {
        return artifactService.createAsset(new CreateArtifactRequest(Map.of("name", text, "rarity", "Common"), 10));
    }

    private ArtifactUnit newUnit(Artifact artifact, Account owner) {
        return artifactUnitService.createArtifactUnit(
                new CreateArtifactUnitRequest(artifact.getId(), owner.getId())
        );
    }

    private ArtifactListing list(ArtifactUnit unit, Account seller, BigDecimal price) {
        return artifactListingService.createArtifactOffer(
                new CreateArtifactListingRequest(unit.getId(), seller.getId(), price, ArtifactListingStatus.ACTIVE)
        );
    }

    private void buy(ArtifactListing listing, Client buyerClient) {
        purchaseUseCase.execute(new PurchaseArtifactCommand(
                listing.getId(), jwtService.generateToken(buyerClient.getId())
        ));
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    @Test
    void shouldThrowWhenUnitDoesNotExist() {
        assertThrows(ArtifactUnitNotFoundException.class, () -> useCase.execute(99999L));
    }

    @Test
    void shouldReturnCorrectUnitData() {
        Client ownerClient = newClient();
        Account owner = newAccount(ownerClient);
        Artifact artifact = newArtifact("RareLines Genesis #001");
        ArtifactUnit unit = newUnit(artifact, owner);

        ArtifactUnitDetailView detail = useCase.execute(unit.getId());

        assertEquals(unit.getId(), detail.unitId());
        assertEquals(artifact.getId(), detail.artifactId());
        assertEquals("RareLines Genesis #001", detail.artifactName());
        assertEquals(owner.getId(), detail.ownerAccountId());
        assertEquals("AVAILABLE", detail.status());
        assertNotNull(detail.createdAt());
    }

    @Test
    void shouldReturnEmptyHistoryAndTransfersWhenNeverSold() {
        Client ownerClient = newClient();
        Account owner = newAccount(ownerClient);
        ArtifactUnit unit = newUnit(newArtifact("Fresh Mint"), owner);

        ArtifactUnitDetailView detail = useCase.execute(unit.getId());

        assertTrue(detail.priceHistory().isEmpty());
        assertTrue(detail.transfers().isEmpty());
    }

    @Test
    void shouldReturnPriceHistoryAfterSale() {
        Client sellerClient = newClient();
        Account seller = newAccount(sellerClient);
        Client buyerClient = newClient();
        fundedAccount(buyerClient, new BigDecimal("500.00"));

        ArtifactUnit unit = newUnit(newArtifact("First Sale"), seller);
        ArtifactListing listing = list(unit, seller, new BigDecimal("75.00"));
        buy(listing, buyerClient);

        ArtifactUnitDetailView detail = useCase.execute(unit.getId());

        assertEquals(1, detail.priceHistory().size());
        assertEquals(0, new BigDecimal("75.00").compareTo(detail.priceHistory().get(0).getNewPrice()));
    }

    @Test
    void shouldReturnTransferChainAfterSale() {
        Client sellerClient = newClient();
        Account seller = newAccount(sellerClient);
        Client buyerClient = newClient();
        Account buyer = fundedAccount(buyerClient, new BigDecimal("500.00"));

        ArtifactUnit unit = newUnit(newArtifact("Provenance Test"), seller);
        ArtifactListing listing = list(unit, seller, new BigDecimal("40.00"));
        buy(listing, buyerClient);

        ArtifactUnitDetailView detail = useCase.execute(unit.getId());

        assertEquals(1, detail.transfers().size());
        var transfer = detail.transfers().get(0);
        assertEquals(seller.getId(), transfer.fromAccountId());
        assertEquals(buyer.getId(), transfer.toAccountId());
        assertEquals(0, new BigDecimal("40.00").compareTo(transfer.salePrice()));
    }

    @Test
    void shouldReturnCorrectPricePairingForResoldUnit() {
        // seller → buyer1 at $30, then buyer1 → buyer2 at $80
        // chain: [seller→buyer1 $30, buyer1→buyer2 $80] ordered oldest first
        Client sellerClient = newClient();
        Account seller = newAccount(sellerClient);
        Client buyer1Client = newClient();
        Account buyer1 = fundedAccount(buyer1Client, new BigDecimal("500.00"));
        Client buyer2Client = newClient();
        Account buyer2 = fundedAccount(buyer2Client, new BigDecimal("500.00"));

        ArtifactUnit unit = newUnit(newArtifact("Resale Artifact"), seller);

        ArtifactListing listing1 = list(unit, seller, new BigDecimal("30.00"));
        buy(listing1, buyer1Client);

        ArtifactUnit refreshed = artifactUnitService.selectById(unit.getId());
        ArtifactListing listing2 = list(refreshed, buyer1, new BigDecimal("80.00"));
        buy(listing2, buyer2Client);

        ArtifactUnitDetailView detail = useCase.execute(unit.getId());

        assertEquals(2, detail.transfers().size());
        assertEquals(2, detail.priceHistory().size());

        // oldest first → first transfer is seller→buyer1 at $30
        var t1 = detail.transfers().get(0);
        assertEquals(seller.getId(), t1.fromAccountId());
        assertEquals(buyer1.getId(), t1.toAccountId());
        assertEquals(0, new BigDecimal("30.00").compareTo(t1.salePrice()));

        // second transfer is buyer1→buyer2 at $80
        var t2 = detail.transfers().get(1);
        assertEquals(buyer1.getId(), t2.fromAccountId());
        assertEquals(buyer2.getId(), t2.toAccountId());
        assertEquals(0, new BigDecimal("80.00").compareTo(t2.salePrice()));
    }

    @Test
    void shouldReflectUpdatedOwnerAfterSale() {
        Client sellerClient = newClient();
        Account seller = newAccount(sellerClient);
        Client buyerClient = newClient();
        Account buyer = fundedAccount(buyerClient, new BigDecimal("500.00"));

        ArtifactUnit unit = newUnit(newArtifact("Owner Track"), seller);
        assertEquals(seller.getId(), useCase.execute(unit.getId()).ownerAccountId());

        ArtifactListing listing = list(unit, seller, new BigDecimal("10.00"));
        buy(listing, buyerClient);

        ArtifactUnitDetailView detail = useCase.execute(unit.getId());
        assertEquals(buyer.getId(), detail.ownerAccountId());
        assertEquals("AVAILABLE", detail.status());
    }
}
