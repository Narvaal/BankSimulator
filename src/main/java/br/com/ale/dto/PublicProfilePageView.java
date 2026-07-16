package br.com.ale.dto;

import java.util.List;

public record PublicProfilePageView(
    List<PublicProfileResponse> items, int page, int pageSize, int totalPages, long totalItems) {}
