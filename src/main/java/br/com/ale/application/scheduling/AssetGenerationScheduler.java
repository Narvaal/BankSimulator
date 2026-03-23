package br.com.ale.application.scheduling;

import br.com.ale.service.asset.AssetGenerationManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AssetGenerationScheduler {

    private final AssetGenerationManager assetGenerationManager;

    public AssetGenerationScheduler(AssetGenerationManager assetGenerationManager) {
        this.assetGenerationManager = assetGenerationManager;
    }

    @Scheduled(cron = "0 0 8 * * ?", zone = "UTC")
    public void generateDailyAssets() {
        assetGenerationManager.generateWeeklyAssets();
    }
}
