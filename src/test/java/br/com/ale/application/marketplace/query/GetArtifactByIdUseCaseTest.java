package br.com.ale.application.marketplace.query;

import static org.junit.jupiter.api.Assertions.*;

import br.com.ale.domain.artifact.Artifact;
import br.com.ale.dto.CreateArtifactRequest;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import br.com.ale.service.artifact.ArtifactService;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GetArtifactByIdUseCaseTest {

  private TestConnectionProvider provider;
  private ArtifactService artifactService;
  private GetArtifactByIdUseCase useCase;

  @BeforeEach
  void setup() {
    provider = new TestConnectionProvider();
    artifactService = new ArtifactService(provider);
    useCase = new GetArtifactByIdUseCase(artifactService);
    cleanDatabase();
  }

  private void cleanDatabase() {
    try (var conn = provider.getConnection();
        var stmt = conn.createStatement()) {
      stmt.execute("DELETE FROM artifact_price_history");
      stmt.execute("DELETE FROM artifact_transfer");
      stmt.execute("DELETE FROM artifact_listing");
      stmt.execute("DELETE FROM artifact_unit");
      stmt.execute("DELETE FROM artifact");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Artifact createArtifact(String name, int supply) {
    return artifactService.createAsset(
        new CreateArtifactRequest(Map.of("name", name, "rarity", "Common"), supply));
  }

  @Test
  void shouldReturnArtifactWhenFound() {
    Artifact created = createArtifact("AI Titan #001", 10);

    Artifact result = useCase.execute(created.getId());

    assertNotNull(result);
    assertEquals(created.getId(), result.getId());
  }

  @Test
  void shouldReturnCorrectFields() {
    Artifact created = createArtifact("Quantum Leap", 5);

    Artifact result = useCase.execute(created.getId());

    assertEquals("Quantum Leap", result.getName());
    assertEquals(5, result.getTotalSupply());
    assertNotNull(result.getCreatedAt());
  }

  @Test
  void shouldThrowWhenArtifactDoesNotExist() {
    RuntimeException ex = assertThrows(RuntimeException.class, () -> useCase.execute(99999L));
    assertTrue(
        ex.getMessage().contains("99999")
            || (ex.getCause() != null && ex.getCause().getMessage().contains("99999")),
        "Exception should reference the missing id");
  }

  @Test
  void shouldDistinguishBetweenDifferentArtifacts() {
    Artifact a1 = createArtifact("First Card", 3);
    Artifact a2 = createArtifact("Second Card", 7);

    Artifact result1 = useCase.execute(a1.getId());
    Artifact result2 = useCase.execute(a2.getId());

    assertEquals("First Card", result1.getName());
    assertEquals(3, result1.getTotalSupply());
    assertEquals("Second Card", result2.getName());
    assertEquals(7, result2.getTotalSupply());
    assertNotEquals(result1.getId(), result2.getId());
  }
}
