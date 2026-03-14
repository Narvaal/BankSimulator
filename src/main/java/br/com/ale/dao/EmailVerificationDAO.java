package br.com.ale.dao;

import br.com.ale.domain.emailVerification.EmailVerification;
import br.com.ale.dto.CreateEmailVerificationRequest;

import java.sql.*;
import java.util.Optional;

public class EmailVerificationDAO {

    public long insert(Connection conn, CreateEmailVerificationRequest request) {

        String sql = """
                INSERT INTO email_verification (
                    client_id,
                    token,
                    expires_at,
                    verified_at
                )
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"id"})) {

            stmt.setLong(1, request.clientId());
            stmt.setString(2, request.token());
            stmt.setTimestamp(3, Timestamp.from(request.expiresAt()));

            if (request.verifiedAt() != null) {
                stmt.setTimestamp(4, Timestamp.from(request.verifiedAt()));
            } else {
                stmt.setNull(4, Types.TIMESTAMP);
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
                    "Database error while email verification " +
                            "[clientId=" + request.clientId() +
                            ", token=" + request.token() + "]",
                    e
            );
        }
    }

    public Optional<EmailVerification> findByToken(Connection conn, String token) {

        String sql = """
                SELECT id, client_id, token, expires_at, verified_at, created_at
                FROM email_verification
                WHERE token = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token);

            try (ResultSet rs = stmt.executeQuery()) {

                if (!rs.next()) {
                    return Optional.empty();
                }

                EmailVerification verification = new EmailVerification(
                        rs.getLong("id"),
                        rs.getLong("client_id"),
                        rs.getString("token"),
                        rs.getTimestamp("expires_at").toInstant(),
                        rs.getTimestamp("verified_at") != null
                                ? rs.getTimestamp("verified_at").toInstant()
                                : null,
                        rs.getTimestamp("created_at").toInstant()
                );

                return Optional.of(verification);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while fetching email verification [token=" + token + "]",
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

    public Optional<EmailVerification> findActiveByClientId(Connection conn, long clientId) {

        String sql = """
                SELECT id, client_id, token, expires_at, verified_at, created_at
                FROM email_verification
                WHERE client_id = ?
                AND verified_at IS NULL
                ORDER BY created_at DESC
                LIMIT 1
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, clientId);

            try (ResultSet rs = stmt.executeQuery()) {

                if (!rs.next()) {
                    return Optional.empty();
                }

                EmailVerification verification = new EmailVerification(
                        rs.getLong("id"),
                        rs.getLong("client_id"),
                        rs.getString("token"),
                        rs.getTimestamp("expires_at").toInstant(),
                        null,
                        rs.getTimestamp("created_at").toInstant()
                );

                return Optional.of(verification);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while fetching active email verification [clientId=" + clientId + "]",
                    e
            );
        }
    }
}