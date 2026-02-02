package br.com.ale.dao;

import br.com.ale.domain.auth.Credential;
import br.com.ale.dto.CreateCredentialRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class CredentialDAO {

    public Optional<Credential> selectByEmail(Connection conn, String email) {

        String sql = """
                SELECT * FROM credential
                WHERE email = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(
                            new Credential(
                                    rs.getLong("id"),
                                    rs.getLong("client_id"),
                                    rs.getString("email"),
                                    rs.getString("password_hash")
                            )
                    );
                }
                return Optional.empty();
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Database error while selecting credential ["
                            + ", email=" + email + "]",
                    e
            );
        }
    }

    public long insert(Connection conn, long clientId, String email, String passwordHash) {

        String sql = """
                INSERT INTO credential (client_id, email, password_hash)
                VALUES (?, ?, ?)
                """;
        try (PreparedStatement stmt =
                     conn.prepareStatement(sql, new String[]{"id"})) {

            stmt.setLong(1, clientId);
            stmt.setString(2, email);
            stmt.setString(3, passwordHash);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new RuntimeException(
                        "Failed to insert credential [clientId=" + clientId
                                + ", email=" + email + ", "
                                + "passwordHash=" + passwordHash + "]"
                );
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
                throw new RuntimeException(
                        "Failed to retrieve credential id [clientId=" + clientId
                                + ", email=" + email + ", "
                                + "passwordHash=" + passwordHash + "]"
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database error while inserting credential "
                            + "[email=" + email + ", "
                            + "passwordHash=" + passwordHash + "]",
                    e
            );
        }
    }
}
