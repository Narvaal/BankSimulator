package br.com.ale.application.api;

import br.com.ale.application.marketplace.command.CreateArtifactBundleCommand;
import br.com.ale.application.marketplace.command.CreateArtifactUnitForAccountCommand;
import br.com.ale.application.marketplace.query.GetArtifactByIdUseCase;
import br.com.ale.application.marketplace.query.ListArtifactBundleItemsUseCase;
import br.com.ale.application.marketplace.query.ListArtifactBundlesUseCase;
import br.com.ale.application.marketplace.query.ListArtifactPriceHistoryByArtifactIdUseCase;
import br.com.ale.application.marketplace.query.ListArtifactsUseCase;
import br.com.ale.application.marketplace.usecase.CreateArtifactBundleUseCase;
import br.com.ale.application.marketplace.usecase.CreateArtifactUnitForAccountUseCase;
import br.com.ale.domain.artifact.Artifact;
import br.com.ale.domain.artifact.ArtifactPriceHistory;
import br.com.ale.domain.artifact.ArtifactUnit;
import br.com.ale.dto.ArtifactBundleItemResponse;
import br.com.ale.dto.ArtifactBundleResponse;
import br.com.ale.dto.ArtifactSummaryResponse;
import br.com.ale.dto.CreateArtifactBundleApiRequest;
import br.com.ale.dto.CreateArtifactUnitApiRequest;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/artifacts")
public class ArtifactQueryController {

  private final GetArtifactByIdUseCase getArtifactByIdUseCase;
  private final ListArtifactPriceHistoryByArtifactIdUseCase
      listArtifactPriceHistoryByArtifactIdUseCase;
  private final ListArtifactsUseCase listArtifactsUseCase;
  private final ListArtifactBundlesUseCase listArtifactBundlesUseCase;
  private final ListArtifactBundleItemsUseCase listArtifactBundleItemsUseCase;
  private final CreateArtifactUnitForAccountUseCase createArtifactUnitForAccountUseCase;
  private final CreateArtifactBundleUseCase createArtifactBundleUseCase;

  public ArtifactQueryController(
      GetArtifactByIdUseCase getArtifactByIdUseCase,
      ListArtifactPriceHistoryByArtifactIdUseCase listArtifactPriceHistoryByArtifactIdUseCase,
      ListArtifactsUseCase listArtifactsUseCase,
      ListArtifactBundlesUseCase listArtifactBundlesUseCase,
      ListArtifactBundleItemsUseCase listArtifactBundleItemsUseCase,
      CreateArtifactUnitForAccountUseCase createArtifactUnitForAccountUseCase,
      CreateArtifactBundleUseCase createArtifactBundleUseCase) {
    this.getArtifactByIdUseCase = getArtifactByIdUseCase;
    this.listArtifactPriceHistoryByArtifactIdUseCase = listArtifactPriceHistoryByArtifactIdUseCase;
    this.listArtifactsUseCase = listArtifactsUseCase;
    this.listArtifactBundlesUseCase = listArtifactBundlesUseCase;
    this.listArtifactBundleItemsUseCase = listArtifactBundleItemsUseCase;
    this.createArtifactUnitForAccountUseCase = createArtifactUnitForAccountUseCase;
    this.createArtifactBundleUseCase = createArtifactBundleUseCase;
  }

  @GetMapping("/{id}")
  public Artifact getById(@PathVariable("id") long id) {
    return getArtifactByIdUseCase.execute(id);
  }

  @PostMapping("/{id}/units")
  public ArtifactUnit createUnity(
      @PathVariable("id") long artifactId,
      @RequestBody CreateArtifactUnitApiRequest request,
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
    String token = extractToken(authorization, request.token());
    return createArtifactUnitForAccountUseCase.execute(
        new CreateArtifactUnitForAccountCommand(artifactId, request.ownerAccountId(), token));
  }

  @GetMapping
  public List<ArtifactSummaryResponse> list() {
    return listArtifactsUseCase.execute();
  }

  @GetMapping("/bundles")
  public List<ArtifactBundleResponse> listBundles(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return listArtifactBundlesUseCase.execute(page, size);
  }

  @PostMapping("/bundles")
  public ArtifactBundleResponse createBundle(@RequestBody CreateArtifactBundleApiRequest request) {
    return createArtifactBundleUseCase.execute(
        new CreateArtifactBundleCommand(request.assets(), request.identifier()));
  }

  @GetMapping("/bundles/{id}/items")
  public List<ArtifactBundleItemResponse> listBundleItems(
      @PathVariable("id") long id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return listArtifactBundleItemsUseCase.execute(id, page, size);
  }

  @GetMapping("/{id}/price-history")
  public List<ArtifactPriceHistory> priceHistory(@PathVariable("id") long id) {
    return listArtifactPriceHistoryByArtifactIdUseCase.execute(id);
  }

  private String extractToken(String authorization, String fallback) {
    if (authorization != null && authorization.startsWith("Bearer ")) {
      return authorization.substring("Bearer ".length()).trim();
    }
    return fallback;
  }
}
