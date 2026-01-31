package br.com.ale.dao.asset;

import br.com.ale.domain.asset.AssetBundle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class AssetBundleDAO {

    public AssetBundle insert(Connection conn, String identifier) {
        String sql = """
                INSERT INTO asset_bundle (identifier)
                VALUES (?)
                """;

        try (PreparedStatement stmt =
                     conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, identifier);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException(
                        "Failed to insert asset bundle [identifier=" + identifier + "]"
                );
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new RuntimeException(
                            "Failed to retrieve asset bundle id [identifier=" + identifier + "]"
                    );
                }

                long id = rs.getLong(1);
                return new AssetBundle(id, identifier, Instant.now());
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while inserting asset bundle " +
                            "[identifier=" + identifier + "]",
                    e
            );
        }
    }

    public List<AssetBundle> selectAll(Connection conn) {
        String sql = """
                SELECT id,
                       identifier,
                       created_at
                  FROM asset_bundle
                 ORDER BY created_at DESC
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                List<AssetBundle> bundles = new ArrayList<>();
                while (rs.next()) {
                    bundles.add(new AssetBundle(
                            rs.getLong("id"),
                            rs.getString("identifier"),
                            rs.getTimestamp("created_at").toInstant()
                    ));
                }
                return bundles;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error while selecting bundles", e);
        }
    }
}
