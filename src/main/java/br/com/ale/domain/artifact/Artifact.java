package br.com.ale.domain.artifact;

import java.time.Instant;

public class Artifact {

    private final Long id;
    private final String text;
    private final int totalSupply;
    private final Instant createdAt;

    public Artifact(Long id, String text, int totalSupply, Instant createAt) {
        this.id = id;
        this.text = validateText(text);
        this.totalSupply = validateTotalSupply(totalSupply);
        this.createdAt = createAt;
    }

    public Artifact(String text, int totalSupply) {
        this(null, text, totalSupply, null);
    }

    private String validateText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Artifact text cannot be blank [text=" + text + "]"
            );
        }
        return text;
    }

    private int validateTotalSupply(int totalSupply) {
        if (totalSupply < 0) {
            throw new IllegalArgumentException(
                    "Artifact total supply cannot be less than zero [totalSupply=" + totalSupply + "]"
            );
        }
        return totalSupply;
    }

    public Long getId() { return id; }
    public String getText() { return text; }
    public int getTotalSupply() { return totalSupply; }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
