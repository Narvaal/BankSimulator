package br.com.ale.service.webhook;

import br.com.ale.domain.artifact.ArtifactPurchase;
import br.com.ale.domain.artifact.ArtifactTransfer;
import br.com.ale.domain.artifact.ArtifactUnit;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.client.RestTemplate;

public class ArtifactWebhookNotifier {

  private final String webhookUrl;
  private final boolean enabled;
  private final RestTemplate restTemplate = new RestTemplate();

  public ArtifactWebhookNotifier(String webhookUrl, boolean enabled) {
    this.webhookUrl = webhookUrl;
    this.enabled = enabled;
  }

  public void notifyArtifactUnitCreated(ArtifactUnit unity) {
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("artifactUnitId", unity.getId());
    data.put("artifactId", unity.getArtifactId());
    data.put("ownerAccountId", unity.getOwnerAccountId());
    send("artifact.unity.created", data);
  }

  public void notifyArtifactTransferCreated(ArtifactTransfer transfer) {
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("artifactUnitId", transfer.getArtifactUnitId());
    data.put("fromAccountId", transfer.getFromAccountId());
    data.put("toAccountId", transfer.getToAccountId());
    send("artifact.transfer.created", data);
  }

  public void notifyArtifactPurchaseCompleted(ArtifactPurchase purchase) {
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("listingId", purchase.getListingId());
    data.put("artifactUnitId", purchase.getArtifactUnitId());
    data.put("sellerAccountId", purchase.getSellerAccountId());
    data.put("buyerAccountId", purchase.getBuyerAccountId());
    data.put("price", purchase.getPrice());
    send("artifact.purchase.completed", data);
  }

  private void send(String type, Map<String, Object> data) {
    if (!enabled || webhookUrl == null || webhookUrl.isBlank()) {
      return;
    }

    WebhookEvent event = new WebhookEvent(UUID.randomUUID().toString(), type, Instant.now(), data);

    try {
      restTemplate.postForEntity(webhookUrl, event, Void.class);
    } catch (Exception ignored) {
      // Best-effort notification.
    }
  }

  private record WebhookEvent(
      String id, String type, Instant timestamp, Map<String, Object> data) {}
}
