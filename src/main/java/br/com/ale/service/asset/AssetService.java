package br.com.ale.service.asset;

import br.com.ale.dao.asset.AssetDAO;
import br.com.ale.domain.asset.Asset;
import br.com.ale.dto.CreateAssetRequest;
import br.com.ale.infrastructure.db.ConnectionProvider;

import java.sql.Connection;

public class AssetService {

    private final ConnectionProvider connectionProvider;
    private final AssetDAO assetDAO = new AssetDAO();

    public AssetService(
            ConnectionProvider connectionProvider
    ) {
        this.connectionProvider = connectionProvider;
    }

    public Asset createAsset(CreateAssetRequest request) {

        if (request.totalSupply() < 1) {
            throw new RuntimeException(
                    "Asset total supply must be more than 0 " +
                            "[totalSupply=" + request.totalSupply() + "]"
            );
        }

        try (Connection conn = connectionProvider.getConnection()) {
            conn.setAutoCommit(false);

            try {

                Asset asset = assetDAO.insert(conn, request);

                conn.commit();

                return asset;

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while creating asset " +
                            "[text=" + request.text() +
                            ", totalSupply=" + request.totalSupply() + "]",
                    e
            );
        }
    }

    public Asset selectById(long assetId) {
        try (Connection conn = connectionProvider.getConnection()) {
            return assetDAO.selectById(conn, assetId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Asset not found [assetId=" + assetId + "]"
                            )

                    );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while selecting asset " +
                            "[assetId=" + assetId + "]",
                    e
            );
        }
    }
}
