package br.com.ale.service.artifact;

import br.com.ale.dao.artifact.ArtifactDAO;
import br.com.ale.domain.artifact.Artifact;
import br.com.ale.dto.ArtifactSummaryResponse;
import br.com.ale.dto.CreateArtifactRequest;
import br.com.ale.infrastructure.db.ConnectionProvider;
import java.sql.Connection;
import java.util.List;

public class ArtifactService {

  private final ConnectionProvider connectionProvider;
  private final ArtifactDAO assetDAO = new ArtifactDAO();

  public ArtifactService(ConnectionProvider connectionProvider) {
    this.connectionProvider = connectionProvider;
  }

  public Artifact createAsset(CreateArtifactRequest request) {

    if (request.totalSupply() < 1) {
      throw new RuntimeException(
          "Artifact total supply must be more than 0 "
              + "[totalSupply="
              + request.totalSupply()
              + "]");
    }

    try (Connection conn = connectionProvider.getConnection()) {
      conn.setAutoCommit(false);

      try {

        Artifact artifact = assetDAO.insert(conn, request);

        conn.commit();

        return artifact;

      } catch (Exception e) {
        conn.rollback();
        throw e;
      }

    } catch (Exception e) {
      throw new RuntimeException(
          "Service error while creating artifact [totalSupply=" + request.totalSupply() + "]", e);
    }
  }

  public Artifact selectById(long artifactId) {
    try (Connection conn = connectionProvider.getConnection()) {
      return assetDAO
          .selectById(conn, artifactId)
          .orElseThrow(
              () -> new RuntimeException("Artifact not found [artifactId=" + artifactId + "]"));

    } catch (Exception e) {
      throw new RuntimeException(
          "Service error while selecting artifact " + "[artifactId=" + artifactId + "]", e);
    }
  }

  public List<ArtifactSummaryResponse> listArtifacts() {
    try (Connection conn = connectionProvider.getConnection()) {
      return assetDAO.selectAllSummaries(conn);
    } catch (Exception e) {
      throw new RuntimeException("Service error while selecting assets", e);
    }
  }
}
