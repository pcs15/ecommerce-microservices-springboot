package com.ecommerce.gateway.controller;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import com.ecommerce.gateway.config.RouteConfig;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * Reverse proxy sederhana: terusin request apa adanya (method, header,
 * query string, body) ke backend service sesuai RouteConfig, lalu
 * kembalikan response backend apa adanya (status, header, body).
 *
 * Request yang lolos sampai sini berarti sudah lolos JwtAuthFilter.
 */
@RestController
@RequiredArgsConstructor
public class ProxyController {

    private static final RestClient restClient = RestClient.create();

    private final RouteConfig routeConfig;

    // Header hop-by-hop yang gak boleh ikut diteruskan (baik arah request maupun response).
    private static final String[] HOP_BY_HOP = {
            "host", "content-length", "transfer-encoding", "connection"
    };

    @RequestMapping("/**")
    public ResponseEntity<byte[]> proxy(HttpServletRequest request) throws IOException {

        String path = request.getRequestURI();

        // Jangan proxy endpoint Swagger agar ditangani langsung oleh SpringDoc.
        if (path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/swagger-ui.html")) {
            return ResponseEntity.notFound().build();
        }

        String baseUrl = routeConfig.resolveBaseUrl(path);

        if (baseUrl == null) {
            return ResponseEntity.notFound().build();
        }

        String queryString = request.getQueryString();
        String targetUri = baseUrl + path + (queryString != null ? "?" + queryString : "");

        byte[] requestBody = StreamUtils.copyToByteArray(request.getInputStream());
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        RestClient.RequestBodySpec spec = restClient.method(method)
                .uri(targetUri)
                .headers(headers -> copyRequestHeaders(request, headers));

        if (requestBody.length > 0) {
            spec.body(requestBody);
        }

        return spec.exchange((clientRequest, clientResponse) -> {
            byte[] responseBody = StreamUtils.copyToByteArray(clientResponse.getBody());
            HttpStatusCode status = clientResponse.getStatusCode();
            HttpHeaders responseHeaders = new HttpHeaders();
            clientResponse.getHeaders().forEach((name, values) -> {
                if (!isHopByHop(name)) {
                    responseHeaders.addAll(name, values);
                }
            });

            return ResponseEntity.status(status)
                    .headers(responseHeaders)
                    .body(responseBody);
        });
    }

    private void copyRequestHeaders(HttpServletRequest request, HttpHeaders headers) {
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (isHopByHop(name)) {
                continue;
            }
            Enumeration<String> values = request.getHeaders(name);
            while (values.hasMoreElements()) {
                headers.add(name, values.nextElement());
            }
        }
    }

    private boolean isHopByHop(String headerName) {
        String lower = headerName.toLowerCase(Locale.ROOT);
        for (String h : HOP_BY_HOP) {
            if (h.equals(lower)) {
                return true;
            }
        }
        return false;
    }
}