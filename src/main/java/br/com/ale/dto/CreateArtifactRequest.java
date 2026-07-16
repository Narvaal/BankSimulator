package br.com.ale.dto;

import java.util.Map;

public record CreateArtifactRequest(Map<String, Object> metadata, int totalSupply) {}
