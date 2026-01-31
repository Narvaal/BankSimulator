package br.com.ale.application.api;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AdminTokenFilter extends OncePerRequestFilter {

    private static final String ADMIN_TOKEN_HEADER = "X-Admin-Token";

    private final String adminToken;

    public AdminTokenFilter(
            @Value("${admin.trigger.token:}") String adminToken
    ) {
        this.adminToken = adminToken;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!requiresAdminToken(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (adminToken == null || adminToken.isBlank()) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return;
        }

        String token = request.getHeader(ADMIN_TOKEN_HEADER);
        if (token == null || !adminToken.equals(token)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean requiresAdminToken(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path == null) {
            return false;
        }

        if (path.startsWith("/admin/")) {
            return true;
        }

        return "POST".equalsIgnoreCase(request.getMethod())
                && "/assets/bundles".equals(path);
    }
}
