package br.com.ale.service.artifact;

import static org.junit.jupiter.api.Assertions.*;

import br.com.ale.domain.artifact.Artifact;
import br.com.ale.domain.artifact.ArtifactListing;
import br.com.ale.domain.artifact.ArtifactListingStatus;
import br.com.ale.domain.artifact.ArtifactUnit;
import br.com.ale.domain.artifact.ReasonType;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.dto.ArtifactListingFilter;
import br.com.ale.dto.CreateArtifactListingRequest;
import br.com.ale.dto.CreateArtifactRequest;
import br.com.ale.dto.CreateArtifactUnitRequest;
import br.com.ale.service.webhook.ArtifactWebhookNotifier;
import br.com.ale.support.DbTestSupport;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ArtifactListingFlowTest extends DbTestSupport {

  private ArtifactService artifactService;
  private ArtifactUnitService unitService;
  private ArtifactListingService listingService;

  private long clientId;
  private long accountId;
  private ArtifactUnit unit;

  @BeforeEach
  void seedMarketplace() {
    artifactService = new ArtifactService(provider);
    unitService = new ArtifactUnitService(provider, new ArtifactWebhookNotifier("", false));
    listingService = new ArtifactListingService(provider);

    clientId = insertClient("Seller", "seller@test.com");
    accountId = insertAccount(clientId, "500-000-001");

    Artifact artifact =
        artifactService.createAsset(
            new CreateArtifactRequest(Map.of("name", "Vision Pro", "rarity", "Epic"), 5));
    unit =
        unitService.createArtifactUnit(new CreateArtifactUnitRequest(artifact.getId(), accountId));
  }

  private ArtifactListing listUnit(BigDecimal price) {
    return listingService.createArtifactOffer(
        new CreateArtifactListingRequest(
            unit.getId(), accountId, price, ArtifactListingStatus.ACTIVE));
  }

  @Test
  void createOfferShouldActivateListing() {
    ArtifactListing listing = listUnit(new BigDecimal("100.00"));

    assertEquals(ArtifactListingStatus.ACTIVE, listing.getStatus());
    assertEquals(listing.getId(), listingService.selectById(listing.getId()).getId());
    assertEquals(listing.getId(), listingService.selectByArtifactUnitId(unit.getId()).getId());
    assertEquals(1, listingService.selectByStatus(ArtifactListingStatus.ACTIVE).size());
  }

  @Test
  void changePriceShouldRecordHistoryAndRejectSamePrice() {
    ArtifactListing listing = listUnit(new BigDecimal("100.00"));

    ArtifactListing updated =
        listingService.changePrice(
            listing.getId(), new BigDecimal("80.00"), accountId, ReasonType.UPDATED);
    assertEquals(0, updated.getPrice().compareTo(new BigDecimal("80.00")));

    assertThrows(
        RuntimeException.class,
        () ->
            listingService.changePrice(
                listing.getId(), new BigDecimal("80.00"), accountId, ReasonType.UPDATED));
    assertThrows(
        RuntimeException.class,
        () ->
            listingService.changePrice(
                9999L, new BigDecimal("80.00"), accountId, ReasonType.UPDATED));
  }

  @Test
  void cancelListingShouldRequireOwnerAndActiveStatus() {
    ArtifactListing listing = listUnit(new BigDecimal("100.00"));

    RuntimeException notOwner =
        assertThrows(
            RuntimeException.class, () -> listingService.cancelListing(listing.getId(), 9999L));
    assertTrue(notOwner.getCause() instanceof UnauthorizedOperationException);

    listingService.cancelListing(listing.getId(), clientId);
    assertEquals(
        ArtifactListingStatus.CANCELED, listingService.selectById(listing.getId()).getStatus());

    assertThrows(
        RuntimeException.class, () -> listingService.cancelListing(listing.getId(), clientId));
  }

  @Test
  void updateStatusShouldPersist() {
    ArtifactListing listing = listUnit(new BigDecimal("100.00"));

    assertEquals(1, listingService.updateStatus(listing.getId(), ArtifactListingStatus.SOLD));
    assertEquals(
        ArtifactListingStatus.SOLD, listingService.selectById(listing.getId()).getStatus());
    assertEquals(0, listingService.updateStatus(9999L, ArtifactListingStatus.SOLD));
  }

  @Test
  void activeListingsShouldSupportFiltersAndExcludeOwnAccount() {
    listUnit(new BigDecimal("100.00"));

    var publicView =
        listingService.selectActiveByActiveStatus(-1L, ArtifactListingFilter.empty(), 0, 10);
    assertEquals(1, publicView.items().size());

    var ownExcluded =
        listingService.selectActiveByActiveStatus(accountId, ArtifactListingFilter.empty(), 0, 10);
    assertEquals(0, ownExcluded.items().size());

    var searchHit =
        listingService.selectActiveByActiveStatus(
            -1L, new ArtifactListingFilter(null, "vision", "price_asc", null, null), 0, 10);
    assertEquals(1, searchHit.items().size());

    var priceMiss =
        listingService.selectActiveByActiveStatus(
            -1L,
            new ArtifactListingFilter(null, null, "price_desc", new BigDecimal("200"), null),
            0,
            10);
    assertEquals(0, priceMiss.items().size());

    var ownerView = listingService.selectByOwnerAccount(accountId, 0, 10);
    assertEquals(1, ownerView.items().size());
  }
}
