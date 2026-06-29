package br.com.ale.service.artifact;

import br.com.ale.dao.AccountDAO;
import br.com.ale.dao.artifact.ArtifactListingDAO;
import br.com.ale.dao.artifact.ArtifactPriceHistoryDAO;
import br.com.ale.dao.artifact.ArtifactUnitDAO;
import br.com.ale.domain.account.Account;
import br.com.ale.domain.artifact.ArtifactListing;
import br.com.ale.domain.artifact.ArtifactListingStatus;
import br.com.ale.domain.artifact.ArtifactUnit;
import br.com.ale.domain.artifact.ReasonType;
import br.com.ale.domain.exception.InvalidArtifactListingStateException;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.dto.ArtifactListingFilter;
import br.com.ale.dto.ArtifactListingPageView;
import br.com.ale.dto.CreateArtifactListingRequest;
import br.com.ale.dto.CreatePriceHistoryRequest;
import br.com.ale.infrastructure.db.ConnectionProvider;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

public class ArtifactListingService {
    private final ConnectionProvider connectionProvider;
    private final AccountDAO accountDAO = new AccountDAO();
    private final ArtifactListingDAO artifactListingDAO = new ArtifactListingDAO();
    private final ArtifactUnitDAO artifactUnitDAO = new ArtifactUnitDAO();
    private final ArtifactPriceHistoryDAO artifactPriceHistoryDAO = new ArtifactPriceHistoryDAO();

    public ArtifactListingService(
            ConnectionProvider connectionProvider
    ) {
        this.connectionProvider = connectionProvider;
    }

    public ArtifactListing changePrice(
            long listingId,
            BigDecimal newPrice,
            long changedByAccountId,
            ReasonType reason
    ) {
        try (Connection conn = connectionProvider.getConnection()) {
            conn.setAutoCommit(false);

            try {
                ArtifactListing listing =
                        artifactListingDAO.selectByIdForUpdate(conn, listingId)
                                .orElseThrow(() -> new RuntimeException("Listing not found"));

                BigDecimal oldPrice = listing.getPrice();

                if (oldPrice.compareTo(newPrice) == 0) {
                    throw new RuntimeException("New price must be different");
                }

                artifactListingDAO.updatePrice(
                        conn,
                        listingId,
                        newPrice
                );

                artifactPriceHistoryDAO.insert(conn, new CreatePriceHistoryRequest(
                        listing.getId(),
                        listing.getArtifactUnitId(),
                        oldPrice,
                        newPrice,
                        changedByAccountId,
                        reason
                ));

                conn.commit();

                return artifactListingDAO
                        .selectById(conn, listingId)
                        .orElseThrow();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException("Error while changing listing price", e);
        }
    }

    public void cancelListing(long listingId, long clientId) {

        try (Connection conn = connectionProvider.getConnection()) {

            conn.setAutoCommit(false);

            try {

                ArtifactListing listing = artifactListingDAO.selectById(conn, listingId)
                        .orElseThrow();

                if (listing.getStatus() != ArtifactListingStatus.ACTIVE) {
                    throw new InvalidArtifactListingStateException(listingId);
                }

                ArtifactUnit unity = artifactUnitDAO.selectById(conn, listing.getArtifactUnitId())
                        .orElseThrow();

                Account account = accountDAO.selectById(conn, unity.getOwnerAccountId())
                        .orElseThrow();

                if (account.getClientId() != clientId) {
                    throw new UnauthorizedOperationException("Not owner");
                }

                artifactListingDAO.updateStatus(conn, listingId, ArtifactListingStatus.CANCELED);

                artifactUnitDAO.updateStatus(conn, unity.getId());

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException("Error canceling listing", e);
        }
    }

    public ArtifactListing createArtifactOffer(CreateArtifactListingRequest request) {

        try (Connection conn = connectionProvider.getConnection()) {

            conn.setAutoCommit(false);

            try {

                validatePrice(request.price());

                boolean locked = artifactUnitDAO.tryUpdateToMarket(conn, request.artifactUnitId(), request.sellerAccountId());

                if (!locked) {
                    throw new UnauthorizedOperationException("Artifact not owned or not available");
                }

                ArtifactListing listing = artifactListingDAO.insert(
                        conn,
                        new CreateArtifactListingRequest(
                                request.artifactUnitId(),
                                request.sellerAccountId(),
                                request.price(),
                                ArtifactListingStatus.ACTIVE
                        )
                );

                conn.commit();
                return listing;

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException("Error creating artifact offer", e);
        }
    }

    private void validatePrice(BigDecimal price) {

        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }

        if (price.scale() > 2) {
            throw new IllegalArgumentException("Price cannot have more than 2 decimal places");
        }
    }

    public ArtifactListing selectById(long artifactListingId) {
        try (Connection conn = connectionProvider.getConnection()) {
            return artifactListingDAO.selectById(conn, artifactListingId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Artifact listing not found [artifactListingId=" + artifactListingId + "]"
                            )

                    );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while selecting artifact listing " +
                            "[artifactId=" + artifactListingId + "]",
                    e
            );
        }
    }

    public List<ArtifactListing> selectByStatus(ArtifactListingStatus status) {
        try (Connection conn = connectionProvider.getConnection()) {
            return artifactListingDAO.selectByStatus(conn, status);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while selecting artifact listing " +
                            "[status=" + status.name() + "]",
                    e
            );
        }
    }

    public int updateStatus(long artifactListingId, ArtifactListingStatus status) {
        try (Connection conn = connectionProvider.getConnection()) {
            return artifactListingDAO.updateStatus(conn, artifactListingId, status);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while updating artifact listing " +
                            "[status=" + status.name() + ", "
                            + "artifactListingId= " + artifactListingId + "]",
                    e
            );
        }
    }

    public ArtifactListing selectByArtifactUnitId(long artifactUnitId) {
        try (Connection conn = connectionProvider.getConnection()) {
            return artifactListingDAO.selectByArtifactUnitId(conn, artifactUnitId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Artifact listing not found [artifactUnitId=" + artifactUnitId + "]"
                            ));
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while selecting artifact listing " +
                            "[artifactUnitId=" + artifactUnitId + "]",
                    e
            );
        }
    }


    public ArtifactListingPageView selectActiveByActiveStatus(long accountId, ArtifactListingFilter filter, int page, int pageSize) {

        if (page < 0 || pageSize <= 0) {
            throw new IllegalArgumentException("Invalid pagination params");
        }

        try (Connection conn = connectionProvider.getConnection()) {
            return artifactListingDAO.selectActiveByActiveStatus(conn, accountId, filter, page, pageSize);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while selecting active artifact listing " +
                            "[accountId=" + accountId + ", page=" + page + "]",
                    e
            );
        }
    }

    public ArtifactListingPageView selectByOwnerAccount(long accountId, int page, int pageSize) {

        if (page < 0 || pageSize <= 0) {
            throw new IllegalArgumentException("Invalid pagination params");
        }

        try (Connection conn = connectionProvider.getConnection()) {
            return artifactListingDAO.selectByOwnerAccount(conn, accountId, page, pageSize);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while selecting artifact listing " +
                            "[accountId=" + accountId + ", page=" + page + "]",
                    e
            );
        }
    }
}
