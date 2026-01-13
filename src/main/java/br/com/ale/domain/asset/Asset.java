package br.com.ale.domain.asset;

import java.time.Instant;

public class Asset {

    private final Long id;
    private final String text;
    private final int totalSupply;
    private final Instant createdAt;

    public Asset(
            Long id,
            String text,
            int totalSupply,
            Instant createdAt
    ) {
        this.id = id;
        this.text = validateText(text);
        this.totalSupply = validateTotalSupply(totalSupply);
        this.createdAt = createdAt;
    }

    public String validateText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Asset text cannot be blank" + "[text=" + text + "]"
            );
        }
        return text;
    }

    public int validateTotalSupply(int totalSupply) {
        if (totalSupply < 1) {
            throw new IllegalArgumentException(
                    "Asset total supply cannot be less than one" + "[totalSupply=" + totalSupply + "]"
            );
        }
        return totalSupply;
    }

    public Long getId() { return id; }
    public String getText() { return text; }
    public int getTotalSupply() { return totalSupply; }
    public Instant getCreatedAt() { return createdAt; }
}
