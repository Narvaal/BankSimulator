package br.com.ale.dto;

import java.util.List;

public record CreateAssetBundleApiRequest(
        List<CreateAssetRequest> assets,
        String identifier
) {
}
