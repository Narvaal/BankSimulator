package br.com.ale.application.api;

import br.com.ale.application.marketplace.command.PurchaseArtifactCommand;
import br.com.ale.application.marketplace.usecase.PurchaseArtifactUseCase;
import br.com.ale.domain.artifact.ArtifactPurchase;
import br.com.ale.infrastructure.auth.AuthCookieService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/artifact-listings")
public class ArtifactPurchaseController {

  private final PurchaseArtifactUseCase purchaseArtifactUseCase;
  private final AuthCookieService authCookieService;

  public ArtifactPurchaseController(
      PurchaseArtifactUseCase purchaseArtifactUseCase, AuthCookieService authCookieService) {
    this.purchaseArtifactUseCase = purchaseArtifactUseCase;
    this.authCookieService = authCookieService;
  }

  @PostMapping("/{id}/purchase")
  public ArtifactPurchase purchase(
      @PathVariable("id") long listingId, HttpServletRequest httpRequest) {
    String token = authCookieService.extractToken(httpRequest);
    PurchaseArtifactCommand command = new PurchaseArtifactCommand(listingId, token);
    return purchaseArtifactUseCase.execute(command);
  }
}
