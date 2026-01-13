package br.com.ale.service.asset;

import br.com.ale.domain.asset.Asset;
import br.com.ale.infrastructure.json.JsonUtils;
import br.com.ale.util.RandomUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AssetGenerationService {

    public static final int NUMBER_OF_ASSETS_PER_WEEK = 10;

    public List<Asset> generateWeeklyAssets() {

        List<String> nouns = JsonUtils.readArray("words/nouns.json");
        List<String> verbs = JsonUtils.readArray("words/verbs.json");
        List<String> adjectives = JsonUtils.readArray("words/adjs.json");

        List<Asset> assets = new ArrayList<>();

        for (int i = 0; i < NUMBER_OF_ASSETS_PER_WEEK; i++) {
            assets.add(
                    generate(
                            RandomUtils.pickRandom(adjectives) + " " +
                                    RandomUtils.pickRandom(nouns) + " " +
                                    RandomUtils.pickRandom(verbs) + " " +
                                    RandomUtils.pickRandom(nouns)
                    )
            );
        }

        return assets;
    }

    private Asset generate(String phrase) {
        return new Asset(
                null,
                phrase + " • " + UUID.randomUUID().toString().substring(0, 6),
                100,
                Instant.now()
        );
    }
}
