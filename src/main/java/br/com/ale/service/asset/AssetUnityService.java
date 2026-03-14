package br.com.ale.service.asset;

import br.com.ale.dao.asset.AssetDAO;
import br.com.ale.dao.asset.AssetUnityDAO;
import br.com.ale.domain.asset.AssetUnity;
import br.com.ale.domain.asset.AssetUnityStatus;
import br.com.ale.dto.AssetUnityPageView;
import br.com.ale.dto.AssetUnityView;
import br.com.ale.dto.CreateAssetUnityRequest;
import br.com.ale.infrastructure.db.ConnectionProvider;
import br.com.ale.service.webhook.AssetWebhookNotifier;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public class AssetUnityService {

    private final ConnectionProvider connectionProvider;
    private final AssetWebhookNotifier webhookNotifier;
    private final AssetDAO assetDAO = new AssetDAO();
    private final AssetUnityDAO assetUnityDAO = new AssetUnityDAO();

    public AssetUnityService(ConnectionProvider connectionProvider,
                             AssetWebhookNotifier webhookNotifier) {
        this.connectionProvider = connectionProvider;
        this.webhookNotifier = webhookNotifier;
    }

    public boolean tryUpdateToMarket(long unityId) {
        try (Connection conn = connectionProvider.getConnection()) {
            return assetUnityDAO.tryUpdateToMarket(conn, unityId);
        } catch (Exception e) {
            throw new RuntimeException("Service error while lock for market [unityId=" + unityId + "]", e);
        }
    }

    public AssetUnity createAssetUnity(CreateAssetUnityRequest request) {
        try (Connection conn = connectionProvider.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int updated = assetDAO.updateTotalSupply(conn, request.assetId(), 1);
                if (updated == 0) {
                    throw new RuntimeException("Asset supply not available [assetId=" + request.assetId() + "]");
                }

                AssetUnity unity = assetUnityDAO.insert(conn, request);
                conn.commit();
                webhookNotifier.notifyAssetUnityCreated(unity);
                return unity;

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while creating assetUnity [assetId=" + request.assetId() +
                            ", ownerAccountId=" + request.ownerAccountId() + "]",
                    e
            );
        }
    }

    public AssetUnity selectById(long assetUnityId) {
        try (Connection conn = connectionProvider.getConnection()) {
            return assetUnityDAO.selectById(conn, assetUnityId)
                    .orElseThrow(() -> new RuntimeException("AssetUnity not found [id=" + assetUnityId + "]"));
        } catch (Exception e) {
            throw new RuntimeException("Service error while selecting assetUnity [id=" + assetUnityId + "]", e);
        }
    }

    public AssetUnityPageView selectByOwnerAccount(long ownerAccountId, int page, int pageSize) {
        try (Connection conn = connectionProvider.getConnection()) {
            return assetUnityDAO.selectByOwnerAccount(conn, ownerAccountId, page, pageSize);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while selecting asset unity [ownerAccountId=" + ownerAccountId + "]",
                    e
            );
        }
    }
}
