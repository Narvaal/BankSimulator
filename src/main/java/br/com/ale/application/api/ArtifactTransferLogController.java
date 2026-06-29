package br.com.ale.application.api;

import br.com.ale.application.marketplace.query.ListArtifactTransferLogUseCase;
import br.com.ale.dto.ArtifactTransferLogPageView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/artifact-transfers")
public class ArtifactTransferLogController {

    private final ListArtifactTransferLogUseCase listArtifactTransferLogUseCase;

    public ArtifactTransferLogController(ListArtifactTransferLogUseCase listArtifactTransferLogUseCase) {
        this.listArtifactTransferLogUseCase = listArtifactTransferLogUseCase;
    }

    @GetMapping("")
    public ArtifactTransferLogPageView list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int pageSize,
            @RequestParam(required = false) Long artifactId
    ) {
        return listArtifactTransferLogUseCase.execute(artifactId, page, pageSize);
    }
}
