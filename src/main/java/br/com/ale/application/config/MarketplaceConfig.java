package br.com.ale.application.config;

import br.com.ale.application.marketplace.query.GetAssetByIdUseCase;
import br.com.ale.application.marketplace.query.GetAssetListingByIdUseCase;
import br.com.ale.application.marketplace.query.ListActiveAssetListingsUseCase;
import br.com.ale.application.marketplace.query.ListAssetsUseCase;
import br.com.ale.application.marketplace.query.ListAssetListingsByOwnerUseCase;
import br.com.ale.application.marketplace.query.ListAssetBundlesUseCase;
import br.com.ale.application.marketplace.query.ListAssetBundleItemsUseCase;
import br.com.ale.application.marketplace.query.ListAssetPriceHistoryByAssetIdUseCase;
import br.com.ale.application.marketplace.query.ListAssetPriceHistoryByListingUseCase;
import br.com.ale.application.marketplace.query.ListAssetUnitsByOwnerUseCase;
import br.com.ale.application.marketplace.usecase.CancelAssetOfferUseCase;
import br.com.ale.application.marketplace.usecase.CreateAssetBundleUseCase;
import br.com.ale.application.marketplace.usecase.CreateAssetOfferUseCase;
import br.com.ale.application.marketplace.usecase.CreateAssetUnityForAccountUseCase;
import br.com.ale.application.marketplace.usecase.PurchaseAssetUseCase;
import br.com.ale.infrastructure.db.ConnectionProvider;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.asset.AssetBundleService;
import br.com.ale.service.asset.AssetGenerationManager;
import br.com.ale.service.asset.AssetGenerationService;
import br.com.ale.service.asset.AssetService;
import br.com.ale.service.asset.AssetListingService;
import br.com.ale.service.asset.AssetUnityService;
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
            AssetListingService assetListingService
    ) {
        return new ListAssetListingsByOwnerUseCase(assetListingService);
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
            AssetUnityService assetUnityService,
            JwtService jwtService
    ) {
        return new CreateAssetOfferUseCase(
                assetListingService,
                assetUnityService,
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
            AccountService accountService,
            AssetListingService assetListingService,
            AssetUnityService assetUnityService,
            AuthService authService
    ) {
        return new CancelAssetOfferUseCase(
                accountService,
                assetListingService,
                assetUnityService,
                authService
        );
    }
}
