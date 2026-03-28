package br.com.ale.dto;

import java.util.Map;

public record CreateGoogleAccountRequest(Map<String, String> body) {
}
