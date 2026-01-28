package br.com.ale.dao.asset;

import br.com.ale.domain.asset.AssetListing;
import br.com.ale.domain.asset.AssetListingStatus;
import br.com.ale.dto.CreateAssetListingRequest;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AssetListingDAO {
    public AssetListing insert(Connection conn, CreateAssetListingRequest request) {

        String sql = """
        INSERT INTO asset_listing (asset_unit_id, seller_account_id, price, status)
        VALUES (?, ?, ?, ?)
        """;

        try (PreparedStatement stmt =
                     conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, request.assetUnityId());
            stmt.setLong(2, request.sellerAccountId());
            stmt.setBigDecimal(3, request.price());
            stmt.setString(4, request.status().name());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new RuntimeException("Failed to insert asset listing");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {

                if (!rs.next()) {
                    throw new RuntimeException("Failed to retrieve generated asset listing id");
                }

                long assetListingId = rs.getLong(1);

                return selectById(conn, assetListingId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Asset listing inserted but not found [id=" + assetListingId + "]"
                                )
                        );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while inserting asset listing " +
                            "[assetUnitId=" + request.assetUnityId() +
                            ", sellerAccountId=" + request.sellerAccountId() +
                            ", price=" + request.price() +
                            ", status=" + request.status().name() + "]",
                    e
            );
        }
    }

    public Optional<AssetListing> selectById(Connection conn, long assetListingId) {

        String sql = """
                SELECT id,
                       asset_unit_id,
                       seller_account_id,
                       price,
                       status,
                       created_at,
                       updated_at
                  FROM asset_listing
                 WHERE id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, assetListingId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while selecting asset listing " +
                            "[assetListingId=" + assetListingId + "]",
                    e
            );
        }
    }

    public List<AssetListing> selectByStatus(Connection conn, AssetListingStatus status) {

        String sql = """
                SELECT id,
                       asset_unit_id,
                       seller_account_id,
                       price,
                       status,
                       created_at,
                       updated_at
                  FROM asset_listing
                 WHERE status = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());

            try (ResultSet rs = stmt.executeQuery()) {

                List<AssetListing> listings = new ArrayList<>();

                while (rs.next()) {
                    listings.add(mapRow(rs));
                }

                return listings;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while selecting asset listings " +
                            "[status=" + status.name() + "]",
                    e
            );
        }
    }

    private static AssetListing mapRow(ResultSet rs) throws SQLException {

        Timestamp updatedAt = rs.getTimestamp("updated_at");

        return new AssetListing(
                rs.getLong("id"),
                rs.getLong("asset_unit_id"),
                rs.getLong("seller_account_id"),
                rs.getBigDecimal("price"),
                AssetListingStatus.valueOf(
                        rs.getString("status").toUpperCase()
                ),
                rs.getTimestamp("created_at").toInstant(),
                updatedAt != null ? updatedAt.toInstant() : null
        );
    }

    public int updateStatus(Connection conn, long assetId, AssetListingStatus status) {

        String sql = """
                UPDATE asset_listing
                SET status = ?
                WHERE id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setLong(2, assetId);

            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while updating asset listing status " +
                            "[assetId=" + assetId + ", "
                            + "[status=" + status.name() + "]",
                    e
            );
        }
    }


    public int updatePrice(Connection conn, long assetListingId, BigDecimal price) {

        String sql = """
                UPDATE asset_listing
                SET price = ?
                WHERE id = ?;
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, price);
            stmt.setLong(2, assetListingId);

            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while updating asset listing price " +
                            "[price=" + price + ", "
                            + "[assetListingId=" + assetListingId + "]",
                    e
            );
        }
    }

    public Optional<AssetListing> selectActiveByAssetUnitId(Connection conn, long assetUnityId) {
        String sql = """
                SELECT id,
                       asset_unit_id,
                       seller_account_id,
                       price,
                       status,
                       created_at,
                       updated_at
                  FROM asset_listing
                 WHERE asset_unit_id = ?
                   AND status = 'ACTIVE'
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
                    "Database error while selecting active asset listing " +
                            "[assetUnityId=" + assetUnityId + "]",
                    e
            );
        }
    }

    public Optional<AssetListing> selectByIdForUpdate(Connection conn, long assetListingId) {

        String sql = """
                SELECT * FROM asset_listing
                WHERE id = ?
                FOR UPDATE;
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, assetListingId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }

                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while selecting asset listings " +
                            "[id=" + assetListingId + "]",
                    e
            );
        }
    }

    public Optional<AssetListing> selectByAssetUnitId(Connection conn, long assetUnityId) {
        String sql = """
                SELECT id,
                       asset_unit_id,
                       seller_account_id,
                       price,
                       status,
                       created_at,
                       updated_at
                  FROM asset_listing
                 WHERE asset_unit_id = ?
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
                    "Database error while selecting asset listings " +
                            "[assetUnityId=" + assetUnityId + "]",
                    e
            );
        }
    }

    public List<AssetListing> selectByOwnerAccount(Connection conn, long ownerAccountId, AssetListingStatus status) {
        String sql = """
                SELECT id,
                       asset_unit_id,
                       seller_account_id,
                       price,
                       status,
                       created_at,
                       updated_at
                  FROM asset_listing
                 WHERE seller_account_id = ? AND status = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, ownerAccountId);
            stmt.setString(2, status.name());

            try (ResultSet rs = stmt.executeQuery()) {

                List<AssetListing> assetListings = new ArrayList<>();
                while (rs.next()) {
                    assetListings.add(mapRow(rs));
                }
                return assetListings;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while selecting asset listings " +
                            "[ownerAccountId=" + ownerAccountId + ", "
                            + "[status=" + status.name() + "]",
                    e
            );
        }
    }
}
