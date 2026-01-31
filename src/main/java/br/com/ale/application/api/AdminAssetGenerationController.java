package br.com.ale.application.api;

import br.com.ale.domain.asset.Asset;
import br.com.ale.service.asset.AssetGenerationManager;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/assets")
public class AdminAssetGenerationController {

    private final AssetGenerationManager assetGenerationManager;
    private final String adminToken;

    public AdminAssetGenerationController(
            AssetGenerationManager assetGenerationManager,
            @Value("${admin.trigger.token:}") String adminToken
    ) {
        this.assetGenerationManager = assetGenerationManager;
        this.adminToken = adminToken;
    }

    @PostMapping("/generate")
    public ResponseEntity<List<Asset>> generate(
            @RequestHeader(value = "X-Admin-Token", required = false) String token
    ) {
        if (adminToken == null || adminToken.isBlank() || !adminToken.equals(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(assetGenerationManager.generateWeeklyAssets());
    }
}
