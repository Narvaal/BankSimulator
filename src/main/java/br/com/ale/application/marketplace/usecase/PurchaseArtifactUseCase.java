package br.com.ale.application.marketplace.usecase;

import br.com.ale.application.marketplace.command.PurchaseArtifactCommand;
import br.com.ale.domain.account.Account;
import br.com.ale.domain.artifact.*;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.dto.CreateArtifactPurchaseRequest;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.artifact.ArtifactListingService;
import br.com.ale.service.auth.JwtService;
import br.com.ale.service.marketplace.ArtifactPriceHistoryService;
import br.com.ale.service.marketplace.ArtifactPurchaseService;

public class PurchaseArtifactUseCase {

  private final AccountService accountService;
  private final ArtifactListingService artifactListingService;
  private final ArtifactPurchaseService artifactPurchaseService;
  private final ArtifactPriceHistoryService artifactPriceHistoryService;
  private final JwtService jwtService;

  public PurchaseArtifactUseCase(
      AccountService accountService,
      ArtifactListingService artifactListingService,
      ArtifactPurchaseService artifactPurchaseService,
      ArtifactPriceHistoryService artifactPriceHistoryService,
      JwtService jwtService) {
    this.accountService = accountService;
    this.artifactListingService = artifactListingService;
    this.artifactPurchaseService = artifactPurchaseService;
    this.artifactPriceHistoryService = artifactPriceHistoryService;
    this.jwtService = jwtService;
  }

  public ArtifactPurchase execute(PurchaseArtifactCommand command) {

    if (!jwtService.isTokenValid(command.token())) {
      throw new UnauthorizedOperationException("Invalid or expired token");
    }

    long clientId = jwtService.extractClientId(command.token());

    Account authenticated =
        accountService
            .getAccountByClientId(clientId)
            .orElseThrow(() -> new UnauthorizedOperationException("Account not found"));

    ArtifactListing listing = artifactListingService.selectById(command.listingId());

    if (listing.getSellerAccountId() == authenticated.getId()) {
      throw new UnauthorizedOperationException("Seller cannot buy own artifact");
    }

    accountService.transfer(
        authenticated.getId(), listing.getSellerAccountId(), listing.getPrice());

    ArtifactPurchase purchase =
        artifactPurchaseService.purchase(
            new CreateArtifactPurchaseRequest(listing.getId(), authenticated.getId()));

    artifactPriceHistoryService.registerPriceChange(
        listing.getId(), listing.getPrice(), authenticated.getId(), ReasonType.SOLD);

    return purchase;
  }
}
