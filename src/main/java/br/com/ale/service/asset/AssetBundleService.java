package br.com.ale.service.asset;

import br.com.ale.dao.asset.AssetBundleDAO;
import br.com.ale.dao.asset.AssetBundleItemDAO;
import br.com.ale.dao.asset.AssetDAO;
import br.com.ale.domain.asset.Asset;
import br.com.ale.domain.asset.AssetBundle;
import br.com.ale.dto.AssetBundleItemResponse;
import br.com.ale.dto.AssetBundleResponse;
import br.com.ale.dto.CreateAssetRequest;
import br.com.ale.infrastructure.db.ConnectionProvider;
import br.com.ale.infrastructure.json.JsonUtils;
import br.com.ale.util.RandomUtils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AssetBundleService {

    private final ConnectionProvider connectionProvider;
    private final AssetDAO assetDAO = new AssetDAO();
    private final AssetBundleDAO assetBundleDAO = new AssetBundleDAO();
    private final AssetBundleItemDAO assetBundleItemDAO = new AssetBundleItemDAO();
    private final List<String> words = JsonUtils.readArray("words/common.json");
    private final List<String> emoji = JsonUtils.readArray("words/emoji.json");

    public AssetBundleService(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public List<Asset> createWeeklyBundle(List<Asset> generatedAssets) {
        List<CreateAssetRequest> requests = new ArrayList<>(generatedAssets.size());
        for (Asset asset : generatedAssets) {
            requests.add(new CreateAssetRequest(asset.getText(), asset.getTotalSupply()));
        }
        return createBundleInternal(requests, null).assets();
    }

    public AssetBundleResponse createBundle(
            List<CreateAssetRequest> assetRequests,
            String identifier
    ) {
        CreatedBundle created = createBundleInternal(assetRequests, identifier);
        AssetBundle bundle = created.bundle();
        return new AssetBundleResponse(
                bundle.getId(),
                bundle.getIdentifier(),
                bundle.getCreatedAt()
        );
    }

    public List<AssetBundleResponse> listBundles() {
        try (Connection conn = connectionProvider.getConnection()) {
            List<AssetBundleResponse> responses = new ArrayList<>();
            for (AssetBundle bundle : assetBundleDAO.selectAll(conn)) {
                responses.add(new AssetBundleResponse(
                        bundle.getId(),
                        bundle.getIdentifier(),
                        bundle.getCreatedAt()
                ));
            }
            return responses;
        } catch (Exception e) {
            throw new RuntimeException("Service error while listing bundles", e);
        }
    }

    public List<AssetBundleItemResponse> listBundleItems(long bundleId) {
        try (Connection conn = connectionProvider.getConnection()) {
            return assetBundleItemDAO.selectItemsByBundleId(conn, bundleId);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while listing bundle items " +
                            "[bundleId=" + bundleId + "]",
                    e
            );
        }
    }

    private String generateIdentifier() {
        return RandomUtils.pickRandom(words) + " " + RandomUtils.pickRandom(emoji);
    }

    private CreatedBundle createBundleInternal(
            List<CreateAssetRequest> assetRequests,
            String identifier
    ) {
        if (assetRequests == null || assetRequests.isEmpty()) {
            throw new IllegalArgumentException("Asset bundle must include at least one asset");
        }

        String bundleIdentifier = (identifier == null || identifier.isBlank())
                ? generateIdentifier()
                : identifier;

        try (Connection conn = connectionProvider.getConnection()) {
            conn.setAutoCommit(false);

            try {
                AssetBundle bundle = assetBundleDAO.insert(conn, bundleIdentifier);

                List<Asset> persisted = new ArrayList<>(assetRequests.size());
                for (CreateAssetRequest request : assetRequests) {
                    persisted.add(assetDAO.insert(conn, request));
                }

                List<Long> assetIds = new ArrayList<>(persisted.size());
                for (Asset asset : persisted) {
                    assetIds.add(asset.getId());
                }

                assetBundleItemDAO.insertItems(conn, bundle.getId(), assetIds);

                conn.commit();
                return new CreatedBundle(bundle, persisted);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while creating asset bundle " +
                            "[identifier=" + bundleIdentifier + "]",
                    e
            );
        }
    }

    private record CreatedBundle(AssetBundle bundle, List<Asset> assets) {
    }
}
