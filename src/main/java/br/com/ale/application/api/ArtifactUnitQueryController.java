package br.com.ale.application.api;

import br.com.ale.application.marketplace.query.GetArtifactUnitByIdUseCase;
import br.com.ale.application.marketplace.query.ListArtifactUnitsByOwnerUseCase;
import br.com.ale.dto.ArtifactUnitDetailView;
import br.com.ale.dto.ArtifactUnitPageView;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/artifact-units")
public class ArtifactUnitQueryController {

    private final ListArtifactUnitsByOwnerUseCase listArtifactUnitsByOwnerUseCase;
    private final GetArtifactUnitByIdUseCase getArtifactUnitByIdUseCase;

    public ArtifactUnitQueryController(
            ListArtifactUnitsByOwnerUseCase listArtifactUnitsByOwnerUseCase,
            GetArtifactUnitByIdUseCase getArtifactUnitByIdUseCase
    ) {
        this.listArtifactUnitsByOwnerUseCase = listArtifactUnitsByOwnerUseCase;
        this.getArtifactUnitByIdUseCase = getArtifactUnitByIdUseCase;
    }

    @GetMapping("/{id}")
    public ArtifactUnitDetailView getById(@PathVariable("id") long id) {
        return getArtifactUnitByIdUseCase.execute(id);
    }

    @GetMapping
    public ArtifactUnitPageView listByOwner(@RequestParam("ownerId") long ownerId,
                                            @RequestParam("page") int page,
                                            @RequestParam("pageSize") int pageSize) {
        return listArtifactUnitsByOwnerUseCase.execute(ownerId, page, pageSize);
    }
}
