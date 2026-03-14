package br.com.ale.dao.asset;

import br.com.ale.dto.AssetBundleItemResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AssetBundleItemDAO {

    public void insertItems(Connection conn, long bundleId, List<Long> assetIds) {
        String sql = """
                INSERT INTO asset_bundle_item (bundle_id, asset_id)
                VALUES (?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Long assetId : assetIds) {
                stmt.setLong(1, bundleId);
                stmt.setLong(2, assetId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while inserting asset bundle items " +
                            "[bundleId=" + bundleId + "]",
                    e
            );
        }
    }

    public List<AssetBundleItemResponse> selectItemsByBundleId(
            Connection conn,
            long bundleId,
            int page,
            int size
    ) {

        String sql = """
            SELECT a.id,
                   a.text,
                   a.total_supply,
                   a.created_at
              FROM asset_bundle_item abi
              JOIN asset a
                ON a.id = abi.asset_id
             WHERE abi.bundle_id = ?
             ORDER BY a.created_at ASC
             LIMIT ? OFFSET ?
            """;

        int offset = page * size;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, bundleId);
            stmt.setInt(2, size);
            stmt.setInt(3, offset);

            try (ResultSet rs = stmt.executeQuery()) {

                List<AssetBundleItemResponse> items = new ArrayList<>();

                while (rs.next()) {
                    items.add(new AssetBundleItemResponse(
                            rs.getLong("id"),
                            rs.getString("text"),
                            rs.getInt("total_supply"),
                            rs.getTimestamp("created_at").toInstant()
                    ));
                }

                return items;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while selecting bundle items " +
                            "[bundleId=" + bundleId + ", page=" + page + ", size=" + size + "]",
                    e
            );
        }
    }
}
