package br.com.ale.service.asset;

import br.com.ale.dao.asset.AssetListingDAO;
import br.com.ale.dao.asset.AssetPriceHistoryDAO;
import br.com.ale.dao.asset.AssetUnityDAO;
import br.com.ale.domain.asset.AssetListing;
import br.com.ale.domain.asset.AssetListingStatus;
import br.com.ale.domain.asset.ReasonType;
import br.com.ale.domain.exception.UnauthorizedOperationException;
import br.com.ale.dto.AssetListingPageView;
import br.com.ale.dto.CreateAssetListingRequest;
import br.com.ale.dto.CreatePriceHistoryRequest;
import br.com.ale.infrastructure.db.ConnectionProvider;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

public class AssetListingService {
    private final ConnectionProvider connectionProvider;
    private final AssetListingDAO assetListingDAO = new AssetListingDAO();
    private final AssetUnityDAO assetUnityDAO = new AssetUnityDAO();
    private final AssetPriceHistoryDAO assetPriceHistoryDAO = new AssetPriceHistoryDAO();

    public AssetListingService(
            ConnectionProvider connectionProvider
    ) {
        this.connectionProvider = connectionProvider;
    }

    public AssetListing changePrice(
            long listingId,
            BigDecimal newPrice,
            long changedByAccountId,
            ReasonType reason
    ) {
        try (Connection conn = connectionProvider.getConnection()) {
            conn.setAutoCommit(false);

            try {
                AssetListing listing =
                        assetListingDAO.selectByIdForUpdate(conn, listingId)
                                .orElseThrow(() -> new RuntimeException("Listing not found"));

                BigDecimal oldPrice = listing.getPrice();

                if (oldPrice.compareTo(newPrice) == 0) {
                    throw new RuntimeException("New price must be different");
                }

                assetListingDAO.updatePrice(
                        conn,
                        listingId,
                        newPrice
                );

                assetPriceHistoryDAO.insert(conn, new CreatePriceHistoryRequest(
                        listing.getId(),
                        listing.getAssetUnityId(),
                        oldPrice,
                        newPrice,
                        changedByAccountId,
                        reason
                ));

                conn.commit();

                return assetListingDAO
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

    public AssetListing createAssetOffer(CreateAssetListingRequest request) {

        try (Connection conn = connectionProvider.getConnection()) {

            conn.setAutoCommit(false);

            try {

                validatePrice(request.price());

                boolean locked = assetUnityDAO.tryUpdateToMarket(conn, request.assetUnityId());

                if (!locked) {
                    throw new UnauthorizedOperationException("Asset not owned or not available");
                }

                AssetListing listing = assetListingDAO.insert(
                        conn,
                        new CreateAssetListingRequest(
                                request.assetUnityId(),
                                request.sellerAccountId(),
                                request.price(),
                                AssetListingStatus.ACTIVE
                        )
                );

                conn.commit();
                return listing;

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException("Error creating asset offer", e);
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

    public AssetListing selectById(long assetListingId) {
        try (Connection conn = connectionProvider.getConnection()) {
            return assetListingDAO.selectById(conn, assetListingId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Asset listing not found [assetListingId=" + assetListingId + "]"
                            )

                    );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while selecting asset listing " +
                            "[assetId=" + assetListingId + "]",
                    e
            );
        }
    }

    public List<AssetListing> selectByStatus(AssetListingStatus status) {
        try (Connection conn = connectionProvider.getConnection()) {
            return assetListingDAO.selectByStatus(conn, status);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while selecting asset listing " +
                            "[status=" + status.name() + "]",
                    e
            );
        }
    }

    public int updateStatus(long assetListingId, AssetListingStatus status) {
        try (Connection conn = connectionProvider.getConnection()) {
            return assetListingDAO.updateStatus(conn, assetListingId, status);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while updating asset listing " +
                            "[status=" + status.name() + ", "
                            + "assetListingId= " + assetListingId + "]",
                    e
            );
        }
    }

    public AssetListing selectByAssetUnitId(long assetUnityId) {
        try (Connection conn = connectionProvider.getConnection()) {
            return assetListingDAO.selectByAssetUnitId(conn, assetUnityId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Asset listing not found [assetUnityId=" + assetUnityId + "]"
                            ));
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while selecting asset listing " +
                            "[assetUnityId=" + assetUnityId + "]",
                    e
            );
        }
    }


    public AssetListingPageView selectActiveByActiveStatus(long accountId, int page, int pageSize) {

        if (page < 0 || pageSize <= 0) {
            throw new IllegalArgumentException("Invalid pagination params");
        }

        try (Connection conn = connectionProvider.getConnection()) {
            return assetListingDAO.selectActiveByActiveStatus(conn, accountId, page, pageSize);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while selecting active asset listing " +
                            "[accountId=" + accountId + ", page=" + page + "]",
                    e
            );
        }
    }

    public AssetListingPageView selectByOwnerAccount(long accountId, int page, int pageSize) {

        if (page < 0 || pageSize <= 0) {
            throw new IllegalArgumentException("Invalid pagination params");
        }

        try (Connection conn = connectionProvider.getConnection()) {
            return assetListingDAO.selectByOwnerAccount(conn, accountId, page, pageSize);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while selecting asset listing " +
                            "[accountId=" + accountId + ", page=" + page + "]",
                    e
            );
        }
    }
}
