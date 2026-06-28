package br.com.ale.dao.artifact;

import br.com.ale.domain.artifact.ArtifactTransfer;
import br.com.ale.dto.ArtifactTransferLogPageView;
import br.com.ale.dto.ArtifactTransferLogView;
import br.com.ale.dto.ArtifactUnitTransferView;
import br.com.ale.dto.CreateArtifactTransferRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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

    public ArtifactTransferLogPageView selectPublicFeed(Connection conn, int page, int pageSize) {

        String sql = """
                WITH ranked_transfers AS (
                    SELECT
                        t.id,
                        t.artifact_unit_id,
                        t.from_account_id,
                        t.to_account_id,
                        t.created_at,
                        (ROW_NUMBER() OVER (PARTITION BY t.artifact_unit_id ORDER BY t.id ASC) - 1) AS transfer_rank
                    FROM artifact_transfer t
                ),
                ranked_prices AS (
                    SELECT
                        p.artifact_unit_id,
                        p.new_price,
                        (ROW_NUMBER() OVER (PARTITION BY p.artifact_unit_id ORDER BY p.id ASC) - 1) AS price_rank
                    FROM artifact_price_history p
                    WHERE p.reason = 'SOLD'
                )
                SELECT
                    rt.id,
                    a.text       AS artifact_text,
                    rt.artifact_unit_id,
                    rp.new_price AS sale_price,
                    rt.from_account_id,
                    rt.to_account_id,
                    rt.created_at,
                    COUNT(*) OVER () AS total_items
                FROM ranked_transfers rt
                JOIN artifact_unit au ON au.id = rt.artifact_unit_id
                JOIN artifact a       ON a.id  = au.artifact_id
                LEFT JOIN ranked_prices rp
                    ON rp.artifact_unit_id = rt.artifact_unit_id
                   AND rp.price_rank       = rt.transfer_rank
                ORDER BY rt.created_at DESC
                LIMIT ? OFFSET ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pageSize);
            stmt.setInt(2, page * pageSize);

            try (ResultSet rs = stmt.executeQuery()) {

                List<ArtifactTransferLogView> items = new ArrayList<>();
                long totalItems = 0;

                while (rs.next()) {
                    if (totalItems == 0) totalItems = rs.getLong("total_items");
                    items.add(new ArtifactTransferLogView(
                            rs.getLong("id"),
                            rs.getString("artifact_text"),
                            rs.getLong("artifact_unit_id"),
                            rs.getBigDecimal("sale_price"),
                            rs.getLong("from_account_id"),
                            rs.getLong("to_account_id"),
                            rs.getTimestamp("created_at").toInstant()
                    ));
                }

                int totalPages = (int) Math.ceil((double) totalItems / pageSize);
                return new ArtifactTransferLogPageView(items, page, pageSize, totalPages, totalItems);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Database error while selecting artifact transfer feed", e);
        }
    }

    public List<ArtifactUnitTransferView> selectByUnitId(Connection conn, long artifactUnitId) {

        String sql = """
                WITH ranked_transfers AS (
                    SELECT t.id, t.from_account_id, t.to_account_id, t.created_at,
                           (ROW_NUMBER() OVER (ORDER BY t.id ASC) - 1) AS transfer_rank
                    FROM artifact_transfer t
                    WHERE t.artifact_unit_id = ?
                ),
                ranked_prices AS (
                    SELECT p.new_price,
                           (ROW_NUMBER() OVER (ORDER BY p.id ASC) - 1) AS price_rank
                    FROM artifact_price_history p
                    WHERE p.artifact_unit_id = ?
                      AND p.reason = 'SOLD'
                )
                SELECT rt.id, rt.from_account_id, rt.to_account_id, rt.created_at,
                       rp.new_price AS sale_price
                FROM ranked_transfers rt
                LEFT JOIN ranked_prices rp ON rp.price_rank = rt.transfer_rank
                ORDER BY rt.created_at ASC
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, artifactUnitId);
            stmt.setLong(2, artifactUnitId);

            try (ResultSet rs = stmt.executeQuery()) {

                List<ArtifactUnitTransferView> result = new ArrayList<>();

                while (rs.next()) {
                    result.add(new ArtifactUnitTransferView(
                            rs.getLong("id"),
                            rs.getLong("from_account_id"),
                            rs.getLong("to_account_id"),
                            rs.getBigDecimal("sale_price"),
                            rs.getTimestamp("created_at").toInstant()
                    ));
                }

                return result;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while selecting transfer chain for unit [artifactUnitId=" + artifactUnitId + "]", e
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
