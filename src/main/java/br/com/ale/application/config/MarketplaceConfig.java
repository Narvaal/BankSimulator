package br.com.ale.application.config;

import br.com.ale.application.marketplace.query.*;
import br.com.ale.application.marketplace.usecase.*;
import br.com.ale.infrastructure.db.ConnectionProvider;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.artifact.*;
import br.com.ale.service.artifact.ArtifactTransferService;
import br.com.ale.service.auth.JwtService;
import br.com.ale.service.marketplace.ArtifactPriceHistoryService;
import br.com.ale.service.marketplace.ArtifactPurchaseService;
import br.com.ale.service.webhook.ArtifactWebhookNotifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MarketplaceConfig {

  @Bean
  public ArtifactListingService artifactListingService(ConnectionProvider connectionProvider) {
    return new ArtifactListingService(connectionProvider);
  }

  @Bean
  public ArtifactService assetService(ConnectionProvider connectionProvider) {
    return new ArtifactService(connectionProvider);
  }

  @Bean
  public ArtifactGenerationService assetGenerationService() {
    return new ArtifactGenerationService();
  }

  @Bean
  public ArtifactBundleService artifactBundleService(ConnectionProvider connectionProvider) {
    return new ArtifactBundleService(connectionProvider);
  }

  @Bean
  public ArtifactWebhookNotifier artifactWebhookNotifier(
      @Value("${webhook.artifact.url:}") String webhookUrl,
      @Value("${webhook.artifact.enabled:false}") boolean enabled) {
    return new ArtifactWebhookNotifier(webhookUrl, enabled);
  }

  @Bean
  public ArtifactGenerationManager assetGenerationManager(
      ArtifactGenerationService assetGenerationService,
      ArtifactBundleService artifactBundleService) {
    return new ArtifactGenerationManager(assetGenerationService, artifactBundleService);
  }

  @Bean
  public ArtifactUnitService artifactUnitService(
      ConnectionProvider connectionProvider, ArtifactWebhookNotifier webhookNotifier) {
    return new ArtifactUnitService(connectionProvider, webhookNotifier);
  }

  @Bean
  public GetArtifactListingByIdUseCase getArtifactListingByIdUseCase(
      ArtifactListingService artifactListingService) {
    return new GetArtifactListingByIdUseCase(artifactListingService);
  }

  @Bean
  public GetArtifactByIdUseCase getArtifactByIdUseCase(ArtifactService assetService) {
    return new GetArtifactByIdUseCase(assetService);
  }

  @Bean
  public ListArtifactsUseCase listArtifactsUseCase(ArtifactService assetService) {
    return new ListArtifactsUseCase(assetService);
  }

  @Bean
  public ListArtifactBundlesUseCase listArtifactBundlesUseCase(
      ArtifactBundleService artifactBundleService) {
    return new ListArtifactBundlesUseCase(artifactBundleService);
  }

  @Bean
  public ListArtifactBundleItemsUseCase listArtifactBundleItemsUseCase(
      ArtifactBundleService artifactBundleService) {
    return new ListArtifactBundleItemsUseCase(artifactBundleService);
  }

  @Bean
  public CreateArtifactBundleUseCase createArtifactBundleUseCase(
      ArtifactBundleService artifactBundleService) {
    return new CreateArtifactBundleUseCase(artifactBundleService);
  }

  @Bean
  public ListActiveArtifactListingsUseCase listActiveArtifactListingsUseCase(
      AccountService accountService,
      ArtifactListingService artifactListingService,
      JwtService jwtService) {
    return new ListActiveArtifactListingsUseCase(
        accountService, artifactListingService, jwtService);
  }

  @Bean
  public ListArtifactListingsByOwnerUseCase listArtifactListingsByOwnerUseCase(
      AccountService accountService,
      ArtifactListingService artifactListingService,
      JwtService jwtService) {
    return new ListArtifactListingsByOwnerUseCase(
        accountService, artifactListingService, jwtService);
  }

  @Bean
  public ListArtifactUnitsByOwnerUseCase listArtifactUnitsByOwnerUseCase(
      ArtifactUnitService artifactUnitService) {
    return new ListArtifactUnitsByOwnerUseCase(artifactUnitService);
  }

  @Bean
  public ArtifactPurchaseService artifactPurchaseService(
      ConnectionProvider connectionProvider, ArtifactWebhookNotifier webhookNotifier) {
    return new ArtifactPurchaseService(connectionProvider, webhookNotifier);
  }

  @Bean
  public ArtifactPriceHistoryService artifactPriceHistoryService(
      ConnectionProvider connectionProvider) {
    return new ArtifactPriceHistoryService(connectionProvider);
  }

  @Bean
  public PurchaseArtifactUseCase purchaseArtifactUseCase(
      AccountService accountService,
      ArtifactListingService artifactListingService,
      ArtifactPurchaseService artifactPurchaseService,
      ArtifactPriceHistoryService artifactPriceHistoryService,
      JwtService jwtService) {
    return new PurchaseArtifactUseCase(
        accountService,
        artifactListingService,
        artifactPurchaseService,
        artifactPriceHistoryService,
        jwtService);
  }

  @Bean
  public ListArtifactPriceHistoryByListingUseCase listArtifactPriceHistoryByListingUseCase(
      ArtifactPriceHistoryService artifactPriceHistoryService) {
    return new ListArtifactPriceHistoryByListingUseCase(artifactPriceHistoryService);
  }

  @Bean
  public ListArtifactPriceHistoryByArtifactIdUseCase listArtifactPriceHistoryByArtifactIdUseCase(
      ArtifactPriceHistoryService artifactPriceHistoryService) {
    return new ListArtifactPriceHistoryByArtifactIdUseCase(artifactPriceHistoryService);
  }

  @Bean
  public CreateArtifactOfferUseCase createArtifactOfferUseCase(
      ArtifactListingService artifactListingService,
      AccountService accountService,
      JwtService jwtService) {
    return new CreateArtifactOfferUseCase(artifactListingService, accountService, jwtService);
  }

  @Bean
  public CreateArtifactUnitForAccountUseCase createArtifactUnitForAccountUseCase(
      ArtifactUnitService artifactUnitService, JwtService jwtService) {
    return new CreateArtifactUnitForAccountUseCase(artifactUnitService, jwtService);
  }

  @Bean
  public CancelArtifactOfferUseCase cancelArtifactOfferUseCase(
      ArtifactListingService artifactListingService, JwtService jwtService) {
    return new CancelArtifactOfferUseCase(artifactListingService, jwtService);
  }

  @Bean
  public ArtifactTransferService artifactTransferService(
      ConnectionProvider connectionProvider, ArtifactWebhookNotifier artifactWebhookNotifier) {
    return new ArtifactTransferService(connectionProvider, artifactWebhookNotifier);
  }

  @Bean
  public ListArtifactTransferLogUseCase listArtifactTransferLogUseCase(
      ArtifactTransferService artifactTransferService) {
    return new ListArtifactTransferLogUseCase(artifactTransferService);
  }

  @Bean
  public GetArtifactUnitByIdUseCase getArtifactUnitByIdUseCase(
      ArtifactUnitService artifactUnitService,
      ArtifactService assetService,
      ArtifactPriceHistoryService artifactPriceHistoryService,
      ArtifactTransferService artifactTransferService) {
    return new GetArtifactUnitByIdUseCase(
        artifactUnitService, assetService, artifactPriceHistoryService, artifactTransferService);
  }
}
