package br.com.ale.dao.asset;

import br.com.ale.domain.asset.AssetPriceHistory;
import br.com.ale.domain.asset.ReasonType;
import br.com.ale.dto.CreatePriceHistoryRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class AssetPriceHistoryDAO {
    public AssetPriceHistory insert(Connection conn, CreatePriceHistoryRequest request) {

        String sql = """
                INSERT INTO asset_price_history (asset_listing_id, asset_unity_id, old_price, new_price, changed_by_account_id, reason)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"id"})) {

            stmt.setLong(1, request.assetListingId());
            stmt.setLong(2, request.assetUnityId());
            stmt.setBigDecimal(3, request.oldPrice());
            stmt.setBigDecimal(4, request.newPrice());
            stmt.setLong(5, request.changedByAccountId());
            stmt.setString(6, request.reason().name());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new RuntimeException(
                        "Failed to insert asset price history [assetListingId=" + request.assetListingId() +
                                ", assetUnityId=" + request.assetUnityId() + "]"
                );
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new RuntimeException(
                            "Failed to retrieve asset price history id [assetListingId=" + request.assetListingId() +
                                    ", assetUnityId=" + request.assetUnityId() + "]"
                    );
                }

                long assetPriceHistoryId = rs.getLong("id");

                return selectById(conn, assetPriceHistoryId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Asset price history inserted but not found [id=" + assetPriceHistoryId + "]"
                                )
                        );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while inserting asset " +
                            "[assetListingId=" + request.assetListingId() +
                            ", assetUnityId=" + request.assetUnityId() + "]",
                    e
            );
        }
    }

    public Optional<AssetPriceHistory> selectById(Connection conn, long assetId) {

        String sql = """
                SELECT * FROM asset_price_history
                WHERE asset_listing_id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, assetId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(
                            mapRow(rs)
                    );
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while selecting asset price " +
                            "[assetId=" + assetId + "]",
                    e
            );
        }
    }

    private AssetPriceHistory mapRow(ResultSet rs) throws SQLException {
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
