package br.com.ale.service.artifact;

import br.com.ale.domain.artifact.Artifact;
import br.com.ale.dto.ArtifactBundleItemResponse;
import br.com.ale.dto.ArtifactBundleResponse;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import br.com.ale.infrastructure.json.JsonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ArtifactBundleServiceTest {

    private TestConnectionProvider provider;
    private ArtifactBundleService service;

    @BeforeEach
    void setup() {
        provider = new TestConnectionProvider();
        service = new ArtifactBundleService(provider);
        cleanDatabase();
    }

    @Test
    void shouldCreateWeeklyBundleAndPersistAssets() {
        List<Artifact> assets = List.of(
                new Artifact(uniqueText("alpha"), 100),
                new Artifact(uniqueText("beta"), 120),
                new Artifact(uniqueText("gamma"), 150)
        );

        List<Artifact> persisted = service.createWeeklyBundle(assets);

        assertEquals(assets.size(), persisted.size());
        for (Artifact artifact : persisted) {
            assertNotNull(artifact.getId());
            assertTrue(artifact.getId() > 0);
        }

        Set<String> expectedTexts = new HashSet<>();
        for (Artifact artifact : assets) {
            expectedTexts.add(artifact.getText());
        }

        Set<String> persistedTexts = new HashSet<>();
        for (Artifact artifact : persisted) {
            persistedTexts.add(artifact.getText());
        }
        assertEquals(expectedTexts, persistedTexts);

        List<ArtifactBundleResponse> bundles = service.listBundles(0, 10);
        assertEquals(1, bundles.size());

        List<ArtifactBundleItemResponse> items =
                service.listBundleItems(bundles.get(0).id(), 0, 10);

        assertEquals(assets.size(), items.size());

        Set<String> itemTexts = new HashSet<>();
        for (ArtifactBundleItemResponse item : items) {
            itemTexts.add(item.text());
        }

        assertEquals(expectedTexts, itemTexts);
    }

    @Test
    void shouldGenerateIdentifierWithRandomWordAndEmoji() {
        service.createWeeklyBundle(List.of(
                new Artifact(uniqueText("delta"), 100)
        ));

        List<ArtifactBundleResponse> bundles = service.listBundles(0, 10);
        assertEquals(1, bundles.size());

        String identifier = bundles.get(0).identifier();
        String[] parts = identifier.split(" ", 2);

        assertEquals(2, parts.length, "Expected identifier in format 'word emoji'");

        List<String> words = new ArrayList<>();
        words.addAll(JsonUtils.readArray("words/common.json"));
        words.addAll(JsonUtils.readArray("words/nouns.json"));
        words.addAll(JsonUtils.readArray("words/verbs.json"));
        words.addAll(JsonUtils.readArray("words/adjs.json"));

        List<String> emojis = JsonUtils.readArray("words/emoji.json");

        assertTrue(words.contains(parts[0]), "Expected word to be from word lists");
        assertTrue(emojis.contains(parts[1]), "Expected emoji to be from emoji list");
    }

    private String uniqueText(String base) {
        return base + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private void cleanDatabase() {
        try (var conn = provider.getConnection();
             var stmt = conn.createStatement()) {

            stmt.execute("DELETE FROM artifact_price_history");
            stmt.execute("DELETE FROM artifact_transfer");
            stmt.execute("DELETE FROM artifact_listing");
            stmt.execute("DELETE FROM artifact_bundle_item");
            stmt.execute("DELETE FROM artifact_unit");
            stmt.execute("DELETE FROM artifact_bundle");
            stmt.execute("DELETE FROM artifact");
            stmt.execute("DELETE FROM transactions");
            stmt.execute("DELETE FROM account");
            stmt.execute("DELETE FROM credential");
            stmt.execute("DELETE FROM client");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
