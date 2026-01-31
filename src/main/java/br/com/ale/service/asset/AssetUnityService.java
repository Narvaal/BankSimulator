package br.com.ale.service.asset;

import br.com.ale.dao.asset.AssetUnityDAO;
import br.com.ale.domain.asset.AssetUnity;
import br.com.ale.dto.CreateAssetUnityRequest;
import br.com.ale.infrastructure.db.ConnectionProvider;

import java.sql.Connection;
import java.util.List;

public class AssetUnityService {

    private final ConnectionProvider connectionProvider;
    private final AssetUnityDAO assetUnityDAO = new AssetUnityDAO();

    public AssetUnityService(
            ConnectionProvider connectionProvider
    ) {
        this.connectionProvider = connectionProvider;
    }

    public AssetUnity createAssetUnity(CreateAssetUnityRequest request) {

        try (Connection conn = connectionProvider.getConnection()) {
            conn.setAutoCommit(false);

            try {

                AssetUnity asset = assetUnityDAO.insert(conn, request);

                conn.commit();

                return asset;

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while creating assetUnity " +
                            "[assetId=" + request.assetId() +
                            ", ownerAccountId=" + request.ownerAccountId() + "]",
                    e
            );
        }
    }

    public AssetUnity selectById(long assetUnityId) {
        try (Connection conn = connectionProvider.getConnection()) {
            return assetUnityDAO.selectById(conn, assetUnityId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "AssetUnity not found [assetId=" + assetUnityId + "]"
                            )

                    );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while selecting assetUnity " +
                            "[assetId=" + assetUnityId + "]",
                    e
            );
        }
    }

    public List<AssetUnity> selectByOwnerAccount(long ownerAccountId) {
        try (Connection conn = connectionProvider.getConnection()) {
            return assetUnityDAO.selectByOwnerAccount(conn, ownerAccountId);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while selecting asset unity " +
                            "[ownerAccountId=" + ownerAccountId + "]",
                    e
            );
        }
    }
}
