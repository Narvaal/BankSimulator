package br.com.ale.service.marketplace;

import br.com.ale.dao.asset.AssetListingDAO;
import br.com.ale.dao.asset.AssetPriceHistoryDAO;
import br.com.ale.domain.asset.AssetListing;
import br.com.ale.domain.asset.AssetPriceHistory;
import br.com.ale.domain.asset.ReasonType;
import br.com.ale.dto.CreatePriceHistoryRequest;
import br.com.ale.infrastructure.db.ConnectionProvider;

import java.math.BigDecimal;
import java.sql.Connection;

public class AssetPriceHistoryService {

    private final ConnectionProvider connectionProvider;

    private final AssetListingDAO assetListingDAO = new AssetListingDAO();
    private final AssetPriceHistoryDAO assetPriceHistoryDAO =
            new AssetPriceHistoryDAO();

    public AssetPriceHistoryService(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public AssetPriceHistory registerPriceChange(
            long assetListingId,
            BigDecimal newPrice,
            long changedByAccountId,
            ReasonType reason
    ) {

        try (Connection conn = connectionProvider.getConnection()) {
            conn.setAutoCommit(false);

            try {

                AssetListing listing =
                        assetListingDAO
                                .selectById(conn, assetListingId)
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "AssetListing not found " +
                                                        "[assetListingId=" + assetListingId + "]"
                                        )
                                );

                BigDecimal oldPrice = listing.getPrice();

                AssetPriceHistory persisted =
                        assetPriceHistoryDAO.insert(
                                conn,
                                new CreatePriceHistoryRequest(
                                        listing.getId(),
                                        listing.getAssetUnityId(),
                                        oldPrice,
                                        newPrice,
                                        changedByAccountId,
                                        reason
                                )
                        );

                conn.commit();
                return persisted;

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while creating asset price history " +
                            "[assetListingId=" + assetListingId +
                            ", newPrice=" + newPrice +
                            ", changedByAccountId=" + changedByAccountId +
                            ", reason=" + reason + "]",
                    e
            );
        }
    }
}
