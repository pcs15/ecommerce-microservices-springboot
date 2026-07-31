package com.ecommerce.gateway.security;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ecommerce.gateway.config.RouteConfig;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Pertahanan lapis pertama (gateway): validasi JWT + role-check per route
 * sebelum request diteruskan ke service tujuan. Tiap service juga
 * validasi JWT sendiri (defense in depth) - lihat SecurityConfig masing2 service.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RouteConfig routeConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Lewati endpoint Swagger agar dokumentasi API dapat diakses tanpa JWT.
        if (path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/swagger-ui.html")) {
            chain.doFilter(request, response);
            return;
        }

        if (routeConfig.isPublic(path)) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            unauthorized(response, "Token tidak ada atau format salah, harus 'Bearer <token>'.");
            return;
        }

        String token = authHeader.substring(7);
        Claims claims;
        try {
            claims = jwtUtil.parseClaims(token);
        } catch (JwtException e) {
            unauthorized(response, "Token invalid atau expired: " + e.getMessage());
            return;
        }

        String role = claims.get("role", String.class);
        boolean isAdmin = "ROLE_ADMIN".equals(role);

        // Domain rule: ADMIN selalu lolos (superadmin). Selain itu, employee
        // domain (karyawan/absensi/izin-cuti) cuma buat ROLE_EMPLOYEE, customer
        // domain (customers/orders) cuma buat ROLE_USER - saling exclude.
        if (!isAdmin) {
            if (routeConfig.isEmployeeDomain(path) && !"ROLE_EMPLOYEE".equals(role)) {
                forbidden(response, "Endpoint ini khusus staff (ROLE_EMPLOYEE).");
                return;
            }
            if (routeConfig.isCustomerDomain(path) && !"ROLE_USER".equals(role)) {
                forbidden(response, "Endpoint ini khusus customer (ROLE_USER).");
                return;
            }
        }

        // Rule role sederhana: method DELETE = ROLE_ADMIN only, selain itu cukup
        // punya token valid & lolos domain rule di atas. PUT kurangi-stok termasuk
        // "selain itu" jadi ROLE_USER tetap lolos (dipanggil dari alur checkout).
        if ("DELETE".equalsIgnoreCase(request.getMethod()) && !isAdmin) {
            forbidden(response, "Endpoint ini butuh ROLE_ADMIN.");
            return;
        }

        chain.doFilter(request, response);
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message.replace("\"", "'") + "\"}");
    }

    private void forbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message.replace("\"", "'") + "\"}");
    }
}