package br.com.ale.service.marketplace;

import br.com.ale.dao.AccountDAO;
import br.com.ale.dao.artifact.ArtifactListingDAO;
import br.com.ale.dao.artifact.ArtifactTransferDAO;
import br.com.ale.dao.artifact.ArtifactUnitDAO;
import br.com.ale.domain.artifact.*;
import br.com.ale.dto.CreateArtifactPurchaseRequest;
import br.com.ale.dto.CreateArtifactTransferRequest;
import br.com.ale.infrastructure.db.ConnectionProvider;
import br.com.ale.service.account.AccountService;
import br.com.ale.service.webhook.ArtifactWebhookNotifier;

import java.sql.Connection;

public class ArtifactPurchaseService {

    private final ConnectionProvider connectionProvider;
    private final ArtifactWebhookNotifier webhookNotifier;
    private final ArtifactUnitDAO artifactUnitDAO = new ArtifactUnitDAO();
    private final ArtifactListingDAO artifactListingDAO = new ArtifactListingDAO();
    private final ArtifactTransferDAO artifactTransferDAO = new ArtifactTransferDAO();

    public ArtifactPurchaseService(
            ConnectionProvider connectionProvider,
            ArtifactWebhookNotifier webhookNotifier
    ) {
        this.connectionProvider = connectionProvider;
        this.webhookNotifier = webhookNotifier;
    }

    public ArtifactPurchase purchase(CreateArtifactPurchaseRequest request) {

        try (Connection conn = connectionProvider.getConnection()) {
            conn.setAutoCommit(false);

            try {
                ArtifactListing listing =
                        artifactListingDAO.selectByIdForUpdate(
                                conn,
                                request.listingId()
                        ).orElseThrow(() ->
                                new RuntimeException("Listing not found")
                        );

                if (listing.getStatus() != ArtifactListingStatus.ACTIVE)
                    throw new RuntimeException("Listing already sold");

                if (listing.getSellerAccountId() == request.buyerAccountId())
                    throw new RuntimeException("Buyer cannot be seller");

                ArtifactUnit unity =
                        artifactUnitDAO.selectByIdForUpdate(
                                conn,
                                listing.getArtifactUnitId()
                        ).orElseThrow(() ->
                                new RuntimeException("ArtifactUnit not found")
                        );

                if (unity.getOwnerAccountId() != listing.getSellerAccountId())
                    throw new RuntimeException("Artifact already purchased by another client");

                artifactListingDAO.updateStatus(
                        conn,
                        listing.getId(),
                        ArtifactListingStatus.SOLD
                );

                ArtifactTransfer transfer = artifactTransferDAO.insert(
                        conn,
                        new CreateArtifactTransferRequest(
                                unity.getId(),
                                listing.getSellerAccountId(),
                                request.buyerAccountId()
                        )
                );

                boolean transferred = artifactUnitDAO.tryTransferOwnership(
                        conn,
                        unity.getId(),
                        listing.getSellerAccountId(),
                        request.buyerAccountId()
                );

                if (!transferred)
                    throw new RuntimeException("Artifact not transferred");

                conn.commit();

                ArtifactPurchase purchase = new ArtifactPurchase(
                        listing.getId(),
                        unity.getId(),
                        listing.getSellerAccountId(),
                        request.buyerAccountId(),
                        listing.getPrice()
                );

                webhookNotifier.notifyArtifactTransferCreated(transfer);
                webhookNotifier.notifyArtifactPurchaseCompleted(purchase);

                return purchase;

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException("Error while purchasing artifact", e);
        }
    }
}
