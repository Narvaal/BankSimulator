package br.com.ale.dao.asset;

import br.com.ale.domain.asset.AssetUnity;
import br.com.ale.dto.CreateAssetUnityRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AssetUnityDAO {
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

    private static AssetUnity mapRow(ResultSet rs) throws SQLException {
        return new AssetUnity(
                rs.getLong("id"),
                rs.getLong("asset_id"),
                rs.getLong("owner_account_id"),
                rs.getTimestamp("created_at").toInstant()
        );
    }

    public int updateOwner(Connection conn, long assetId, long ownerAccount) {

        String sql = """
                UPDATE asset_unit
                SET owner_account_id = ?
                WHERE id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, ownerAccount);
            stmt.setLong(2, assetId);

            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while updating asset unity owner " +
                            "[assetId=" + assetId + "]",
                    e
            );
        }
    }

    public List<AssetUnity> selectByOwnerAccount(Connection conn, long ownerAccountId) {
        String sql = """
                SELECT id,
                       asset_id,
                       owner_account_id,
                       created_at
                  FROM asset_unit
                 WHERE owner_account_id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, ownerAccountId);

            try (ResultSet rs = stmt.executeQuery()) {
                List<AssetUnity> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
                return result;
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
