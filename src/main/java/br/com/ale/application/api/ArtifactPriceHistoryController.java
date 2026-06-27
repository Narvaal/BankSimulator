package br.com.ale.application.api;

import br.com.ale.application.marketplace.query.ListArtifactPriceHistoryByListingUseCase;
import br.com.ale.domain.artifact.ArtifactPriceHistory;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/artifact-listings")
public class ArtifactPriceHistoryController {

    private final ListArtifactPriceHistoryByListingUseCase listArtifactPriceHistoryByListingUseCase;

    public ArtifactPriceHistoryController(
            ListArtifactPriceHistoryByListingUseCase listArtifactPriceHistoryByListingUseCase
    ) {
        this.listArtifactPriceHistoryByListingUseCase = listArtifactPriceHistoryByListingUseCase;
    }

    @GetMapping("/{id}/price-history")
    public List<ArtifactPriceHistory> listByListing(@PathVariable("id") long id) {
        return listArtifactPriceHistoryByListingUseCase.execute(id);
    }
}
