package br.com.ale.application.config;

import br.com.ale.application.marketplace.query.*;
import br.com.ale.application.marketplace.usecase.*;
import br.com.ale.infrastructure.db.ConnectionProvider;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.asset.*;
import br.com.ale.service.auth.AuthService;
import br.com.ale.service.auth.JwtService;
import br.com.ale.service.marketplace.AssetPriceHistoryService;
import br.com.ale.service.marketplace.AssetPurchaseService;
import br.com.ale.service.webhook.AssetWebhookNotifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MarketplaceConfig {

    @Bean
    public AssetListingService assetListingService(ConnectionProvider connectionProvider) {
        return new AssetListingService(connectionProvider);
    }

    @Bean
    public AssetService assetService(ConnectionProvider connectionProvider) {
        return new AssetService(connectionProvider);
    }

    @Bean
    public AssetGenerationService assetGenerationService() {
        return new AssetGenerationService();
    }

    @Bean
    public AssetBundleService assetBundleService(ConnectionProvider connectionProvider) {
        return new AssetBundleService(connectionProvider);
    }

    @Bean
    public AssetWebhookNotifier assetWebhookNotifier(
            @Value("${webhook.asset.url:}") String webhookUrl,
            @Value("${webhook.asset.enabled:false}") boolean enabled
    ) {
        return new AssetWebhookNotifier(webhookUrl, enabled);
    }

    @Bean
    public AssetGenerationManager assetGenerationManager(
            AssetGenerationService assetGenerationService,
            AssetBundleService assetBundleService
    ) {
        return new AssetGenerationManager(assetGenerationService, assetBundleService);
    }

    @Bean
    public AssetUnityService assetUnityService(
            ConnectionProvider connectionProvider,
            AssetWebhookNotifier webhookNotifier
    ) {
        return new AssetUnityService(connectionProvider, webhookNotifier);
    }

    @Bean
    public GetAssetListingByIdUseCase getAssetListingByIdUseCase(
            AssetListingService assetListingService
    ) {
        return new GetAssetListingByIdUseCase(assetListingService);
    }

    @Bean
    public GetAssetByIdUseCase getAssetByIdUseCase(AssetService assetService) {
        return new GetAssetByIdUseCase(assetService);
    }

    @Bean
    public ListAssetsUseCase listAssetsUseCase(AssetService assetService) {
        return new ListAssetsUseCase(assetService);
    }

    @Bean
    public ListAssetBundlesUseCase listAssetBundlesUseCase(AssetBundleService assetBundleService) {
        return new ListAssetBundlesUseCase(assetBundleService);
    }

    @Bean
    public ListAssetBundleItemsUseCase listAssetBundleItemsUseCase(
            AssetBundleService assetBundleService
    ) {
        return new ListAssetBundleItemsUseCase(assetBundleService);
    }

    @Bean
    public CreateAssetBundleUseCase createAssetBundleUseCase(
            AssetBundleService assetBundleService
    ) {
        return new CreateAssetBundleUseCase(assetBundleService);
    }

    @Bean
    public ListActiveAssetListingsUseCase listActiveAssetListingsUseCase(
            AccountService accountService,
            AssetListingService assetListingService,
            JwtService jwtService
    ) {
        return new ListActiveAssetListingsUseCase(accountService, assetListingService, jwtService);
    }

    @Bean
    public ListAssetListingsByOwnerUseCase listAssetListingsByOwnerUseCase(
            AccountService accountService,
            AssetListingService assetListingService,
            JwtService jwtService
    ) {
        return new ListAssetListingsByOwnerUseCase(accountService,
                assetListingService, jwtService);
    }

    @Bean
    public ListAssetUnitsByOwnerUseCase listAssetUnitsByOwnerUseCase(
            AssetUnityService assetUnityService
    ) {
        return new ListAssetUnitsByOwnerUseCase(assetUnityService);
    }

    @Bean
    public AssetPurchaseService assetPurchaseService(
            ConnectionProvider connectionProvider,
            AssetWebhookNotifier webhookNotifier
    ) {
        return new AssetPurchaseService(connectionProvider, webhookNotifier);
    }

    @Bean
    public AssetPriceHistoryService assetPriceHistoryService(
            ConnectionProvider connectionProvider
    ) {
        return new AssetPriceHistoryService(connectionProvider);
    }

    @Bean
    public PurchaseAssetUseCase purchaseAssetUseCase(
            AccountService accountService,
            AssetListingService assetListingService,
            AssetPurchaseService assetPurchaseService,
            AssetPriceHistoryService assetPriceHistoryService,
            JwtService jwtService
    ) {
        return new PurchaseAssetUseCase(
                accountService,
                assetListingService,
                assetPurchaseService,
                assetPriceHistoryService,
                jwtService
        );
    }

    @Bean
    public ListAssetPriceHistoryByListingUseCase listAssetPriceHistoryByListingUseCase(
            AssetPriceHistoryService assetPriceHistoryService
    ) {
        return new ListAssetPriceHistoryByListingUseCase(assetPriceHistoryService);
    }

    @Bean
    public ListAssetPriceHistoryByAssetIdUseCase listAssetPriceHistoryByAssetIdUseCase(
            AssetPriceHistoryService assetPriceHistoryService
    ) {
        return new ListAssetPriceHistoryByAssetIdUseCase(assetPriceHistoryService);
    }

    @Bean
    public CreateAssetOfferUseCase createAssetOfferUseCase(
            AssetListingService assetListingService,
            AccountService accountService,
            JwtService jwtService
    ) {
        return new CreateAssetOfferUseCase(
                assetListingService,
                accountService,
                jwtService
        );
    }

    @Bean
    public CreateAssetUnityForAccountUseCase createAssetUnityForAccountUseCase(
            AssetUnityService assetUnityService,
            JwtService jwtService) {
        return new CreateAssetUnityForAccountUseCase(
                assetUnityService,
                jwtService
        );
    }

    @Bean
    public CancelAssetOfferUseCase cancelAssetOfferUseCase(
            AssetListingService assetListingService,
            AuthService authService
    ) {
        return new CancelAssetOfferUseCase(
                assetListingService,
                authService
        );
    }
}
