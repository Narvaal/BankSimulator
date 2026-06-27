package br.com.ale.dao.artifact;

import br.com.ale.domain.artifact.ArtifactPriceHistory;
import br.com.ale.domain.artifact.ReasonType;
import br.com.ale.dto.CreatePriceHistoryRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ArtifactPriceHistoryDAO {

    public ArtifactPriceHistory insert(Connection conn, CreatePriceHistoryRequest request) {

        String sql = """
                INSERT INTO artifact_price_history
                    (artifact_listing_id,
                     artifact_unit_id,
                     old_price,
                     new_price,
                     changed_by_account_id,
                     reason)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt =
                     conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, request.artifactListingId());
            stmt.setLong(2, request.artifactUnitId());
            stmt.setBigDecimal(3, request.oldPrice());
            stmt.setBigDecimal(4, request.newPrice());
            stmt.setLong(5, request.changedByAccountId());
            stmt.setString(6, request.reason().name());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new RuntimeException(
                        "Failed to insert artifact price history " +
                                "[artifactListingId=" + request.artifactListingId() +
                                ", artifactUnitId=" + request.artifactUnitId() + "]"
                );
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {

                if (!rs.next()) {
                    throw new RuntimeException(
                            "Failed to retrieve generated id for artifact price history " +
                                    "[artifactListingId=" + request.artifactListingId() +
                                    ", artifactUnitId=" + request.artifactUnitId() + "]"
                    );
                }

                long id = rs.getLong(1);

                return new ArtifactPriceHistory(
                        id,
                        request.artifactListingId(),
                        request.artifactUnitId(),
                        request.oldPrice(),
                        request.newPrice(),
                        request.changedByAccountId(),
                        request.reason(),
                        Instant.now()
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while inserting artifact price history " +
                            "[artifactListingId=" + request.artifactListingId() +
                            ", artifactUnitId=" + request.artifactUnitId() +
                            ", oldPrice=" + request.oldPrice() +
                            ", newPrice=" + request.newPrice() +
                            ", changedByAccountId=" + request.changedByAccountId() +
                            ", reason=" + request.reason() + "]",
                    e
            );
        }
    }

    public List<ArtifactPriceHistory> selectByArtifactListingId(Connection conn, long artifactListingId) {
        String sql = """
                SELECT id,
                       artifact_listing_id,
                       artifact_unit_id,
                       old_price,
                       new_price,
                       changed_by_account_id,
                       reason,
                       created_at
                  FROM artifact_price_history
                 WHERE artifact_listing_id = ?
                 ORDER BY created_at ASC
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, artifactListingId);

            try (ResultSet rs = stmt.executeQuery()) {
                List<ArtifactPriceHistory> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while selecting artifact price history " +
                            "[artifactListingId=" + artifactListingId + "]",
                    e
            );
        }
    }

    public Optional<ArtifactPriceHistory> selectLatestByArtifactUnitId(
            Connection conn,
            long artifactUnitId
    ) {
        String sql = """
                SELECT id,
                       artifact_listing_id,
                       artifact_unit_id,
                       old_price,
                       new_price,
                       changed_by_account_id,
                       reason,
                       created_at
                  FROM artifact_price_history
                 WHERE artifact_unit_id = ?
                 ORDER BY created_at DESC
                 LIMIT 1
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, artifactUnitId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while selecting latest artifact price history " +
                            "[artifactUnitId=" + artifactUnitId + "]",
                    e
            );
        }
    }

    public List<ArtifactPriceHistory> selectByArtifactUnitId(Connection conn, long artifactUnitId) {

        String sql = """
            SELECT aph.id,
                   aph.artifact_listing_id,
                   aph.artifact_unit_id,
                   aph.old_price,
                   aph.new_price,
                   aph.changed_by_account_id,
                   aph.reason,
                   aph.created_at
              FROM artifact_price_history aph
             WHERE aph.artifact_unit_id = ?
             ORDER BY aph.created_at ASC
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, artifactUnitId);

            try (ResultSet rs = stmt.executeQuery()) {

                List<ArtifactPriceHistory> result = new ArrayList<>();

                while (rs.next()) {
                    result.add(mapRow(rs));
                }

                return result;
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Database error while selecting artifact price history " +
                            "[artifactUnitId=" + artifactUnitId + "]",
                    e
            );
        }
    }

    private static ArtifactPriceHistory mapRow(ResultSet rs) throws SQLException {
        return new ArtifactPriceHistory(
                rs.getLong("id"),
                rs.getLong("artifact_listing_id"),
                rs.getLong("artifact_unit_id"),
                rs.getBigDecimal("old_price"),
                rs.getBigDecimal("new_price"),
                rs.getLong("changed_by_account_id"),
                ReasonType.valueOf(rs.getString("reason")),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
