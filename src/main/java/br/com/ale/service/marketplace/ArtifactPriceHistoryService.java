package br.com.ale.service.marketplace;

import br.com.ale.dao.artifact.ArtifactListingDAO;
import br.com.ale.dao.artifact.ArtifactPriceHistoryDAO;
import br.com.ale.domain.artifact.ArtifactListing;
import br.com.ale.domain.artifact.ArtifactPriceHistory;
import br.com.ale.domain.artifact.ReasonType;
import br.com.ale.dto.CreatePriceHistoryRequest;
import br.com.ale.infrastructure.db.ConnectionProvider;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

public class ArtifactPriceHistoryService {

    private final ConnectionProvider connectionProvider;

    private final ArtifactListingDAO artifactListingDAO = new ArtifactListingDAO();
    private final ArtifactPriceHistoryDAO artifactPriceHistoryDAO =
            new ArtifactPriceHistoryDAO();

    public ArtifactPriceHistoryService(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public ArtifactPriceHistory registerPriceChange(
            long artifactListingId,
            BigDecimal newPrice,
            long changedByAccountId,
            ReasonType reason
    ) {

        try (Connection conn = connectionProvider.getConnection()) {
            conn.setAutoCommit(false);

            try {

                ArtifactListing listing =
                        artifactListingDAO
                                .selectById(conn, artifactListingId)
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "ArtifactListing not found " +
                                                        "[artifactListingId=" + artifactListingId + "]"
                                        )
                                );

                BigDecimal oldPrice = listing.getPrice();
                if (reason == ReasonType.SOLD) {
                    oldPrice = artifactPriceHistoryDAO
                            .selectLatestByArtifactUnitId(
                                    conn,
                                    listing.getArtifactUnitId()
                            )
                            .map(ArtifactPriceHistory::getNewPrice)
                            .orElse(oldPrice);
                }

                ArtifactPriceHistory persisted =
                        artifactPriceHistoryDAO.insert(
                                conn,
                                new CreatePriceHistoryRequest(
                                        listing.getId(),
                                        listing.getArtifactUnitId(),
                                        oldPrice,
                                        newPrice,
                                        changedByAccountId,
                                        reason
                                )
                        );

                conn.commit();
                return persisted;

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while creating artifact price history " +
                            "[artifactListingId=" + artifactListingId +
                            ", newPrice=" + newPrice +
                            ", changedByAccountId=" + changedByAccountId +
                            ", reason=" + reason + "]",
                    e
            );
        }
    }

    public List<ArtifactPriceHistory> listByArtifactListingId(long artifactListingId) {
        try (Connection conn = connectionProvider.getConnection()) {
            return artifactPriceHistoryDAO.selectByArtifactListingId(conn, artifactListingId);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while listing artifact price history " +
                            "[artifactListingId=" + artifactListingId + "]",
                    e
            );
        }
    }

    public List<ArtifactPriceHistory> listByArtifactId(long artifactId) {
        try (Connection conn = connectionProvider.getConnection()) {
            return artifactPriceHistoryDAO.selectByArtifactUnitId(conn, artifactId);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while listing artifact price history " +
                            "[artifactId=" + artifactId + "]",
                    e
            );
        }
    }
}
