package br.com.ale.service.artifact;

import br.com.ale.dao.artifact.ArtifactDAO;
import br.com.ale.dao.artifact.ArtifactUnitDAO;
import br.com.ale.domain.artifact.ArtifactUnit;
import br.com.ale.domain.exception.ArtifactUnitNotFoundException;
import br.com.ale.dto.ArtifactUnitPageView;
import br.com.ale.dto.CreateArtifactUnitRequest;
import br.com.ale.infrastructure.db.ConnectionProvider;
import br.com.ale.service.webhook.ArtifactWebhookNotifier;

import java.sql.Connection;

public class ArtifactUnitService {

    private final ConnectionProvider connectionProvider;
    private final ArtifactWebhookNotifier webhookNotifier;
    private final ArtifactDAO assetDAO = new ArtifactDAO();
    private final ArtifactUnitDAO artifactUnitDAO = new ArtifactUnitDAO();

    public ArtifactUnitService(ConnectionProvider connectionProvider,
                             ArtifactWebhookNotifier webhookNotifier) {
        this.connectionProvider = connectionProvider;
        this.webhookNotifier = webhookNotifier;
    }

    public boolean tryUpdateToMarket(long artifactUnitId, long accountId) {
        try (Connection conn = connectionProvider.getConnection()) {
            return artifactUnitDAO.tryUpdateToMarket(conn, artifactUnitId, accountId);
        } catch (Exception e) {
            throw new RuntimeException("Service error while lock for market [unityId=" + artifactUnitId + "]", e);
        }
    }

    public ArtifactUnit createArtifactUnit(CreateArtifactUnitRequest request) {
        try (Connection conn = connectionProvider.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int updated = assetDAO.updateTotalSupply(conn, request.artifactId(), 1);
                if (updated == 0) {
                    throw new RuntimeException("Artifact supply not available [artifactId=" + request.artifactId() + "]");
                }

                ArtifactUnit unity = artifactUnitDAO.insert(conn, request);
                conn.commit();
                webhookNotifier.notifyArtifactUnitCreated(unity);
                return unity;

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while creating artifactUnit [artifactId=" + request.artifactId() +
                            ", ownerAccountId=" + request.ownerAccountId() + "]",
                    e
            );
        }
    }

    public ArtifactUnit selectById(long artifactUnitId) {
        try (Connection conn = connectionProvider.getConnection()) {
            return artifactUnitDAO.selectById(conn, artifactUnitId)
                    .orElseThrow(() -> new ArtifactUnitNotFoundException(artifactUnitId));
        } catch (ArtifactUnitNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Service error while selecting artifactUnit [id=" + artifactUnitId + "]", e);
        }
    }

    public ArtifactUnitPageView selectByOwnerAccount(long ownerAccountId, int page, int pageSize) {
        try (Connection conn = connectionProvider.getConnection()) {
            return artifactUnitDAO.selectByOwnerAccount(conn, ownerAccountId, page, pageSize);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while selecting artifact unity [ownerAccountId=" + ownerAccountId + "]",
                    e
            );
        }
    }
}
