package br.com.ale.dao.asset;

import br.com.ale.domain.asset.Asset;
import br.com.ale.domain.asset.AssetTransfer;
import br.com.ale.dto.CreateAssetTransferRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class AssetTransferDAO {
    public AssetTransfer insert(Connection conn, CreateAssetTransferRequest request) {

        String sql = """
                INSERT INTO asset_transfer (asset_unit_id, from_account_id, to_account_id)
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement stmt =
                     conn.prepareStatement(sql, new String[]{"id"})) {

            stmt.setLong(1, request.assetUnityId());
            stmt.setLong(2, request.fromAccountId());
            stmt.setLong(3, request.toAccountId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new RuntimeException(
                        "Failed to insert asset transfer [assetUnityId=" + request.assetUnityId() +
                                ", fromAccountId=" + request.fromAccountId() + ", " +
                                ", toAccountId=" + request.toAccountId() + "]"
                );
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new RuntimeException(
                            "Failed to retrieve asset transfer id [assetUnityId=" + request.assetUnityId() +
                                    ", fromAccountId=" + request.fromAccountId() + ", " +
                                    ", toAccountId=" + request.toAccountId() + "]"
                    );
                }

                long assetTransferId = rs.getLong("id");

                return selectById(conn, assetTransferId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Asset transfer inserted but not found [id=" + assetTransferId + "]"
                                )
                        );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while inserting asset transfer [assetUnityId=" + request.assetUnityId() +
                            ", fromAccountId=" + request.fromAccountId() + ", " +
                            ", toAccountId=" + request.toAccountId() + "]",
                    e
            );
        }
    }

    public Optional<AssetTransfer> selectById(Connection conn, long assetTransferId) {

        String sql = """
                SELECT id,
                       asset_unit_id,
                       from_account_id,
                       to_account_id,
                       created_at
                  FROM asset_transfer
                 WHERE id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, assetTransferId);

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
                    "Database error while selecting asset " +
                            "[assetId=" + assetTransferId + "]",
                    e
            );
        }
    }

    private static AssetTransfer mapRow(ResultSet rs) throws SQLException {
        return new AssetTransfer(
                rs.getLong("id"),
                rs.getLong("asset_unit_id"),
                rs.getLong("from_account_id"),
                rs.getLong("to_account_id"),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
