package br.com.ale.application.api;

import br.com.ale.application.marketplace.command.CancelAssetCommand;
import br.com.ale.application.marketplace.usecase.CancelAssetOfferUseCase;
import br.com.ale.dto.CancelAssetOfferApiRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/asset-offers")
public class AssetOfferCancelController {

    private final CancelAssetOfferUseCase cancelAssetOfferUseCase;

    public AssetOfferCancelController(CancelAssetOfferUseCase cancelAssetOfferUseCase) {
        this.cancelAssetOfferUseCase = cancelAssetOfferUseCase;
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable("id") long listingId,
            @RequestBody CancelAssetOfferApiRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        String token = extractToken(authorization, request.token());
        CancelAssetCommand command =
                new CancelAssetCommand(request.accountId(), listingId, token);
        cancelAssetOfferUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private String extractToken(String authorization, String fallback) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length()).trim();
        }
        return fallback;
    }
}
