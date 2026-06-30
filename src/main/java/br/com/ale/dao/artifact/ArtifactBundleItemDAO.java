package br.com.ale.dao.artifact;

import br.com.ale.dto.ArtifactBundleItemResponse;
import br.com.ale.infrastructure.json.JsonUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ArtifactBundleItemDAO {

    public void insertItems(Connection conn, long bundleId, List<Long> artifactIds) {
        String sql = """
                INSERT INTO artifact_bundle_item (bundle_id, artifact_id)
                VALUES (?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Long artifactId : artifactIds) {
                stmt.setLong(1, bundleId);
                stmt.setLong(2, artifactId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while inserting artifact bundle items " +
                            "[bundleId=" + bundleId + "]",
                    e
            );
        }
    }

    public List<ArtifactBundleItemResponse> selectItemsByBundleId(
            Connection conn,
            long bundleId,
            int page,
            int size
    ) {

        String sql = """
            SELECT a.id,
                   a.metadata,
                   a.total_supply,
                   a.created_at
              FROM artifact_bundle_item abi
              JOIN artifact a
                ON a.id = abi.artifact_id
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

                List<ArtifactBundleItemResponse> items = new ArrayList<>();

                while (rs.next()) {
                    items.add(new ArtifactBundleItemResponse(
                            rs.getLong("id"),
                            JsonUtils.fromJson(rs.getString("metadata")),
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
