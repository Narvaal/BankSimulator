package br.com.ale.service.marketplace;

class AssetPriceHistoryServiceTest {
    /*
    private ClientService clientService;
    private AccountService accountService;
    private AssetService assetService;
    private AssetUnityService assetUnityService;
    private AssetListingService assetListingService;
    private AssetPriceHistoryService assetPriceHistoryService;
    private AssetWebhookNotifier webhookNotifier;

    @BeforeEach
    void setup() {
        ConnectionProvider connectionProvider = new TestConnectionProvider();
        webhookNotifier = new AssetWebhookNotifier("", false);
        PrivateKeyStorage privateKeyStorage = new InMemoryPrivateKeyStorage();
        clientService = new ClientService(connectionProvider);
        accountService = new AccountService(connectionProvider, privateKeyStorage);
        assetService = new AssetService(connectionProvider);
        assetUnityService = new AssetUnityService(connectionProvider, webhookNotifier);
        assetListingService = new AssetListingService(connectionProvider);
        assetPriceHistoryService = new AssetPriceHistoryService(connectionProvider);
    }

    @Test
    void shouldRegisterPriceChangeSuccessfully() {

        AssetListing listing = createListing(new BigDecimal("100.00"));
        Account admin = createAccount();

        assertDoesNotThrow(() ->
                assetPriceHistoryService.registerPriceChange(
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
                        () -> assetPriceHistoryService.registerPriceChange(
                                9999L,
                                new BigDecimal("120.00"),
                                admin.getId(),
                                ReasonType.MANUAL_ADJUSTMENT
                        )
                );

        assertNotNull(ex.getCause());
        assertTrue(
                ex.getCause().getMessage().contains("AssetListing not found"),
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

    private AssetListing createListing(BigDecimal price) {

        Account seller = createAccount();

        Asset asset =
                assetService.createAsset(
                        new CreateAssetRequest(
                                "Asset " + System.nanoTime(),
                                1
                        )
                );

        var unity =
                assetUnityService.createAssetUnity(
                        new CreateAssetUnityRequest(
                                asset.getId(),
                                seller.getId()
                        )
                );

        return assetListingService.createAssetListing(
                new CreateAssetListingRequest(
                        unity.getId(),
                        seller.getId(),
                        price,
                        AssetListingStatus.ACTIVE
                )
        );
    }

     */
}
