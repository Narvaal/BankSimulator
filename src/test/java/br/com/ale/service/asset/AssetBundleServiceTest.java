package br.com.ale.service.asset;

import br.com.ale.domain.asset.Asset;
import br.com.ale.dto.AssetBundleItemResponse;
import br.com.ale.dto.AssetBundleResponse;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import br.com.ale.infrastructure.json.JsonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssetBundleServiceTest {

    private TestConnectionProvider provider;
    private AssetBundleService service;

    @BeforeEach
    void setup() {
        provider = new TestConnectionProvider();
        service = new AssetBundleService(provider);
        cleanDatabase();
    }

    @Test
    void shouldCreateWeeklyBundleAndPersistAssets() {
        List<Asset> assets = List.of(
                new Asset(uniqueText("alpha"), 100),
                new Asset(uniqueText("beta"), 120),
                new Asset(uniqueText("gamma"), 150)
        );

        List<Asset> persisted = service.createWeeklyBundle(assets);

        assertEquals(assets.size(), persisted.size());
        for (Asset asset : persisted) {
            assertNotNull(asset.getId());
            assertTrue(asset.getId() > 0);
        }

        Set<String> expectedTexts = new HashSet<>();
        for (Asset asset : assets) {
            expectedTexts.add(asset.getText());
        }

        Set<String> persistedTexts = new HashSet<>();
        for (Asset asset : persisted) {
            persistedTexts.add(asset.getText());
        }
        assertEquals(expectedTexts, persistedTexts);

        List<AssetBundleResponse> bundles = service.listBundles(0, 10);
        assertEquals(1, bundles.size());

        List<AssetBundleItemResponse> items =
                service.listBundleItems(bundles.get(0).id(), 10, 20);

        assertEquals(assets.size(), items.size());

        Set<String> itemTexts = new HashSet<>();
        for (AssetBundleItemResponse item : items) {
            itemTexts.add(item.text());
        }

        assertEquals(expectedTexts, itemTexts);
    }

    @Test
    void shouldGenerateIdentifierWithRandomWordAndEmoji() {
        service.createWeeklyBundle(List.of(
                new Asset(uniqueText("delta"), 100)
        ));

        List<AssetBundleResponse> bundles = service.listBundles(0, 10);
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

            stmt.execute("DELETE FROM asset_price_history");
            stmt.execute("DELETE FROM asset_transfer");
            stmt.execute("DELETE FROM asset_listing");
            stmt.execute("DELETE FROM asset_bundle_item");
            stmt.execute("DELETE FROM asset_unit");
            stmt.execute("DELETE FROM asset_bundle");
            stmt.execute("DELETE FROM asset");
            stmt.execute("DELETE FROM transactions");
            stmt.execute("DELETE FROM account");
            stmt.execute("DELETE FROM credential");
            stmt.execute("DELETE FROM client");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
