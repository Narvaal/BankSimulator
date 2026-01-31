package br.com.ale.application.api;

import br.com.ale.domain.asset.Asset;
import br.com.ale.service.asset.AssetGenerationManager;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/assets")
public class AdminAssetGenerationController {

    private final AssetGenerationManager assetGenerationManager;

    public AdminAssetGenerationController(
            AssetGenerationManager assetGenerationManager
    ) {
        this.assetGenerationManager = assetGenerationManager;
    }

    @PostMapping("/generate")
    public ResponseEntity<List<Asset>> generate() {
        return ResponseEntity.ok(assetGenerationManager.generateWeeklyAssets());
    }
}
