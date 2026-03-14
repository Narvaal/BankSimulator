package br.com.ale.application.marketplace.usecase;

class CreateAssetOfferUseCaseTest {
    /*
    private TestConnectionProvider provider;

    private ClientService clientService;
    private AccountService accountService;

    private AssetService assetService;
    private AssetUnityService assetUnityService;
    private AssetListingService assetListingService;
    private AssetWebhookNotifier webhookNotifier;

    private AuthService authService;
    private CreateAssetOfferUseCase useCase;
    private JwtService jwtService;

    @BeforeEach
    void setup() {

        provider = new TestConnectionProvider();
        webhookNotifier = new AssetWebhookNotifier("", false);

        clientService = new ClientService(provider);
        accountService = new AccountService(provider, new InMemoryPrivateKeyStorage());

        assetService = new AssetService(provider);
        assetUnityService = new AssetUnityService(provider, webhookNotifier);
        assetListingService = new AssetListingService(provider);

        authService = new AuthService(provider);

        cleanDatabase();
    }

    private void cleanDatabase() {
        try (var conn = provider.getConnection();
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
    void shouldCreateAssetOfferSuccessfully() {

        Client client = createClient();
        Account owner = createAccountWithCredential(client);

        KeyPairService keyPairService = new KeyPairService();
        var keyPair = keyPairService.generate();

        InMemoryPrivateKeyStorage privateKeyStorage =
                new InMemoryPrivateKeyStorage();

        privateKeyStorage.save(
                owner.getId(),
                keyPair.getPrivate().getEncoded()
        );

        SimpleTokenGenerator tokenGenerator =
                new SimpleTokenGenerator(
                        keyPair.getPrivate(),
                        keyPair.getPublic()
                );

        authService = new AuthService(provider, tokenGenerator);

        useCase = new CreateAssetOfferUseCase(
                assetListingService,
                assetUnityService,
                jwtService
        );

        AssetUnity unity = createAssetUnity(owner);


        AuthToken token = authService.authenticate(
                new CreateAuthenticationRequest(
                        client.getEmail(),
                        "pass"
                )
        );

        CreateAssetOfferCommand command =
                new CreateAssetOfferCommand(
                        owner.getId(),
                        unity.getId(),
                        new BigDecimal("100.00"),
                        token.getToken()
                );

        AssetListing listing = useCase.execute(command);

        assertNotNull(listing);
        assertEquals(AssetListingStatus.ACTIVE, listing.getStatus());
        assertEquals(owner.getId(), listing.getSellerAccountId());
        assertEquals(unity.getId(), listing.getAssetUnityId());
    }

    @Test
    void shouldFailWhenTokenIsInvalid() {

        Client client = createClient();
        Account owner = createAccountWithCredential(client);

        KeyPairService keyPairService = new KeyPairService();
        var keyPair = keyPairService.generate();

        InMemoryPrivateKeyStorage privateKeyStorage =
                new InMemoryPrivateKeyStorage();

        privateKeyStorage.save(
                owner.getId(),
                keyPair.getPrivate().getEncoded()
        );

        SimpleTokenGenerator tokenGenerator =
                new SimpleTokenGenerator(
                        keyPair.getPrivate(),
                        keyPair.getPublic()
                );

        authService = new AuthService(provider, tokenGenerator);

        useCase = new CreateAssetOfferUseCase(
                assetListingService,
                assetUnityService,
                jwtService
        );

        AssetUnity unity = createAssetUnity(owner);

        CreateAssetOfferCommand command =
                new CreateAssetOfferCommand(
                        owner.getId(),
                        unity.getId(),
                        new BigDecimal("100.00"),
                        "invalid.token.value"
                );

        assertThrows(
                InvalidCredentialsException.class,
                () -> useCase.execute(command)
        );
    }

    @Test
    void shouldFailWhenAuthenticatedClientIsNotOwner() {

        Client ownerClient = createClient();
        Account owner = createAccountWithCredential(ownerClient);

        Client attackerClient = createClient();
        Account attacker = createAccountWithCredential(attackerClient);

        KeyPairService keyPairService = new KeyPairService();
        var keyPair = keyPairService.generate();

        InMemoryPrivateKeyStorage privateKeyStorage =
                new InMemoryPrivateKeyStorage();

        privateKeyStorage.save(
                attacker.getId(),
                keyPair.getPrivate().getEncoded()
        );

        SimpleTokenGenerator tokenGenerator =
                new SimpleTokenGenerator(
                        keyPair.getPrivate(),
                        keyPair.getPublic()
                );

        authService = new AuthService(provider, tokenGenerator);

        useCase = new CreateAssetOfferUseCase(
                assetListingService,
                assetUnityService,
                jwtService
        );

        AssetUnity unity = createAssetUnity(owner);

        AuthToken attackerToken =
                authService.authenticate(
                        new CreateAuthenticationRequest(
                                attackerClient.getEmail(),
                                "pass"
                        )
                );

        CreateAssetOfferCommand command =
                new CreateAssetOfferCommand(
                        owner.getId(),
                        unity.getId(),
                        new BigDecimal("100.00"),
                        attackerToken.getToken()
                );

        assertThrows(
                UnauthorizedOperationException.class,
                () -> useCase.execute(command)
        );
    }

    private Account createAccountWithCredential(Client client) {

        Account account =
                accountService.createAccount(
                        new CreateAccountRequest(
                                client.getId(),
                                "ACC-" + System.nanoTime(),
                                AccountType.DEFAULT,
                                AccountStatus.ACTIVE
                        )
                );

        return account;
    }

    private AssetUnity createAssetUnity(Account owner) {

        Asset asset =
                assetService.createAsset(
                        new CreateAssetRequest(
                                "Asset " + System.nanoTime(),
                                1
                        )
                );

        return assetUnityService.createAssetUnity(
                new CreateAssetUnityRequest(
                        asset.getId(),
                        owner.getId()
                )
        );
    }

    private Client createClient() {

        String hashed = PasswordHasher.hash("pass");

        return clientService.createClient(
                new CreateClientRequest(
                        "Client " + System.nanoTime(),
                        "client" + System.nanoTime() + "@test.com",
                        hashed,
                        Provider.LOCAL,
                        null,
                        false,
                        null
                )
        );
    }

     */
}
