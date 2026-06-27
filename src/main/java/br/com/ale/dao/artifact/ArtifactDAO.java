package br.com.ale.dao.artifact;

import br.com.ale.domain.artifact.Artifact;
import br.com.ale.dto.ArtifactSummaryResponse;
import br.com.ale.dto.CreateArtifactRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ArtifactDAO {

    public Artifact insert(Connection conn, CreateArtifactRequest request) {

        String sql = """
                INSERT INTO artifact (text, total_supply)
                VALUES (?, ?)
                """;

        try (PreparedStatement stmt =
                     conn.prepareStatement(sql, new String[]{"id"})) {

            stmt.setString(1, request.text());
            stmt.setInt(2, request.totalSupply());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new RuntimeException(
                        "Failed to insert artifact [text=" + request.text() +
                                ", totalSupply=" + request.totalSupply() + "]"
                );
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new RuntimeException(
                            "Failed to retrieve artifact id [text=" + request.text() +
                                    ", totalSupply=" + request.totalSupply() + "]"
                    );
                }

                long artifactId = rs.getLong("id");

                return selectById(conn, artifactId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Artifact inserted but not found [id=" + artifactId + "]"
                                )
                        );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while inserting artifact " +
                            "[text=" + request.text() +
                            ", totalSupply=" + request.totalSupply() + "]",
                    e
            );
        }
    }

    public Optional<Artifact> selectById(Connection conn, long artifactId) {

        String sql = """
                SELECT id,
                       text,
                       total_supply,
                       created_at
                  FROM artifact
                 WHERE id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, artifactId);

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
                    "Database error while selecting artifact " +
                            "[artifactId=" + artifactId + "]",
                    e
            );
        }
    }

    private static Artifact mapRow(ResultSet rs) throws SQLException {
        return new Artifact(
                rs.getLong("id"),
                rs.getString("text"),
                rs.getInt("total_supply"),
                rs.getTimestamp("created_at").toInstant()
        );
    }

    public List<ArtifactSummaryResponse> selectAllSummaries(Connection conn) {
        String sql = """
                SELECT text,
                       total_supply,
                       created_at
                  FROM artifact
                 ORDER BY created_at DESC
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                List<ArtifactSummaryResponse> assets = new ArrayList<>();
                while (rs.next()) {
                    assets.add(new ArtifactSummaryResponse(
                            rs.getString("text"),
                            rs.getInt("total_supply"),
                            rs.getTimestamp("created_at").toInstant()
                    ));
                }
                return assets;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error while selecting assets", e);
        }
    }

    public int updateTotalSupply(Connection conn, long artifactId, int supplyUsed) {

        String sql = """
                UPDATE artifact
                SET total_supply = total_supply - ?
                WHERE id = ? AND total_supply >= ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, supplyUsed);
            stmt.setLong(2, artifactId);
            stmt.setInt(3, supplyUsed);

            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while updating artifact supply " +
                            "[artifactId=" + artifactId + "]",
                    e
            );
        }
    }
}
