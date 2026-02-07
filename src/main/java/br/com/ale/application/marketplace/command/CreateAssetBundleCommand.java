package br.com.ale.application.marketplace.command;

import br.com.ale.dto.CreateAssetRequest;

import java.util.List;

public record CreateAssetBundleCommand(List<CreateAssetRequest> assetRequests,
                                       String identifier) {
}
