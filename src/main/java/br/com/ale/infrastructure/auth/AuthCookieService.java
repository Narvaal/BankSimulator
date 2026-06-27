package br.com.ale.infrastructure.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@Component
public class AuthCookieService {

    private static final String AUTH_COOKIE_NAME = "AUTH_TOKEN";

    @Value("${auth.cookie.domain:}")
    private String cookieDomain;

    @Value("${auth.cookie.secure:true}")
    private boolean cookieSecure;

    @Value("${auth.cookie.same-site:None}")
    private String cookieSameSite;

    public void addAuthCookie(HttpServletResponse response, String token) {

        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from("AUTH_TOKEN", token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(Duration.ofDays(1));

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }

        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }

    public String extractToken(HttpServletRequest request) {
        String token = extractTokenOrNull(request);
        if (token == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        return token;
    }

    public String extractTokenOrNull(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7).trim();
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if (AUTH_COOKIE_NAME.equals(cookie.getName())) {
                String token = cookie.getValue();
                return (token == null || token.isBlank()) ? null : token;
            }
        }

        return null;
    }
}
