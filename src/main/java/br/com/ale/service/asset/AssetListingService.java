package br.com.ale.service.asset;

import br.com.ale.dao.asset.AssetListingDAO;
import br.com.ale.domain.asset.AssetListing;
import br.com.ale.domain.asset.AssetListingStatus;
import br.com.ale.dto.CreateAssetListingRequest;
import br.com.ale.infrastructure.db.ConnectionProvider;

import java.sql.Connection;
import java.util.List;

public class AssetListingService {
    private final ConnectionProvider connectionProvider;
    private final AssetListingDAO assetListingDAO = new AssetListingDAO();

    public AssetListingService(
            ConnectionProvider connectionProvider
    ) {
        this.connectionProvider = connectionProvider;
    }

    public AssetListing createAssetListing(CreateAssetListingRequest request) {

        try (Connection conn = connectionProvider.getConnection()) {
            conn.setAutoCommit(false);

            try {

                AssetListing asset = assetListingDAO.insert(conn, request);

                conn.commit();

                return asset;

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while creating asset listing [assetUnityId=" + request.assetUnityId() +
                            ", sellerAccountId=" + request.sellerAccountId() + ", " +
                            ", price=" + request.price() + ", " +
                            ", status=" + request.status().name() + "]"
            );
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

    public List<AssetListing> selectById(AssetListingStatus status) {
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
}
