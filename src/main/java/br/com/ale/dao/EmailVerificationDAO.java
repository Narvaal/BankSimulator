package br.com.ale.dao;

import br.com.ale.domain.emailVerification.EmailVerification;
import br.com.ale.domain.emailVerification.EmailVerificationType;
import br.com.ale.dto.CreateEmailVerificationRequest;

import java.sql.*;
import java.time.Instant;
import java.util.Optional;

public class EmailVerificationDAO {

    public long insert(Connection conn, CreateEmailVerificationRequest request) {

        String sql = """
                INSERT INTO email_verification (
                    client_id,
                    token,
                    type,
                    expires_at,
                    verified_at
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"id"})) {

            stmt.setLong(1, request.clientId());
            stmt.setString(2, request.token());
            stmt.setObject(3, request.type().name(), Types.OTHER);
            stmt.setTimestamp(4, Timestamp.from(request.expiresAt()));

            if (request.verifiedAt() != null) {
                stmt.setTimestamp(5, Timestamp.from(request.verifiedAt()));
            } else {
                stmt.setNull(5, Types.TIMESTAMP);
            }

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new RuntimeException(
                        "Failed to insert email verification [clientId=" + request.clientId() +
                                ", token=" + request.token() + "]"
                );
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
                throw new RuntimeException(
                        "Failed to retrieve email verification id [clientId=" +
                                request.clientId() + ", token=" + request.token() + "]"
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while inserting email verification " +
                            "[clientId=" + request.clientId() +
                            ", token=" + request.token() + "]",
                    e
            );
        }
    }

    public Optional<EmailVerification> findValidByToken(
            Connection conn,
            String token,
            EmailVerificationType type
    ) {

        String sql = """
                SELECT id, client_id, token, type, expires_at, verified_at, created_at
                FROM email_verification
                WHERE token = ?
                  AND type = ?
                  AND verified_at IS NULL
                  AND expires_at > now()
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token);
            stmt.setObject(2, type.name(), Types.OTHER);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while fetching valid email verification [token=" + token + "]",
                    e
            );
        }
    }

    public Optional<EmailVerification> findActiveByClientId(
            Connection conn,
            long clientId,
            EmailVerificationType type
    ) {

        String sql = """
                SELECT id, client_id, token, type, expires_at, verified_at, created_at
                FROM email_verification
                WHERE client_id = ?
                  AND type = ?
                  AND verified_at IS NULL
                  AND expires_at > now()
                ORDER BY created_at DESC
                LIMIT 1
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, clientId);
            stmt.setObject(2, type.name(), Types.OTHER);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while fetching active email verification [clientId=" + clientId + "]",
                    e
            );
        }
    }

    public void markVerified(Connection conn, long id) {

        String sql = """
                UPDATE email_verification
                SET verified_at = now()
                WHERE id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException(
                        "Failed to mark email verification as verified [id=" + id + "]"
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while marking email verification verified [id=" + id + "]",
                    e
            );
        }
    }

    public void invalidatePreviousTokens(
            Connection conn,
            long clientId,
            EmailVerificationType type
    ) {

        String sql = """
                UPDATE email_verification
                SET verified_at = now()
                WHERE client_id = ?
                  AND type = ?
                  AND verified_at IS NULL
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, clientId);
            stmt.setObject(2, type.name(), Types.OTHER);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while invalidating tokens [clientId=" + clientId + "]",
                    e
            );
        }
    }

    private EmailVerification mapRow(ResultSet rs) throws SQLException {

        Timestamp verifiedAtTs = rs.getTimestamp("verified_at");
        Instant verifiedAt = verifiedAtTs != null ? verifiedAtTs.toInstant() : null;

        return new EmailVerification(
                rs.getLong("id"),
                rs.getLong("client_id"),
                rs.getString("token"),
                EmailVerificationType.valueOf(rs.getString("type")),
                rs.getTimestamp("expires_at").toInstant(),
                verifiedAt,
                rs.getTimestamp("created_at").toInstant()
        );
    }
}