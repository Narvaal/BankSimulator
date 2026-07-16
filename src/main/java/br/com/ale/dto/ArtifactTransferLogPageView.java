package br.com.ale.dto;

import java.util.List;

public record ArtifactTransferLogPageView(
    List<ArtifactTransferLogView> items, int page, int pageSize, int totalPages, long totalItems) {}
