package br.com.ale.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KofiWebHookResponse(
        @JsonProperty("verification_token")
        String token,

        @JsonProperty("email")
        String email,

        @JsonProperty("amount")
        String amount,

        @JsonProperty("currency")
        String currency

) {
}