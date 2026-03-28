package br.com.ale.service;

import br.com.ale.dao.EmailVerificationDAO;
import br.com.ale.domain.emailVerification.EmailVerification;
import br.com.ale.domain.emailVerification.EmailVerificationType;
import br.com.ale.dto.CreateEmailVerificationRequest;
import br.com.ale.infrastructure.db.ConnectionProvider;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.Optional;

@Service
public class EmailVerificationService {

    private final ConnectionProvider connectionProvider;
    private final EmailVerificationDAO emailVerificationDAO = new EmailVerificationDAO();

    public EmailVerificationService(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public long create(CreateEmailVerificationRequest request) {

        try (Connection conn = connectionProvider.getConnection()) {

            conn.setAutoCommit(false);

            try {
                emailVerificationDAO.invalidatePreviousTokens(
                        conn,
                        request.clientId(),
                        request.type()
                );

                long id = emailVerificationDAO.insert(conn, request);

                conn.commit();
                return id;

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while creating email verification " +
                            "[clientId=" + request.clientId() + "]",
                    e
            );
        }
    }

    public Optional<EmailVerification> findByToken(String token, EmailVerificationType type) {

        try (Connection conn = connectionProvider.getConnection()) {

            return emailVerificationDAO.findValidByToken(conn, token, type);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while fetching email verification " +
                            "[token=" + token + "]",
                    e
            );
        }
    }

    public Optional<EmailVerification> findActiveByClientId(
            long clientId,
            EmailVerificationType type
    ) {

        try (Connection conn = connectionProvider.getConnection()) {

            return emailVerificationDAO.findActiveByClientId(conn, clientId, type);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while fetching active email verification " +
                            "[clientId=" + clientId + "]",
                    e
            );
        }
    }

    public EmailVerification validateToken(String token, EmailVerificationType type) {

        return findByToken(token, type)
                .orElseThrow(() ->
                        new RuntimeException("Invalid or expired email verification token")
                );
    }

    public EmailVerification confirmToken(String token, EmailVerificationType type) {

        try (Connection conn = connectionProvider.getConnection()) {

            conn.setAutoCommit(false);

            try {
                EmailVerification verification = emailVerificationDAO
                        .findValidByToken(conn, token, type)
                        .orElseThrow(() ->
                                new RuntimeException("Invalid or expired email verification token")
                        );

                emailVerificationDAO.markVerified(conn, verification.getId());

                conn.commit();

                return verification;

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while confirming email verification " +
                            "[token=" + token + "]",
                    e
            );
        }
    }
}