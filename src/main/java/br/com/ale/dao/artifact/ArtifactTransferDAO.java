package br.com.ale.dao.artifact;

import br.com.ale.domain.artifact.Artifact;
import br.com.ale.domain.artifact.ArtifactTransfer;
import br.com.ale.dto.CreateArtifactTransferRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class ArtifactTransferDAO {
    public ArtifactTransfer insert(Connection conn, CreateArtifactTransferRequest request) {

        String sql = """
                INSERT INTO artifact_transfer (artifact_unit_id, from_account_id, to_account_id)
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement stmt =
                     conn.prepareStatement(sql, new String[]{"id"})) {

            stmt.setLong(1, request.artifactUnitId());
            stmt.setLong(2, request.fromAccountId());
            stmt.setLong(3, request.toAccountId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new RuntimeException(
                        "Failed to insert artifact transfer [artifactUnitId=" + request.artifactUnitId() +
                                ", fromAccountId=" + request.fromAccountId() + ", " +
                                ", toAccountId=" + request.toAccountId() + "]"
                );
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new RuntimeException(
                            "Failed to retrieve artifact transfer id [artifactUnitId=" + request.artifactUnitId() +
                                    ", fromAccountId=" + request.fromAccountId() + ", " +
                                    ", toAccountId=" + request.toAccountId() + "]"
                    );
                }

                long artifactTransferId = rs.getLong("id");

                return selectById(conn, artifactTransferId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Artifact transfer inserted but not found [id=" + artifactTransferId + "]"
                                )
                        );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while inserting artifact transfer [artifactUnitId=" + request.artifactUnitId() +
                            ", fromAccountId=" + request.fromAccountId() + ", " +
                            ", toAccountId=" + request.toAccountId() + "]",
                    e
            );
        }
    }

    public Optional<ArtifactTransfer> selectById(Connection conn, long artifactTransferId) {

        String sql = """
                SELECT id,
                       artifact_unit_id,
                       from_account_id,
                       to_account_id,
                       created_at
                  FROM artifact_transfer
                 WHERE id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, artifactTransferId);

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
                            "[artifactId=" + artifactTransferId + "]",
                    e
            );
        }
    }

    private static ArtifactTransfer mapRow(ResultSet rs) throws SQLException {
        return new ArtifactTransfer(
                rs.getLong("id"),
                rs.getLong("artifact_unit_id"),
                rs.getLong("from_account_id"),
                rs.getLong("to_account_id"),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
