package br.com.ale.service.artifact;

import br.com.ale.dao.artifact.ArtifactBundleDAO;
import br.com.ale.dao.artifact.ArtifactBundleItemDAO;
import br.com.ale.dao.artifact.ArtifactDAO;
import br.com.ale.domain.artifact.Artifact;
import br.com.ale.domain.artifact.ArtifactBundle;
import br.com.ale.dto.ArtifactBundleItemResponse;
import br.com.ale.dto.ArtifactBundleResponse;
import br.com.ale.dto.CreateArtifactRequest;
import br.com.ale.infrastructure.db.ConnectionProvider;
import br.com.ale.infrastructure.json.JsonUtils;
import br.com.ale.util.RandomUtils;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class ArtifactBundleService {

  private final ConnectionProvider connectionProvider;
  private final ArtifactDAO assetDAO = new ArtifactDAO();
  private final ArtifactBundleDAO artifactBundleDAO = new ArtifactBundleDAO();
  private final ArtifactBundleItemDAO artifactBundleItemDAO = new ArtifactBundleItemDAO();
  private final List<String> words = JsonUtils.readArray("words/common.json");
  private final List<String> emoji = JsonUtils.readArray("words/emoji.json");

  public ArtifactBundleService(ConnectionProvider connectionProvider) {
    this.connectionProvider = connectionProvider;
  }

  public List<Artifact> createWeeklyBundle(List<Artifact> generatedAssets) {
    List<CreateArtifactRequest> requests = new ArrayList<>(generatedAssets.size());
    for (Artifact artifact : generatedAssets) {
      requests.add(new CreateArtifactRequest(artifact.getMetadata(), artifact.getTotalSupply()));
    }
    return createBundleInternal(requests, null).assets();
  }

  public ArtifactBundleResponse createBundle(
      List<CreateArtifactRequest> assetRequests, String identifier) {
    CreatedBundle created = createBundleInternal(assetRequests, identifier);
    ArtifactBundle bundle = created.bundle();
    return new ArtifactBundleResponse(
        bundle.getId(), bundle.getIdentifier(), bundle.getCreatedAt());
  }

  public List<ArtifactBundleResponse> listBundles(int page, int size) {
    try (Connection conn = connectionProvider.getConnection()) {
      List<ArtifactBundleResponse> responses = new ArrayList<>();
      for (ArtifactBundle bundle : artifactBundleDAO.selectAll(conn, page, size)) {
        responses.add(
            new ArtifactBundleResponse(
                bundle.getId(), bundle.getIdentifier(), bundle.getCreatedAt()));
      }
      return responses;
    } catch (Exception e) {
      throw new RuntimeException("Service error while listing bundles", e);
    }
  }

  public List<ArtifactBundleItemResponse> listBundleItems(long bundleId, int page, int size) {
    try (Connection conn = connectionProvider.getConnection()) {
      return artifactBundleItemDAO.selectItemsByBundleId(conn, bundleId, page, size);
    } catch (Exception e) {
      throw new RuntimeException(
          "Service error while listing bundle items " + "[bundleId=" + bundleId + "]", e);
    }
  }

  private String generateIdentifier() {
    return RandomUtils.pickRandom(words) + " " + RandomUtils.pickRandom(emoji);
  }

  private CreatedBundle createBundleInternal(
      List<CreateArtifactRequest> assetRequests, String identifier) {
    if (assetRequests == null || assetRequests.isEmpty()) {
      throw new IllegalArgumentException("Artifact bundle must include at least one artifact");
    }

    String bundleIdentifier =
        (identifier == null || identifier.isBlank()) ? generateIdentifier() : identifier;

    try (Connection conn = connectionProvider.getConnection()) {
      conn.setAutoCommit(false);

      try {
        ArtifactBundle bundle = artifactBundleDAO.insert(conn, bundleIdentifier);

        List<Artifact> persisted = new ArrayList<>(assetRequests.size());
        for (CreateArtifactRequest request : assetRequests) {
          persisted.add(assetDAO.insert(conn, request));
        }

        List<Long> artifactIds = new ArrayList<>(persisted.size());
        for (Artifact artifact : persisted) {
          artifactIds.add(artifact.getId());
        }

        artifactBundleItemDAO.insertItems(conn, bundle.getId(), artifactIds);

        conn.commit();
        return new CreatedBundle(bundle, persisted);
      } catch (Exception e) {
        conn.rollback();
        throw e;
      }
    } catch (Exception e) {
      throw new RuntimeException(
          "Service error while creating artifact bundle " + "[identifier=" + bundleIdentifier + "]",
          e);
    }
  }

  private record CreatedBundle(ArtifactBundle bundle, List<Artifact> assets) {}
}
