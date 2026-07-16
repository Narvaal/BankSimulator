package br.com.ale.application.marketplace.command;

import java.math.BigDecimal;

public record CreateArtifactOfferCommand(long artifactUnitId, BigDecimal price, String token) {}
