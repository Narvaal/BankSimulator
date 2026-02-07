package br.com.ale.infrastructure.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

// TODO: Change for production
@Component
public class AuthCookieService {

    public void addAuthCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("AUTH_TOKEN", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // true (HTTPS)
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24); // 1 day
        cookie.setAttribute("SameSite", "Strict");

        response.addCookie(cookie);
    }
}
