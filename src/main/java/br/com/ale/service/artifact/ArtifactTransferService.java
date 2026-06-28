package br.com.ale.service.artifact;

import br.com.ale.dao.artifact.ArtifactTransferDAO;
import br.com.ale.domain.artifact.ArtifactTransfer;
import br.com.ale.dto.ArtifactTransferLogPageView;
import br.com.ale.dto.ArtifactUnitTransferView;
import br.com.ale.dto.CreateArtifactTransferRequest;
import br.com.ale.infrastructure.db.ConnectionProvider;
import br.com.ale.service.webhook.ArtifactWebhookNotifier;

import java.sql.Connection;
import java.util.List;

public class ArtifactTransferService {

    private final ConnectionProvider connectionProvider;
    private final ArtifactWebhookNotifier webhookNotifier;
    private final ArtifactTransferDAO artifactTransferDAO = new ArtifactTransferDAO();

    public ArtifactTransferService(
            ConnectionProvider connectionProvider,
            ArtifactWebhookNotifier webhookNotifier
    ) {
        this.connectionProvider = connectionProvider;
        this.webhookNotifier = webhookNotifier;
    }

    public ArtifactTransfer createAsset(CreateArtifactTransferRequest request) {

        if (request.fromAccountId() == request.toAccountId()) {
            throw new RuntimeException(
                    "Not allowed artifact transfer to the same account " +
                            "[fromAccountId=" + request.fromAccountId() +
                            ", toAccountId=" + request.toAccountId() + "]"
            );
        }

        try (Connection conn = connectionProvider.getConnection()) {
            conn.setAutoCommit(false);

            try {

                ArtifactTransfer artifact = artifactTransferDAO.insert(conn, request);

                conn.commit();

                webhookNotifier.notifyArtifactTransferCreated(artifact);

                return artifact;

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while creating artifact Transfer[artifactUnitId=" + request.artifactUnitId() +
                            ", fromAccountId=" + request.fromAccountId() + ", " +
                            ", toAccountId=" + request.toAccountId() + "]"
            );
        }
    }

    public ArtifactTransferLogPageView publicFeed(int page, int pageSize) {
        try (Connection conn = connectionProvider.getConnection()) {
            return artifactTransferDAO.selectPublicFeed(conn, page, pageSize);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while fetching artifact transfer feed " +
                            "[page=" + page + ", pageSize=" + pageSize + "]",
                    e
            );
        }
    }

    public List<ArtifactUnitTransferView> selectByUnitId(long artifactUnitId) {
        try (Connection conn = connectionProvider.getConnection()) {
            return artifactTransferDAO.selectByUnitId(conn, artifactUnitId);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while fetching transfer chain [unitId=" + artifactUnitId + "]", e
            );
        }
    }

    public ArtifactTransfer selectById(long artifactTransferId) {
        try (Connection conn = connectionProvider.getConnection()) {
            return artifactTransferDAO.selectById(conn, artifactTransferId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Artifact transfer not found [artifactId=" + artifactTransferId + "]"
                            )

                    );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while selecting artifact transfer " +
                            "[artifactId=" + artifactTransferId + "]",
                    e
            );
        }
    }
}
