package br.com.ale.service.marketplace;

import br.com.ale.dao.asset.AssetListingDAO;
import br.com.ale.dao.asset.AssetTransferDAO;
import br.com.ale.dao.asset.AssetUnityDAO;
import br.com.ale.domain.asset.AssetListing;
import br.com.ale.domain.asset.AssetListingStatus;
import br.com.ale.domain.asset.AssetPurchase;
import br.com.ale.domain.asset.AssetUnity;
import br.com.ale.dto.CreateAssetPurchaseRequest;
import br.com.ale.dto.CreateAssetTransferRequest;
import br.com.ale.infrastructure.db.ConnectionProvider;
import br.com.ale.service.crypto.PrivateKeyStorage;

import java.sql.Connection;

public class AssetPurchaseService {

    private final ConnectionProvider connectionProvider;
    private final AssetUnityDAO assetUnityDAO = new AssetUnityDAO();
    private final AssetListingDAO assetListingDAO = new AssetListingDAO();
    private final AssetTransferDAO assetTransferDAO = new AssetTransferDAO();

    public AssetPurchaseService(
            ConnectionProvider connectionProvider
    ) {
        this.connectionProvider = connectionProvider;
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
                    throw new RuntimeException("Listing not active");

                if (listing.getSellerAccountId() == request.buyerAccountId())
                    throw new RuntimeException("Buyer cannot be seller");

                AssetUnity unity =
                        assetUnityDAO.selectById(
                                conn,
                                listing.getAssetUnityId()
                        ).orElseThrow(() ->
                                new RuntimeException("AssetUnity not found")
                        );

                assetListingDAO.updateStatus(
                        conn,
                        listing.getId(),
                        AssetListingStatus.SOLD
                );

                assetTransferDAO.insert(
                        conn,
                        new CreateAssetTransferRequest(
                                unity.getId(),
                                listing.getSellerAccountId(),
                                request.buyerAccountId()
                        )
                );

                assetUnityDAO.updateOwner(
                        conn,
                        unity.getId(),
                        request.buyerAccountId()
                );

                conn.commit();

                return new AssetPurchase(
                        listing.getId(),
                        unity.getId(),
                        listing.getSellerAccountId(),
                        request.buyerAccountId(),
                        listing.getPrice()
                );

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException("Error while purchasing asset", e);
        }
    }
}
