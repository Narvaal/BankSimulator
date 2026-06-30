package br.com.ale.dao.artifact;

import br.com.ale.domain.artifact.ArtifactUnit;
import br.com.ale.domain.artifact.ArtifactUnitStatus;
import br.com.ale.dto.ArtifactUnitPageView;
import br.com.ale.dto.ArtifactUnitView;
import br.com.ale.dto.CreateArtifactUnitRequest;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ArtifactUnitDAO {
    private static Instant getInstantOrNull(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts != null ? ts.toInstant() : null;
    }

    private static ArtifactUnit mapRow(ResultSet rs) throws SQLException {
        return new ArtifactUnit(
                rs.getLong("id"),
                rs.getLong("artifact_id"),
                rs.getLong("owner_account_id"),
                ArtifactUnitStatus.valueOf(rs.getString("status")),
                getInstantOrNull(rs, "locked_at"),
                getInstantOrNull(rs, "created_at")
        );
    }

    public ArtifactUnit insert(Connection conn, CreateArtifactUnitRequest request) {
        String sql = """
                INSERT INTO artifact_unit (artifact_id, owner_account_id)
                VALUES (?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"id"})) {

            stmt.setLong(1, request.artifactId());
            stmt.setLong(2, request.ownerAccountId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new RuntimeException(
                        "Failed to insert artifactUnit [artifactId=" + request.artifactId() +
                                ", ownerAccountId=" + request.ownerAccountId() + "]"
                );
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new RuntimeException(
                            "Failed to retrieve artifactUnit id [artifactId=" + request.artifactId() +
                                    ", ownerAccountId=" + request.ownerAccountId() + "]"
                    );
                }

                long artifactUnitId = rs.getLong("id");

                return selectById(conn, artifactUnitId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "ArtifactUnit inserted but not found [id=" + artifactUnitId + "]"
                                )
                        );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while inserting artifactUnit " +
                            "[artifactId=" + request.artifactId() +
                            ", ownerAccountId=" + request.ownerAccountId() + "]",
                    e
            );
        }
    }

    public Optional<ArtifactUnit> selectById(Connection conn, long artifactUnitId) {

        String sql = """
                SELECT id,
                       artifact_id,
                       owner_account_id,
                       status,
                       locked_at,
                       created_at
                  FROM artifact_unit
                 WHERE id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, artifactUnitId);

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
                    "Database error while selecting artifactUnit " +
                            "[artifactUnitId=" + artifactUnitId + "]",
                    e
            );
        }
    }

    public void updateOwner(Connection conn, long artifactId, long ownerAccount) {

        String sql = """
                UPDATE artifact_unit
                SET owner_account_id = ?
                WHERE id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, ownerAccount);
            stmt.setLong(2, artifactId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while updating artifact unity owner " +
                            "[artifactId=" + artifactId + "]",
                    e
            );
        }
    }

    public void updateStatus(Connection conn, long artifactUnitId) {

        String sql = """
                UPDATE artifact_unit
                SET status = 'AVAILABLE'
                WHERE id = ?
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, artifactUnitId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while updating artifact unity status " +
                            "[artifactUnitId=" + artifactUnitId + ", status=AVAILABLE" + "]",
                    e
            );
        }
    }

    public Optional<ArtifactUnit> selectByIdForUpdate(Connection conn, long id) {

        String sql = """
                    SELECT id,
                        artifact_id,
                        owner_account_id,
                        status,
                        locked_at,
                        created_at
                    FROM artifact_unit
                    WHERE id = ?
                    FOR UPDATE
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                return Optional.of(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error locking artifactUnit id=" + id, e);
        }
    }

    public boolean tryUpdateToMarket(Connection conn, long artifactUnitId, long accountId) {

        String sql = """
                    UPDATE artifact_unit
                    SET status = 'IN_MARKET'
                    WHERE id = ?
                      AND owner_account_id = ?
                      AND status = 'AVAILABLE'
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, artifactUnitId);
            stmt.setLong(2, accountId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error updating artifact unit to market", e);
        }
    }

    public boolean tryTransferOwnership(
            Connection conn,
            long id,
            long sellerAccountId,
            long buyerAccountId
    ) {

        String sql = """
                UPDATE artifact_unit
                   SET owner_account_id = ?,
                       status = 'AVAILABLE'
                 WHERE id = ?
                   AND owner_account_id = ?
                   AND status = 'IN_MARKET'
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, buyerAccountId);
            stmt.setLong(2, id);
            stmt.setLong(3, sellerAccountId);

            return stmt.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to transfer artifact unity [id=" + id +
                            ", seller=" + sellerAccountId +
                            ", buyer=" + buyerAccountId + "]",
                    e
            );
        }
    }

    public ArtifactUnitPageView selectByOwnerAccount(
            Connection conn,
            long ownerAccountId,
            int page,
            int pageSize
    ) {

        String sql = """
                SELECT
                    u.id AS artifact_unit_id,
                    u.created_at,
                    a.id AS artifact_id,
                    JSON_VALUE(a.metadata, '$.name') AS artifact_name,
                    COUNT(*) OVER() AS total_items
                FROM artifact_unit u
                JOIN artifact a ON a.id = u.artifact_id
                WHERE u.owner_account_id = ?
                  AND u.status = 'AVAILABLE'
                ORDER BY u.created_at DESC
                LIMIT ?
                OFFSET ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            int offset = page * pageSize;

            stmt.setLong(1, ownerAccountId);
            stmt.setInt(2, pageSize);
            stmt.setInt(3, offset);

            try (ResultSet rs = stmt.executeQuery()) {

                List<ArtifactUnitView> items = new ArrayList<>();

                long totalItems = 0;

                while (rs.next()) {

                    if (totalItems == 0) {
                        totalItems = rs.getLong("total_items");
                    }

                    items.add(
                            new ArtifactUnitView(
                                    rs.getLong("artifact_id"),
                                    rs.getLong("artifact_unit_id"),
                                    rs.getString("artifact_name"),
                                    getInstantOrNull(rs, "created_at")
                            )
                    );
                }

                int totalPages = (int) Math.ceil((double) totalItems / pageSize);

                return new ArtifactUnitPageView(
                        items,
                        page,
                        pageSize,
                        totalPages,
                        totalItems
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Database error while selecting artifact unity by owner " +
                            "[ownerAccountId=" + ownerAccountId + "]",
                    e
            );
        }
    }
}
