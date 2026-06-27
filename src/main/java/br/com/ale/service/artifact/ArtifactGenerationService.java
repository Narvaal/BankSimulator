package br.com.ale.service.artifact;

import br.com.ale.domain.artifact.Artifact;
import br.com.ale.infrastructure.json.JsonUtils;
import br.com.ale.util.RandomUtils;

import java.util.ArrayList;
import java.util.List;

public class ArtifactGenerationService {

    public static final int NUMBER_OF_ASSETS_PER_WEEK = 12;
    public static final int TOTAL_ASSETS_SUPPLY = 10;

    private final List<String> nouns = JsonUtils.readArray("words/nouns.json");
    private final List<String> verbs = JsonUtils.readArray("words/verbs.json");
    private final List<String> adjectives = JsonUtils.readArray("words/adjs.json");

    public List<Artifact> generateWeeklyAssets() {

        List<Artifact> assets = new ArrayList<>();

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

    private Artifact generate(String phrase) {
        return new Artifact(
                phrase,
                TOTAL_ASSETS_SUPPLY
        );
    }
}
