package br.com.ale.service.webhook;

import br.com.ale.domain.asset.AssetPurchase;
import br.com.ale.domain.asset.AssetTransfer;
import br.com.ale.domain.asset.AssetUnity;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.client.RestTemplate;

public class AssetWebhookNotifier {

    private final String webhookUrl;
    private final boolean enabled;
    private final RestTemplate restTemplate = new RestTemplate();

    public AssetWebhookNotifier(String webhookUrl, boolean enabled) {
        this.webhookUrl = webhookUrl;
        this.enabled = enabled;
    }

    public void notifyAssetUnityCreated(AssetUnity unity) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("assetUnityId", unity.getId());
        data.put("assetId", unity.getAssetId());
        data.put("ownerAccountId", unity.getOwnerAccountId());
        send("asset.unity.created", data);
    }

    public void notifyAssetTransferCreated(AssetTransfer transfer) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("assetUnityId", transfer.getAssetUnityId());
        data.put("fromAccountId", transfer.getFromAccountId());
        data.put("toAccountId", transfer.getToAccountId());
        send("asset.transfer.created", data);
    }

    public void notifyAssetPurchaseCompleted(AssetPurchase purchase) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("listingId", purchase.getListingId());
        data.put("assetUnityId", purchase.getAssetUnityId());
        data.put("sellerAccountId", purchase.getSellerAccountId());
        data.put("buyerAccountId", purchase.getBuyerAccountId());
        data.put("price", purchase.getPrice());
        send("asset.purchase.completed", data);
    }

    private void send(String type, Map<String, Object> data) {
        if (!enabled || webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }

        WebhookEvent event = new WebhookEvent(
                UUID.randomUUID().toString(),
                type,
                Instant.now(),
                data
        );

        try {
            restTemplate.postForEntity(webhookUrl, event, Void.class);
        } catch (Exception ignored) {
            // Best-effort notification.
        }
    }

    private record WebhookEvent(
            String id,
            String type,
            Instant timestamp,
            Map<String, Object> data
    ) {
    }
}
