package br.com.ale.service.marketplace;

import br.com.ale.dao.AccountDAO;
import br.com.ale.dao.asset.AssetListingDAO;
import br.com.ale.dao.asset.AssetTransferDAO;
import br.com.ale.dao.asset.AssetUnityDAO;
import br.com.ale.domain.asset.*;
import br.com.ale.dto.CreateAssetPurchaseRequest;
import br.com.ale.dto.CreateAssetTransferRequest;
import br.com.ale.infrastructure.db.ConnectionProvider;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.webhook.AssetWebhookNotifier;

import java.sql.Connection;

public class AssetPurchaseService {

    private final ConnectionProvider connectionProvider;
    private final AssetWebhookNotifier webhookNotifier;
    private final AssetUnityDAO assetUnityDAO = new AssetUnityDAO();
    private final AssetListingDAO assetListingDAO = new AssetListingDAO();
    private final AssetTransferDAO assetTransferDAO = new AssetTransferDAO();

    public AssetPurchaseService(
            ConnectionProvider connectionProvider,
            AssetWebhookNotifier webhookNotifier
    ) {
        this.connectionProvider = connectionProvider;
        this.webhookNotifier = webhookNotifier;
    }

    public AssetPurchase purchase(CreateAssetPurchaseRequest request) {

        try (Connection conn = connectionProvider.getConnection()) {
            conn.setAutoCommit(false);

            try {
                AssetListing listing =
                        assetListingDAO.selectByIdForUpdate(
                                conn,
                                request.listingId()
                        ).orElseThrow(() ->
                                new RuntimeException("Listing not found")
                        );

                if (listing.getStatus() != AssetListingStatus.ACTIVE)
                    throw new RuntimeException("Listing already sold");

                if (listing.getSellerAccountId() == request.buyerAccountId())
                    throw new RuntimeException("Buyer cannot be seller");

                AssetUnity unity =
                        assetUnityDAO.selectByIdForUpdate(
                                conn,
                                listing.getAssetUnityId()
                        ).orElseThrow(() ->
                                new RuntimeException("AssetUnity not found")
                        );

                if (unity.getOwnerAccountId() != listing.getSellerAccountId())
                    throw new RuntimeException("Asset already purchased by another client");

                assetListingDAO.updateStatus(
                        conn,
                        listing.getId(),
                        AssetListingStatus.SOLD
                );

                AssetTransfer transfer = assetTransferDAO.insert(
                        conn,
                        new CreateAssetTransferRequest(
                                unity.getId(),
                                listing.getSellerAccountId(),
                                request.buyerAccountId()
                        )
                );

                boolean transferred = assetUnityDAO.tryTransferOwnership(
                        conn,
                        unity.getId(),
                        listing.getSellerAccountId(),
                        request.buyerAccountId()
                );

                if (!transferred)
                    throw new RuntimeException("Asset not transferred");

                conn.commit();

                AssetPurchase purchase = new AssetPurchase(
                        listing.getId(),
                        unity.getId(),
                        listing.getSellerAccountId(),
                        request.buyerAccountId(),
                        listing.getPrice()
                );

                webhookNotifier.notifyAssetTransferCreated(transfer);
                webhookNotifier.notifyAssetPurchaseCompleted(purchase);

                return purchase;

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException("Error while purchasing asset", e);
        }
    }
}
