package br.com.ale.dao.asset;

import br.com.ale.domain.asset.Asset;
import br.com.ale.dto.CreateAssetRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class AssetDAO {

    public Asset insert(Connection conn, CreateAssetRequest request) {

        String sql = """
                INSERT INTO asset (text, total_supply)
                VALUES (?, ?)
                """;

        try (PreparedStatement stmt =
                     conn.prepareStatement(sql, new String[]{"id"})) {

            stmt.setString(1, request.text());
            stmt.setInt(2, request.totalSupply());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new RuntimeException(
                        "Failed to insert asset [text=" + request.text() +
                                ", totalSupply=" + request.totalSupply() + "]"
                );
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new RuntimeException(
                            "Failed to retrieve asset id [text=" + request.text() +
                                    ", totalSupply=" + request.totalSupply() + "]"
                    );
                }

                long assetId = rs.getLong("id");

                return selectById(conn, assetId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Asset inserted but not found [id=" + assetId + "]"
                                )
                        );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while inserting asset " +
                            "[text=" + request.text() +
                            ", totalSupply=" + request.totalSupply() + "]",
                    e
            );
        }
    }

    public Optional<Asset> selectById(Connection conn, long assetId) {

        String sql = """
                SELECT id,
                       text,
                       total_supply,
                       created_at
                  FROM asset
                 WHERE id = ?
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
                    "Database error while selecting asset " +
                            "[assetId=" + assetId + "]",
                    e
            );
        }
    }

    private static Asset mapRow(ResultSet rs) throws SQLException {
        return new Asset(
                rs.getLong("id"),
                rs.getString("text"),
                rs.getInt("total_supply"),
                rs.getTimestamp("created_at").toInstant()
        );
    }

    public int updateTotalSupply(Connection conn, long assetId, int supplyUsed) {

        String sql = """
                UPDATE asset
                SET total_supply = total_supply - ?
                WHERE id = ? AND total_supply >= ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, assetId);
            stmt.setInt(2, supplyUsed);
            stmt.setInt(3, supplyUsed);

            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while updating asset supply " +
                            "[assetId=" + assetId + "]",
                    e
            );
        }
    }
}
