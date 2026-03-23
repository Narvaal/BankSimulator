package br.com.ale.service.asset;

import br.com.ale.domain.asset.Asset;
import br.com.ale.infrastructure.json.JsonUtils;
import br.com.ale.util.RandomUtils;

import java.util.ArrayList;
import java.util.List;

public class AssetGenerationService {

    public static final int NUMBER_OF_ASSETS_PER_WEEK = 12;
    public static final int TOTAL_ASSETS_SUPPLY = 10;

    private final List<String> nouns = JsonUtils.readArray("words/nouns.json");
    private final List<String> verbs = JsonUtils.readArray("words/verbs.json");
    private final List<String> adjectives = JsonUtils.readArray("words/adjs.json");

    public List<Asset> generateWeeklyAssets() {

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
                phrase,
                TOTAL_ASSETS_SUPPLY
        );
    }
}
