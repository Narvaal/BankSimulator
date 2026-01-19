package br.com.ale.dao.asset;

import br.com.ale.domain.asset.AssetPriceHistory;
import br.com.ale.domain.asset.ReasonType;
import br.com.ale.dto.CreatePriceHistoryRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

public class AssetPriceHistoryDAO {

    public AssetPriceHistory insert(Connection conn, CreatePriceHistoryRequest request) {

        String sql = """
                INSERT INTO asset_price_history
                    (asset_listing_id,
                     asset_unity_id,
                     old_price,
                     new_price,
                     changed_by_account_id,
                     reason)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt =
                     conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, request.assetListingId());
            stmt.setLong(2, request.assetUnityId());
            stmt.setBigDecimal(3, request.oldPrice());
            stmt.setBigDecimal(4, request.newPrice());
            stmt.setLong(5, request.changedByAccountId());
            stmt.setString(6, request.reason().name());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new RuntimeException(
                        "Failed to insert asset price history " +
                                "[assetListingId=" + request.assetListingId() +
                                ", assetUnityId=" + request.assetUnityId() + "]"
                );
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {

                if (!rs.next()) {
                    throw new RuntimeException(
                            "Failed to retrieve generated id for asset price history " +
                                    "[assetListingId=" + request.assetListingId() +
                                    ", assetUnityId=" + request.assetUnityId() + "]"
                    );
                }

                long id = rs.getLong(1);

                return new AssetPriceHistory(
                        id,
                        request.assetListingId(),
                        request.assetUnityId(),
                        request.oldPrice(),
                        request.newPrice(),
                        request.changedByAccountId(),
                        request.reason(),
                        Instant.now()
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while inserting asset price history " +
                            "[assetListingId=" + request.assetListingId() +
                            ", assetUnityId=" + request.assetUnityId() +
                            ", oldPrice=" + request.oldPrice() +
                            ", newPrice=" + request.newPrice() +
                            ", changedByAccountId=" + request.changedByAccountId() +
                            ", reason=" + request.reason() + "]",
                    e
            );
        }
    }
}
