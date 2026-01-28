package br.com.ale.service.auth;
// TODO: FIX ENCODER
public class PlainTextPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(String raw) {
        return raw;
    }

    @Override
    public boolean matches(String raw, String encoded) {
        return raw.equals(encoded);
    }
}
