package br.com.ale.dao.asset;

import br.com.ale.domain.asset.AssetPriceHistory;
import br.com.ale.domain.asset.ReasonType;
import br.com.ale.dto.CreatePriceHistoryRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public List<AssetPriceHistory> selectByAssetListingId(Connection conn, long assetListingId) {
        String sql = """
                SELECT id,
                       asset_listing_id,
                       asset_unity_id,
                       old_price,
                       new_price,
                       changed_by_account_id,
                       reason,
                       created_at
                  FROM asset_price_history
                 WHERE asset_listing_id = ?
                 ORDER BY created_at ASC
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, assetListingId);

            try (ResultSet rs = stmt.executeQuery()) {
                List<AssetPriceHistory> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while selecting asset price history " +
                            "[assetListingId=" + assetListingId + "]",
                    e
            );
        }
    }

    public Optional<AssetPriceHistory> selectLatestByAssetUnityId(
            Connection conn,
            long assetUnityId
    ) {
        String sql = """
                SELECT id,
                       asset_listing_id,
                       asset_unity_id,
                       old_price,
                       new_price,
                       changed_by_account_id,
                       reason,
                       created_at
                  FROM asset_price_history
                 WHERE asset_unity_id = ?
                 ORDER BY created_at DESC
                 LIMIT 1
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, assetUnityId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while selecting latest asset price history " +
                            "[assetUnityId=" + assetUnityId + "]",
                    e
            );
        }
    }

    public List<AssetPriceHistory> selectByAssetId(Connection conn, long assetId) {
        String sql = """
                SELECT aph.id,
                       aph.asset_listing_id,
                       aph.asset_unity_id,
                       aph.old_price,
                       aph.new_price,
                       aph.changed_by_account_id,
                       aph.reason,
                       aph.created_at
                  FROM asset_price_history aph
                  JOIN asset_unit au
                    ON au.id = aph.asset_unity_id
                 WHERE au.asset_id = ?
                 ORDER BY aph.created_at ASC
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, assetId);

            try (ResultSet rs = stmt.executeQuery()) {
                List<AssetPriceHistory> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while selecting asset price history " +
                            "[assetId=" + assetId + "]",
                    e
            );
        }
    }

    private static AssetPriceHistory mapRow(ResultSet rs) throws SQLException {
        return new AssetPriceHistory(
                rs.getLong("id"),
                rs.getLong("asset_listing_id"),
                rs.getLong("asset_unity_id"),
                rs.getBigDecimal("old_price"),
                rs.getBigDecimal("new_price"),
                rs.getLong("changed_by_account_id"),
                ReasonType.valueOf(rs.getString("reason")),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
