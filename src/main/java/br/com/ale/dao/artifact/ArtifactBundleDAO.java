package br.com.ale.dao.artifact;

import br.com.ale.domain.artifact.ArtifactBundle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ArtifactBundleDAO {

  public ArtifactBundle insert(Connection conn, String identifier) {
    String sql =
        """
                INSERT INTO artifact_bundle (identifier)
                VALUES (?)
                """;

    try (PreparedStatement stmt =
        conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

      stmt.setString(1, identifier);

      int rowsAffected = stmt.executeUpdate();
      if (rowsAffected == 0) {
        throw new RuntimeException(
            "Failed to insert artifact bundle [identifier=" + identifier + "]");
      }

      try (ResultSet rs = stmt.getGeneratedKeys()) {
        if (!rs.next()) {
          throw new RuntimeException(
              "Failed to retrieve artifact bundle id [identifier=" + identifier + "]");
        }

        long id = rs.getLong(1);
        return new ArtifactBundle(id, identifier, Instant.now());
      }

    } catch (SQLException e) {
      throw new RuntimeException(
          "Database error while inserting artifact bundle " + "[identifier=" + identifier + "]", e);
    }
  }

  public List<ArtifactBundle> selectAll(Connection conn, int page, int size) {

    String sql =
        """
        SELECT id,
               identifier,
               created_at
          FROM artifact_bundle
         ORDER BY created_at DESC
         LIMIT ? OFFSET ?
        """;

    int offset = page * size;

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, size);
      stmt.setInt(2, offset);

      try (ResultSet rs = stmt.executeQuery()) {

        List<ArtifactBundle> bundles = new ArrayList<>();

        while (rs.next()) {
          bundles.add(
              new ArtifactBundle(
                  rs.getLong("id"),
                  rs.getString("identifier"),
                  rs.getTimestamp("created_at").toInstant()));
        }

        return bundles;
      }

    } catch (SQLException e) {
      throw new RuntimeException(
          "Database error while selecting bundles [page=" + page + ", size=" + size + "]", e);
    }
  }
}
