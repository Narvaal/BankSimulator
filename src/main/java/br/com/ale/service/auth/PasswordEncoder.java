package br.com.ale.service.auth;

public interface PasswordEncoder {
    public String encode(String raw);

    public boolean matches(String raw, String encoded);
}
