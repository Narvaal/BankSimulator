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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ListActiveArtifactListingsUseCaseTest {

    private TestConnectionProvider provider;

    private ClientService clientService;
    private AccountService accountService;
    private ArtifactService artifactService;
    private ArtifactUnitService artifactUnitService;
    private ArtifactListingService artifactListingService;
    private ArtifactPurchaseService artifactPurchaseService;
    private ArtifactPriceHistoryService artifactPriceHistoryService;
    private JwtService jwtService;

    private PurchaseArtifactUseCase purchaseUseCase;
    private ListActiveArtifactListingsUseCase useCase;

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

        purchaseUseCase = new PurchaseArtifactUseCase(
                accountService,
                artifactListingService,
                artifactPurchaseService,
                artifactPriceHistoryService,
                jwtService
        );

        useCase = new ListActiveArtifactListingsUseCase(
                accountService,
                artifactListingService,
                jwtService
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

    private Artifact newArtifact(String name) {
        return artifactService.createAsset(new CreateArtifactRequest(Map.of("name", name, "rarity", "Common"), 10));
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

    private ArtifactListingFilter noFilter() {
        return ArtifactListingFilter.empty();
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    @Test
    void shouldReturnEmptyWhenNoListingsExist() {
        var result = useCase.execute(null, noFilter(), 0, 20);

        assertNotNull(result);
        assertTrue(result.items().isEmpty());
        assertEquals(0, result.totalItems());
    }

    @Test
    void shouldReturnActiveListings() {
        Client sellerClient = newClient();
        Account seller = newAccount(sellerClient);

        Artifact artifact = newArtifact("Alpha");
        ArtifactUnit unit = newUnit(artifact, seller);
        list(unit, seller, new BigDecimal("10.00"));

        var result = useCase.execute(null, noFilter(), 0, 20);

        assertEquals(1, result.totalItems());
        assertEquals("Alpha", result.items().get(0).artifactName());
    }

    @Test
    void shouldExcludeOwnListingsWhenTokenProvided() {
        Client sellerClient = newClient();
        Account seller = newAccount(sellerClient);

        Client otherClient = newClient();
        Account other = newAccount(otherClient);

        Artifact a1 = newArtifact("Mine");
        Artifact a2 = newArtifact("Theirs");

        list(newUnit(a1, seller), seller, new BigDecimal("5.00"));
        list(newUnit(a2, other), other, new BigDecimal("8.00"));

        String sellerToken = jwtService.generateToken(sellerClient.getId());
        var result = useCase.execute(sellerToken, noFilter(), 0, 20);

        // seller's own listing is excluded
        assertEquals(1, result.totalItems());
        assertEquals("Theirs", result.items().get(0).artifactName());
    }

    @Test
    void shouldFilterByArtifactId() {
        Client sellerClient = newClient();
        Account seller = newAccount(sellerClient);

        Artifact target = newArtifact("Target Artifact");
        Artifact other  = newArtifact("Other Artifact");

        list(newUnit(target, seller), seller, new BigDecimal("1.00"));
        list(newUnit(other, seller),  seller, new BigDecimal("2.00"));

        // second seller so our listing isn't excluded
        Client buyer2Client = newClient();
        Account buyer2 = newAccount(buyer2Client);

        var filter = new ArtifactListingFilter(target.getId(), null, "newest", null, null);
        var result = useCase.execute(null, filter, 0, 20);

        assertEquals(1, result.totalItems());
        assertEquals("Target Artifact", result.items().get(0).artifactName());
    }

    @Test
    void shouldFilterBySearchName() {
        Client sellerClient = newClient();
        Account seller = newAccount(sellerClient);

        list(newUnit(newArtifact("Apple Vision Pro"), seller), seller, new BigDecimal("100.00"));
        list(newUnit(newArtifact("Google Pixel"),     seller), seller, new BigDecimal("50.00"));

        var filter = new ArtifactListingFilter(null, "apple", "newest", null, null);
        var result = useCase.execute(null, filter, 0, 20);

        assertEquals(1, result.totalItems());
        assertEquals("Apple Vision Pro", result.items().get(0).artifactName());
    }

    @Test
    void shouldFilterBySearchNameCaseInsensitive() {
        Client sellerClient = newClient();
        Account seller = newAccount(sellerClient);

        list(newUnit(newArtifact("Bitcoin ETF Approved"), seller), seller, new BigDecimal("200.00"));
        list(newUnit(newArtifact("Ethereum Merge"),       seller), seller, new BigDecimal("150.00"));

        var filter = new ArtifactListingFilter(null, "BITCOIN", "newest", null, null);
        var result = useCase.execute(null, filter, 0, 20);

        assertEquals(1, result.totalItems());
        assertEquals("Bitcoin ETF Approved", result.items().get(0).artifactName());
    }

    @Test
    void shouldSortByPriceAscending() {
        Client sellerClient = newClient();
        Account seller = newAccount(sellerClient);

        list(newUnit(newArtifact("Cheap"),     seller), seller, new BigDecimal("5.00"));
        list(newUnit(newArtifact("Expensive"), seller), seller, new BigDecimal("999.00"));
        list(newUnit(newArtifact("Mid"),       seller), seller, new BigDecimal("50.00"));

        var filter = new ArtifactListingFilter(null, null, "price_asc", null, null);
        var result = useCase.execute(null, filter, 0, 20);

        List<ArtifactListingView> items = result.items();
        assertEquals(3, items.size());
        assertEquals("Cheap",     items.get(0).artifactName());
        assertEquals("Mid",       items.get(1).artifactName());
        assertEquals("Expensive", items.get(2).artifactName());
    }

    @Test
    void shouldSortByPriceDescending() {
        Client sellerClient = newClient();
        Account seller = newAccount(sellerClient);

        list(newUnit(newArtifact("Cheap"),     seller), seller, new BigDecimal("5.00"));
        list(newUnit(newArtifact("Expensive"), seller), seller, new BigDecimal("999.00"));
        list(newUnit(newArtifact("Mid"),       seller), seller, new BigDecimal("50.00"));

        var filter = new ArtifactListingFilter(null, null, "price_desc", null, null);
        var result = useCase.execute(null, filter, 0, 20);

        List<ArtifactListingView> items = result.items();
        assertEquals(3, items.size());
        assertEquals("Expensive", items.get(0).artifactName());
        assertEquals("Mid",       items.get(1).artifactName());
        assertEquals("Cheap",     items.get(2).artifactName());
    }

    @Test
    void shouldFilterByMinPrice() {
        Client sellerClient = newClient();
        Account seller = newAccount(sellerClient);

        list(newUnit(newArtifact("Cheap"),     seller), seller, new BigDecimal("5.00"));
        list(newUnit(newArtifact("Expensive"), seller), seller, new BigDecimal("500.00"));

        var filter = new ArtifactListingFilter(null, null, "newest", new BigDecimal("100.00"), null);
        var result = useCase.execute(null, filter, 0, 20);

        assertEquals(1, result.totalItems());
        assertEquals("Expensive", result.items().get(0).artifactName());
    }

    @Test
    void shouldFilterByMaxPrice() {
        Client sellerClient = newClient();
        Account seller = newAccount(sellerClient);

        list(newUnit(newArtifact("Cheap"),     seller), seller, new BigDecimal("5.00"));
        list(newUnit(newArtifact("Expensive"), seller), seller, new BigDecimal("500.00"));

        var filter = new ArtifactListingFilter(null, null, "newest", null, new BigDecimal("10.00"));
        var result = useCase.execute(null, filter, 0, 20);

        assertEquals(1, result.totalItems());
        assertEquals("Cheap", result.items().get(0).artifactName());
    }

    @Test
    void shouldFilterByPriceRange() {
        Client sellerClient = newClient();
        Account seller = newAccount(sellerClient);

        list(newUnit(newArtifact("Dirt Cheap"), seller), seller, new BigDecimal("1.00"));
        list(newUnit(newArtifact("Mid Range"),  seller), seller, new BigDecimal("50.00"));
        list(newUnit(newArtifact("Premium"),    seller), seller, new BigDecimal("500.00"));

        var filter = new ArtifactListingFilter(null, null, "newest",
                new BigDecimal("10.00"), new BigDecimal("100.00"));
        var result = useCase.execute(null, filter, 0, 20);

        assertEquals(1, result.totalItems());
        assertEquals("Mid Range", result.items().get(0).artifactName());
    }

    @Test
    void shouldNotReturnSoldListings() {
        Client sellerClient = newClient();
        Account seller = newAccount(sellerClient);
        Client buyerClient = newClient();
        fundedAccount(buyerClient, new BigDecimal("500.00"));

        Artifact artifact = newArtifact("Sold Out");
        ArtifactUnit unit = newUnit(artifact, seller);
        ArtifactListing listing = list(unit, seller, new BigDecimal("20.00"));
        buy(listing, buyerClient);

        var result = useCase.execute(null, noFilter(), 0, 20);

        assertTrue(result.items().isEmpty());
        assertEquals(0, result.totalItems());
    }

    @Test
    void shouldPaginateCorrectly() {
        Client sellerClient = newClient();
        Account seller = newAccount(sellerClient);

        for (int i = 1; i <= 5; i++) {
            Artifact artifact = newArtifact("Card #" + String.format("%02d", i));
            list(newUnit(artifact, seller), seller, new BigDecimal(i + ".00"));
        }

        var page0 = useCase.execute(null, noFilter(), 0, 3);
        assertEquals(5, page0.totalItems());
        assertEquals(2, page0.totalPages());
        assertEquals(3, page0.items().size());

        var page1 = useCase.execute(null, noFilter(), 1, 3);
        assertEquals(5, page1.totalItems());
        assertEquals(2, page1.items().size());
    }

    @Test
    void shouldCombineArtifactIdAndSearchFilter() {
        Client sellerClient = newClient();
        Account seller = newAccount(sellerClient);

        Artifact apple = newArtifact("Apple Vision Pro");
        Artifact google = newArtifact("Google Vision");

        list(newUnit(apple, seller),  seller, new BigDecimal("100.00"));
        list(newUnit(google, seller), seller, new BigDecimal("50.00"));

        // Filter by artifactId of "Apple Vision Pro" AND search "vision" — should match only apple
        var filter = new ArtifactListingFilter(apple.getId(), "vision", "newest", null, null);
        var result = useCase.execute(null, filter, 0, 20);

        assertEquals(1, result.totalItems());
        assertEquals("Apple Vision Pro", result.items().get(0).artifactName());
    }

    @Test
    void shouldReturnUnknownSortAsNewest() {
        Client sellerClient = newClient();
        Account seller = newAccount(sellerClient);

        list(newUnit(newArtifact("First"),  seller), seller, new BigDecimal("1.00"));
        list(newUnit(newArtifact("Second"), seller), seller, new BigDecimal("2.00"));

        // Unknown sort value → defaults to newest (created_at DESC)
        var filter = new ArtifactListingFilter(null, null, "random_garbage", null, null);
        var result = useCase.execute(null, filter, 0, 20);

        assertEquals(2, result.totalItems());
        // newest first = "Second" was inserted last
        assertEquals("Second", result.items().get(0).artifactName());
    }
}
