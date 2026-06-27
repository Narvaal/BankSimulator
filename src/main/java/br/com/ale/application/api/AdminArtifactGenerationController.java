package br.com.ale.application.api;

import br.com.ale.domain.artifact.Artifact;
import br.com.ale.service.artifact.ArtifactGenerationManager;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/artifacts")
public class AdminArtifactGenerationController {

    private final ArtifactGenerationManager assetGenerationManager;

    public AdminArtifactGenerationController(
            ArtifactGenerationManager assetGenerationManager
    ) {
        this.assetGenerationManager = assetGenerationManager;
    }

    @PostMapping("/generate")
    public ResponseEntity<List<Artifact>> generate() {
        return ResponseEntity.ok(assetGenerationManager.generateWeeklyAssets());
    }
}
