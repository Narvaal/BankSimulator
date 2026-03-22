package br.com.ale.dao.asset;

import br.com.ale.domain.asset.AssetUnity;
import br.com.ale.domain.asset.AssetUnityStatus;
import br.com.ale.dto.AssetUnityPageView;
import br.com.ale.dto.AssetUnityView;
import br.com.ale.dto.CreateAssetUnityRequest;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AssetUnityDAO {
    private static Instant getInstantOrNull(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts != null ? ts.toInstant() : null;
    }

    private static AssetUnity mapRow(ResultSet rs) throws SQLException {
        return new AssetUnity(
                rs.getLong("id"),
                rs.getLong("asset_id"),
                rs.getLong("owner_account_id"),
                AssetUnityStatus.valueOf(rs.getString("status")),
                getInstantOrNull(rs, "locked_at"),
                getInstantOrNull(rs, "created_at")
        );
    }

    public AssetUnity insert(Connection conn, CreateAssetUnityRequest request) {
        String sql = """
                INSERT INTO asset_unit (asset_id, owner_account_id)
                VALUES (?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"id"})) {

            stmt.setLong(1, request.assetId());
            stmt.setLong(2, request.ownerAccountId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new RuntimeException(
                        "Failed to insert assetUnity [assetId=" + request.assetId() +
                                ", ownerAccountId=" + request.ownerAccountId() + "]"
                );
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new RuntimeException(
                            "Failed to retrieve assetUnity id [assetId=" + request.assetId() +
                                    ", ownerAccountId=" + request.ownerAccountId() + "]"
                    );
                }

                long assetUnityId = rs.getLong("id");

                return selectById(conn, assetUnityId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "AssetUnity inserted but not found [id=" + assetUnityId + "]"
                                )
                        );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while inserting assetUnity " +
                            "[assetId=" + request.assetId() +
                            ", ownerAccountId=" + request.ownerAccountId() + "]",
                    e
            );
        }
    }

    public Optional<AssetUnity> selectById(Connection conn, long assetUnityId) {

        String sql = """
                SELECT id,
                       asset_id,
                       owner_account_id,
                       status,
                       locked_at,
                       created_at
                  FROM asset_unit
                 WHERE id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, assetUnityId);

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
                    "Database error while selecting assetUnity " +
                            "[assetUnityId=" + assetUnityId + "]",
                    e
            );
        }
    }

    public void updateOwner(Connection conn, long assetId, long ownerAccount) {

        String sql = """
                UPDATE asset_unit
                SET owner_account_id = ?
                WHERE id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, ownerAccount);
            stmt.setLong(2, assetId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while updating asset unity owner " +
                            "[assetId=" + assetId + "]",
                    e
            );
        }
    }

    public void updateStatus(Connection conn, long assetUnitId, AssetUnityStatus status) {

        String sql = """
                UPDATE asset_unit
                SET status = ?
                WHERE id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setLong(2, assetUnitId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while updating asset unity status " +
                            "[assetUnitId=" + assetUnitId + ", status=" + status + "]",
                    e
            );
        }
    }

    public Optional<AssetUnity> selectByIdForUpdate(Connection conn, long id) {

        String sql = """
                    SELECT id,
                        asset_id,
                        owner_account_id,
                        status,
                        locked_at,
                        created_at
                    FROM asset_unit
                    WHERE id = ?
                    FOR UPDATE
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                return Optional.of(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error locking assetUnity id=" + id, e);
        }
    }

    public boolean tryUpdateToMarket(Connection conn, long assetUnitId, long accountId) {

        String sql = """
                    UPDATE asset_unit
                    SET status = 'IN_MARKET'
                    WHERE id = ?
                      AND owner_account_id = ?
                      AND status = 'AVAILABLE'
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, assetUnitId);
            stmt.setLong(2, accountId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error updating asset unit to market", e);
        }
    }

    public boolean tryTransferOwnership(
            Connection conn,
            long id,
            long sellerAccountId,
            long buyerAccountId
    ) {

        String sql = """
                UPDATE asset_unit
                   SET owner_account_id = ?,
                       status = 'AVAILABLE'
                 WHERE id = ?
                   AND owner_account_id = ?
                   AND status = 'IN_MARKET'
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, buyerAccountId);
            stmt.setLong(2, id);
            stmt.setLong(3, sellerAccountId);

            return stmt.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to transfer asset unity [id=" + id +
                            ", seller=" + sellerAccountId +
                            ", buyer=" + buyerAccountId + "]",
                    e
            );
        }
    }

    public AssetUnityPageView selectByOwnerAccount(
            Connection conn,
            long ownerAccountId,
            int page,
            int pageSize
    ) {

        String sql = """
                SELECT
                    u.id AS asset_unity_id,
                    u.created_at,
                    a.id AS asset_id,
                    a.text AS asset_text,
                    COUNT(*) OVER() AS total_items
                FROM asset_unit u
                JOIN asset a ON a.id = u.asset_id
                WHERE u.owner_account_id = ?
                  AND u.status = 'AVAILABLE'
                ORDER BY u.created_at DESC
                LIMIT ?
                OFFSET ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            int offset = page * pageSize;

            stmt.setLong(1, ownerAccountId);
            stmt.setInt(2, pageSize);
            stmt.setInt(3, offset);

            try (ResultSet rs = stmt.executeQuery()) {

                List<AssetUnityView> items = new ArrayList<>();

                long totalItems = 0;

                while (rs.next()) {

                    if (totalItems == 0) {
                        totalItems = rs.getLong("total_items");
                    }

                    items.add(
                            new AssetUnityView(
                                    rs.getLong("asset_id"),
                                    rs.getLong("asset_unity_id"),
                                    rs.getString("asset_text"),
                                    getInstantOrNull(rs, "created_at")
                            )
                    );
                }

                int totalPages = (int) Math.ceil((double) totalItems / pageSize);

                return new AssetUnityPageView(
                        items,
                        page,
                        pageSize,
                        totalPages,
                        totalItems
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Database error while selecting asset unity by owner " +
                            "[ownerAccountId=" + ownerAccountId + "]",
                    e
            );
        }
    }
}
