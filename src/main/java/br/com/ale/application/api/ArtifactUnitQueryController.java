package br.com.ale.application.api;

import br.com.ale.application.marketplace.query.ListArtifactUnitsByOwnerUseCase;
import br.com.ale.domain.artifact.ArtifactUnit;
import java.util.List;

import br.com.ale.dto.ArtifactUnitPageView;
import br.com.ale.dto.ArtifactUnitView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/artifact-units")
public class ArtifactUnitQueryController {

    private final ListArtifactUnitsByOwnerUseCase listArtifactUnitsByOwnerUseCase;

    public ArtifactUnitQueryController(ListArtifactUnitsByOwnerUseCase listArtifactUnitsByOwnerUseCase) {
        this.listArtifactUnitsByOwnerUseCase = listArtifactUnitsByOwnerUseCase;
    }

    @GetMapping
    public ArtifactUnitPageView listByOwner(@RequestParam("ownerId") long ownerId,
                                          @RequestParam("page") int page,
                                          @RequestParam("pageSize") int pageSize) {
        return listArtifactUnitsByOwnerUseCase.execute(ownerId, page, pageSize);
    }
}
