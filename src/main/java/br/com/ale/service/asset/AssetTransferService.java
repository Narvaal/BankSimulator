package br.com.ale.service.asset;

import br.com.ale.dao.asset.AssetTransferDAO;
import br.com.ale.domain.asset.AssetTransfer;
import br.com.ale.dto.CreateAssetTransferRequest;
import br.com.ale.infrastructure.db.ConnectionProvider;

import java.sql.Connection;

public class AssetTransferService {

    private final ConnectionProvider connectionProvider;
    private final AssetTransferDAO assetTransferDAO = new AssetTransferDAO();

    public AssetTransferService(
            ConnectionProvider connectionProvider
    ) {
        this.connectionProvider = connectionProvider;
    }

    public AssetTransfer createAsset(CreateAssetTransferRequest request) {

        if (request.fromAccountId() == request.toAccountId()) {
            throw new RuntimeException(
                    "Not allowed asset transfer to the same account " +
                            "[fromAccountId=" + request.fromAccountId() +
                            ", toAccountId=" + request.toAccountId() + "]"
            );
        }

        try (Connection conn = connectionProvider.getConnection()) {
            conn.setAutoCommit(false);

            try {

                AssetTransfer asset = assetTransferDAO.insert(conn, request);

                conn.commit();

                return asset;

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while creating asset Transfer[assetUnityId=" + request.assetUnityId() +
                            ", fromAccountId=" + request.fromAccountId() + ", " +
                            ", toAccountId=" + request.toAccountId() + "]"
            );
        }
    }

    public AssetTransfer selectById(long assetTransferId) {
        try (Connection conn = connectionProvider.getConnection()) {
            return assetTransferDAO.selectById(conn, assetTransferId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Asset transfer not found [assetId=" + assetTransferId + "]"
                            )

                    );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while selecting asset transfer " +
                            "[assetId=" + assetTransferId + "]",
                    e
            );
        }
    }
}
