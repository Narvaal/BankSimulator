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
import br.com.ale.dto.*;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import br.com.ale.service.ClientService;
import br.com.ale.service.account.AccountService;
import java.util.Map;
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

class ListArtifactTransferLogUseCaseTest {

    private TestConnectionProvider provider;

    private ClientService clientService;
    private AccountService accountService;
    private ArtifactService artifactService;
    private ArtifactUnitService artifactUnitService;
    private ArtifactListingService artifactListingService;
    private ArtifactPurchaseService artifactPurchaseService;
    private ArtifactPriceHistoryService artifactPriceHistoryService;
    private ArtifactTransferService artifactTransferService;
    private ArtifactWebhookNotifier webhookNotifier;
    private JwtService jwtService;

    private PurchaseArtifactUseCase purchaseUseCase;
    private ListArtifactTransferLogUseCase useCase;

    @BeforeEach
    void setup() {
        provider = new TestConnectionProvider();
        webhookNotifier = new ArtifactWebhookNotifier("", false);
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

        useCase = new ListArtifactTransferLogUseCase(artifactTransferService);

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
                "User " + System.nanoTime(),
                "user-" + System.nanoTime() + "@test.com",
                "pass", Provider.LOCAL, null, false, null
        ));
    }

    private Account newAccount(Client client) {
        return accountService.createAccount(new CreateAccountRequest(
                client.getId(), "ACC-" + System.nanoTime(),
                AccountType.DEFAULT, AccountStatus.ACTIVE
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
                new CreateArtifactListingRequest(unit.getId(), seller.getId(),
                        price, ArtifactListingStatus.ACTIVE)
        );
    }

    private void buy(ArtifactListing listing, Client buyerClient) {
        purchaseUseCase.execute(new PurchaseArtifactCommand(
                listing.getId(), jwtService.generateToken(buyerClient.getId())
        ));
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    @Test
    void shouldReturnEmptyFeedWhenNoTransfersExist() {
        var result = useCase.execute(null, 0, 30);

        assertNotNull(result);
        assertTrue(result.items().isEmpty());
        assertEquals(0, result.totalItems());
        assertEquals(0, result.totalPages());
    }

    @Test
    void shouldReturnSaleWithCorrectData() {
        Client sellerClient = newClient();
        Account seller = newAccount(sellerClient);
        Client buyerClient = newClient();
        Account buyer = fundedAccount(buyerClient, new BigDecimal("200.00"));

        Artifact artifact = newArtifact("Rare Lines #001");
        ArtifactUnit unit = newUnit(artifact, seller);
        ArtifactListing listing = list(unit, seller, new BigDecimal("50.00"));

        buy(listing, buyerClient);

        var result = useCase.execute(null, 0, 30);

        assertEquals(1, result.totalItems());
        assertEquals(1, result.items().size());

        var entry = result.items().get(0);
        assertEquals("Rare Lines #001", entry.artifactName());
        assertEquals(unit.getId(), entry.artifactUnitId());
        assertEquals(0, new BigDecimal("50.00").compareTo(entry.salePrice()));
        assertEquals(seller.getId(), entry.fromAccountId());
        assertEquals(buyer.getId(), entry.toAccountId());
        assertNotNull(entry.createdAt());
    }

    @Test
    void shouldOrderResultsByNewestFirst() {
        Client sellerClient = newClient();
        Account seller = newAccount(sellerClient);

        Artifact artifact1 = newArtifact("First Card");
        Artifact artifact2 = newArtifact("Second Card");

        Client buyer1Client = newClient();
        Account buyer1 = fundedAccount(buyer1Client, new BigDecimal("500.00"));

        Client buyer2Client = newClient();
        Account buyer2 = fundedAccount(buyer2Client, new BigDecimal("500.00"));

        ArtifactUnit unit1 = newUnit(artifact1, seller);
        ArtifactListing listing1 = list(unit1, seller, new BigDecimal("10.00"));
        buy(listing1, buyer1Client);

        ArtifactUnit unit2 = newUnit(artifact2, seller);
        ArtifactListing listing2 = list(unit2, seller, new BigDecimal("20.00"));
        buy(listing2, buyer2Client);

        var result = useCase.execute(null, 0, 30);

        assertEquals(2, result.totalItems());
        assertEquals("Second Card", result.items().get(0).artifactName());
        assertEquals("First Card", result.items().get(1).artifactName());
    }

    @Test
    void shouldCorrectlyPairPriceForEachSaleOfSameUnit() {
        // Seller → Buyer1 at $30, then Buyer1 → Buyer2 at $60
        // The log must show $30 for the first transfer and $60 for the second.
        Client sellerClient = newClient();
        Account seller = newAccount(sellerClient);

        Client buyer1Client = newClient();
        Account buyer1 = fundedAccount(buyer1Client, new BigDecimal("500.00"));

        Client buyer2Client = newClient();
        Account buyer2 = fundedAccount(buyer2Client, new BigDecimal("500.00"));

        Artifact artifact = newArtifact("Resold Card");
        ArtifactUnit unit = newUnit(artifact, seller);

        // First sale: seller → buyer1 at $30
        ArtifactListing listing1 = list(unit, seller, new BigDecimal("30.00"));
        buy(listing1, buyer1Client);

        // Second sale: buyer1 → buyer2 at $60
        ArtifactUnit refreshedUnit = artifactUnitService.selectById(unit.getId());
        ArtifactListing listing2 = list(refreshedUnit, buyer1, new BigDecimal("60.00"));
        buy(listing2, buyer2Client);

        var result = useCase.execute(null, 0, 30);

        assertEquals(2, result.totalItems());

        // Newest first → transfer2 (buyer1→buyer2, $60)
        var transfer2 = result.items().get(0);
        assertEquals(0, new BigDecimal("60.00").compareTo(transfer2.salePrice()));
        assertEquals(buyer1.getId(), transfer2.fromAccountId());
        assertEquals(buyer2.getId(), transfer2.toAccountId());

        // Oldest → transfer1 (seller→buyer1, $30)
        var transfer1 = result.items().get(1);
        assertEquals(0, new BigDecimal("30.00").compareTo(transfer1.salePrice()));
        assertEquals(seller.getId(), transfer1.fromAccountId());
        assertEquals(buyer1.getId(), transfer1.toAccountId());
    }

    @Test
    void shouldPaginateCorrectly() {
        Client sellerClient = newClient();
        Account seller = newAccount(sellerClient);
        Client buyerClient = newClient();
        Account buyer = fundedAccount(buyerClient, new BigDecimal("9999.00"));

        for (int i = 1; i <= 5; i++) {
            Artifact artifact = newArtifact("Card #" + String.format("%02d", i));
            ArtifactUnit unit = newUnit(artifact, seller);
            ArtifactListing listing = list(unit, seller, new BigDecimal("1.00"));
            buy(listing, buyerClient);
        }

        var page0 = useCase.execute(null, 0, 3);
        assertEquals(5, page0.totalItems());
        assertEquals(2, page0.totalPages());
        assertEquals(3, page0.items().size());
        assertEquals(0, page0.page());

        var page1 = useCase.execute(null, 1, 3);
        assertEquals(5, page1.totalItems());
        assertEquals(2, page1.items().size());
        assertEquals(1, page1.page());
    }

    @Test
    void shouldFilterByArtifactId() {
        Client sellerClient = newClient();
        Account seller = newAccount(sellerClient);
        Client buyerClient = newClient();
        Account buyer = fundedAccount(buyerClient, new BigDecimal("9999.00"));

        Artifact target = newArtifact("Target");
        Artifact other  = newArtifact("Other");

        ArtifactUnit targetUnit = newUnit(target, seller);
        ArtifactUnit otherUnit  = newUnit(other, seller);

        buy(list(targetUnit, seller, new BigDecimal("10.00")), buyerClient);
        buy(list(otherUnit,  seller, new BigDecimal("20.00")), buyerClient);

        var result = useCase.execute(target.getId(), 0, 30);

        assertEquals(1, result.totalItems());
        assertEquals("Target", result.items().get(0).artifactName());
        assertEquals(targetUnit.getId(), result.items().get(0).artifactUnitId());
    }

    @Test
    void shouldReturnAllTransfersWhenArtifactIdIsNull() {
        Client sellerClient = newClient();
        Account seller = newAccount(sellerClient);
        Client buyerClient = newClient();
        Account buyer = fundedAccount(buyerClient, new BigDecimal("9999.00"));

        buy(list(newUnit(newArtifact("A"), seller), seller, new BigDecimal("1.00")), buyerClient);
        buy(list(newUnit(newArtifact("B"), seller), seller, new BigDecimal("2.00")), buyerClient);
        buy(list(newUnit(newArtifact("C"), seller), seller, new BigDecimal("3.00")), buyerClient);

        var result = useCase.execute(null, 0, 30);

        assertEquals(3, result.totalItems());
    }

    @Test
    void shouldThrowOnNegativePage() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null, -1, 30));
    }

    @Test
    void shouldThrowOnZeroPageSize() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null, 0, 0));
    }
}
