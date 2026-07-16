package br.com.ale.dto;

public record PublicProfileResponse(
    long accountId, String name, String picture, String accountNumber) {}
