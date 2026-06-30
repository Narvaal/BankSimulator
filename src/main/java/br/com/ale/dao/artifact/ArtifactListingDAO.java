package br.com.ale.dao.artifact;

import br.com.ale.domain.artifact.ArtifactListing;
import br.com.ale.domain.artifact.ArtifactListingStatus;
import br.com.ale.dto.ArtifactListingFilter;
import br.com.ale.dto.ArtifactListingPageView;
import br.com.ale.dto.ArtifactListingView;
import br.com.ale.dto.CreateArtifactListingRequest;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

public class ArtifactListingDAO {
    private static ArtifactListing mapRow(ResultSet rs) throws SQLException {

        Timestamp updatedAt = rs.getTimestamp("updated_at");

        return new ArtifactListing(
                rs.getLong("id"),
                rs.getLong("artifact_unit_id"),
                rs.getLong("seller_account_id"),
                rs.getBigDecimal("price"),
                ArtifactListingStatus.valueOf(
                        rs.getString("status").toUpperCase()
                ),
                rs.getTimestamp("created_at").toInstant(),
                updatedAt != null ? updatedAt.toInstant() : null
        );
    }

    public ArtifactListing insert(Connection conn, CreateArtifactListingRequest request) {

        String sql = """
                INSERT INTO artifact_listing (artifact_unit_id, seller_account_id, price, status)
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement stmt =
                     conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, request.artifactUnitId());
            stmt.setLong(2, request.sellerAccountId());
            stmt.setBigDecimal(3, request.price());
            stmt.setString(4, request.status().name());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new RuntimeException("Failed to insert artifact listing");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {

                if (!rs.next()) {
                    throw new RuntimeException("Failed to retrieve generated artifact listing id");
                }

                long artifactListingId = rs.getLong(1);

                return selectById(conn, artifactListingId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Artifact listing inserted but not found [id=" + artifactListingId + "]"
                                )
                        );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while inserting artifact listing " +
                            "[artifactUnitId=" + request.artifactUnitId() +
                            ", sellerAccountId=" + request.sellerAccountId() +
                            ", price=" + request.price() +
                            ", status=" + request.status().name() + "]",
                    e
            );
        }
    }

    public Optional<ArtifactListing> selectById(Connection conn, long artifactListingId) {

        String sql = """
                SELECT id,
                       artifact_unit_id,
                       seller_account_id,
                       price,
                       status,
                       created_at,
                       updated_at
                  FROM artifact_listing
                 WHERE id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, artifactListingId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while selecting artifact listing " +
                            "[artifactListingId=" + artifactListingId + "]",
                    e
            );
        }
    }

    public List<ArtifactListing> selectByStatus(Connection conn, ArtifactListingStatus status) {

        String sql = """
                SELECT id,
                       artifact_unit_id,
                       seller_account_id,
                       price,
                       status,
                       created_at,
                       updated_at
                  FROM artifact_listing
                 WHERE status = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());

            try (ResultSet rs = stmt.executeQuery()) {

                List<ArtifactListing> listings = new ArrayList<>();

                while (rs.next()) {
                    listings.add(mapRow(rs));
                }

                return listings;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while selecting artifact listings " +
                            "[status=" + status.name() + "]",
                    e
            );
        }
    }

    public int updateStatus(Connection conn, long artifactId, ArtifactListingStatus status) {

        String sql = """
                UPDATE artifact_listing
                SET status = ?
                WHERE id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setLong(2, artifactId);

            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while updating artifact listing status " +
                            "[artifactId=" + artifactId + ", "
                            + "[status=" + status.name() + "]",
                    e
            );
        }
    }


    public int updatePrice(Connection conn, long artifactListingId, BigDecimal price) {

        String sql = """
                UPDATE artifact_listing
                SET price = ?
                WHERE id = ?;
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, price);
            stmt.setLong(2, artifactListingId);

            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while updating artifact listing price " +
                            "[price=" + price + ", "
                            + "[artifactListingId=" + artifactListingId + "]",
                    e
            );
        }
    }

    public ArtifactListingPageView selectActiveByActiveStatus(Connection conn, long accountId, ArtifactListingFilter filter, int page, int pageSize) {

        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT
                    l.id,
                    l.artifact_unit_id,
                    l.price,
                    l.created_at,
                    u.artifact_id,
                    JSON_VALUE(a.metadata, '$.name') AS artifact_name,
                    COUNT(*) OVER() AS total_items
                FROM artifact_listing l
                JOIN artifact_unit u ON u.id = l.artifact_unit_id
                JOIN artifact a ON a.id = u.artifact_id
                WHERE l.status = 'ACTIVE' AND l.seller_account_id != ?
                """);
        params.add(accountId);

        if (filter.artifactId() != null) {
            sql.append("AND u.artifact_id = ?\n");
            params.add(filter.artifactId());
        }
        if (filter.search() != null && !filter.search().isBlank()) {
            sql.append("AND LOWER(JSON_VALUE(a.metadata, '$.name')) LIKE LOWER(?)\n");
            params.add("%" + filter.search() + "%");
        }
        if (filter.minPrice() != null) {
            sql.append("AND l.price >= ?\n");
            params.add(filter.minPrice());
        }
        if (filter.maxPrice() != null) {
            sql.append("AND l.price <= ?\n");
            params.add(filter.maxPrice());
        }

        String orderBy = switch (Objects.toString(filter.sort(), "newest")) {
            case "price_asc"  -> "l.price ASC";
            case "price_desc" -> "l.price DESC";
            default           -> "l.created_at DESC";
        };
        sql.append("ORDER BY ").append(orderBy).append("\nLIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add(page * pageSize);

        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof Long l)        stmt.setLong(i + 1, l);
                else if (p instanceof Integer n) stmt.setInt(i + 1, n);
                else if (p instanceof String s)  stmt.setString(i + 1, s);
                else if (p instanceof BigDecimal bd) stmt.setBigDecimal(i + 1, bd);
            }

            try (ResultSet rs = stmt.executeQuery()) {

                ArrayList<ArtifactListingView> items = new ArrayList<>();
                long totalItems = 0;

                while (rs.next()) {
                    if (totalItems == 0) totalItems = rs.getLong("total_items");
                    items.add(new ArtifactListingView(
                            rs.getLong("id"),
                            rs.getLong("artifact_unit_id"),
                            rs.getLong("artifact_id"),
                            rs.getString("artifact_name"),
                            rs.getBigDecimal("price"),
                            rs.getTimestamp("created_at").toInstant()
                    ));
                }

                int totalPages = (int) Math.ceil((double) totalItems / pageSize);
                return new ArtifactListingPageView(items, page, pageSize, totalPages, totalItems);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Database error while selecting active artifact listings", e);
        }
    }

    public Optional<ArtifactListing> selectByIdForUpdate(Connection conn, long artifactListingId) {

        String sql = """
                SELECT * FROM artifact_listing
                WHERE id = ?
                FOR UPDATE;
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, artifactListingId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }

                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while selecting artifact listings " +
                            "[id=" + artifactListingId + "]",
                    e
            );
        }
    }

    public Optional<ArtifactListing> selectByArtifactUnitId(Connection conn, long artifactUnitId) {
        String sql = """
                SELECT id,
                       artifact_unit_id,
                       seller_account_id,
                       price,
                       status,
                       created_at,
                       updated_at
                  FROM artifact_listing
                 WHERE artifact_unit_id = ?
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
                    "Database error while selecting artifact listings " +
                            "[artifactUnitId=" + artifactUnitId + "]",
                    e
            );
        }
    }

    public ArtifactListingPageView selectByOwnerAccount(
            Connection conn,
            long accountId,
            int page,
            int pageSize
    ) {

        String sql = """
                SELECT
                    l.id,
                    l.artifact_unit_id,
                    u.artifact_id,
                    JSON_VALUE(a.metadata, '$.name') AS artifact_name,
                    l.price,
                    l.created_at,
                    l.updated_at,
                    COUNT(*) OVER() AS total_items
                FROM artifact_listing l
                JOIN artifact_unit u ON u.id = l.artifact_unit_id
                JOIN artifact a ON a.id = u.artifact_id
                WHERE l.seller_account_id = ?
                  AND l.status = 'ACTIVE'
                ORDER BY l.created_at DESC
                LIMIT ? OFFSET ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, accountId);
            stmt.setInt(2, pageSize);
            stmt.setInt(3, page * pageSize);

            try (ResultSet rs = stmt.executeQuery()) {

                ArrayList<ArtifactListingView> items = new ArrayList<>();
                long totalItems = 0;

                while (rs.next()) {

                    if (totalItems == 0) {
                        totalItems = rs.getLong("total_items");
                    }

                    items.add(
                            new ArtifactListingView(
                                    rs.getLong("id"),
                                    rs.getLong("artifact_unit_id"),
                                    rs.getLong("artifact_id"),
                                    rs.getString("artifact_name"),
                                    rs.getBigDecimal("price"),
                                    rs.getTimestamp("created_at").toInstant()
                            )
                    );
                }

                int totalPages = (int) Math.ceil((double) totalItems / pageSize);

                return new ArtifactListingPageView(
                        items,
                        page,
                        pageSize,
                        totalPages,
                        totalItems
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while selecting artifact listings [accountId=" + accountId + "]",
                    e
            );
        }
    }
}
