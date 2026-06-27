package br.com.ale.application.scheduling;

import br.com.ale.service.artifact.ArtifactGenerationManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ArtifactGenerationScheduler {

    private final ArtifactGenerationManager assetGenerationManager;

    public ArtifactGenerationScheduler(ArtifactGenerationManager assetGenerationManager) {
        this.assetGenerationManager = assetGenerationManager;
    }

    @Scheduled(cron = "0 0 8 * * ?", zone = "UTC")
    public void generateDailyAssets() {
        assetGenerationManager.generateWeeklyAssets();
    }
}
