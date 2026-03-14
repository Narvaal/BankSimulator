package br.com.ale.service;

import br.com.ale.dao.EmailVerificationDAO;
import br.com.ale.domain.emailVerification.EmailVerification;
import br.com.ale.dto.CreateEmailVerificationRequest;
import br.com.ale.infrastructure.db.ConnectionProvider;

import java.sql.Connection;
import java.util.Optional;

public class EmailVerificationService {

    private final ConnectionProvider connectionProvider;
    private final EmailVerificationDAO emailVerificationDAO = new EmailVerificationDAO();

    public EmailVerificationService(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public Long insert(CreateEmailVerificationRequest request) {

        try (Connection conn = connectionProvider.getConnection()) {

            return emailVerificationDAO.insert(conn, request);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while inserting email verification " +
                            "[clientId=" + request.clientId() + "]",
                    e
            );
        }
    }

    public Optional<EmailVerification> findByToken(String token) {

        try (Connection conn = connectionProvider.getConnection()) {

            return emailVerificationDAO.findByToken(conn, token);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while fetching email verification " +
                            "[token=" + token + "]",
                    e
            );
        }
    }

    public void markVerified(long id) {

        try (Connection conn = connectionProvider.getConnection()) {

            emailVerificationDAO.markVerified(conn, id);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while marking email verification as verified " +
                            "[id=" + id + "]",
                    e
            );
        }
    }

    public Optional<EmailVerification> findActiveByClientId(long clientId) {

        try (Connection conn = connectionProvider.getConnection()) {

            return emailVerificationDAO.findActiveByClientId(conn, clientId);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while fetching active email verification " +
                            "[clientId=" + clientId + "]",
                    e
            );
        }
    }

    public EmailVerification validateToken(String token) {

        Optional<EmailVerification> optional = findByToken(token);

        if (optional.isEmpty()) {
            throw new RuntimeException("Invalid email verification token");
        }

        EmailVerification verification = optional.get();

        if (verification.isVerified()) {
            throw new RuntimeException("Email already verified");
        }

        if (verification.isExpired()) {
            throw new RuntimeException("Email verification token expired");
        }

        return verification;
    }
}