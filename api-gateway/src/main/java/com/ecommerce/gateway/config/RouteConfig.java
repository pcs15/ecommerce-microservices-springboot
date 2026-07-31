package com.ecommerce.gateway.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Pemetaan prefix path publik -> base URL service tujuan.
 * Urutan penting: prefix lebih spesifik ditaruh sebelum yang lebih umum
 * kalau ada tumpang tindih (di sini tidak ada, tiap prefix unik).
 */
@Configuration
public class RouteConfig {

    @Value("${routes.auth}")
    private String authUrl;
    @Value("${routes.customers}")
    private String customersUrl;
    @Value("${routes.products}")
    private String productsUrl;
    @Value("${routes.orders}")
    private String ordersUrl;
    @Value("${routes.karyawan}")
    private String karyawanUrl;
    @Value("${routes.absensi}")
    private String absensiUrl;
    @Value("${routes.izin-cuti}")
    private String izinCutiUrl;

    private Map<String, String> routes;

    private Map<String, String> routes() {
        if (routes == null) {
            routes = new LinkedHashMap<>();
            routes.put("/api/auth", authUrl);
            routes.put("/api/customers", customersUrl);
            routes.put("/api/products", productsUrl);
            routes.put("/api/orders", ordersUrl);
            routes.put("/api/karyawan", karyawanUrl);
            routes.put("/api/absensi", absensiUrl);
            routes.put("/api/izin-cuti", izinCutiUrl);
        }
        return routes;
    }

    /**
     * true kalau path ini publik (gak perlu JWT). Cuma register & login -
     * BUKAN seluruh /api/auth/**, soalnya /api/auth/employees itu ADMIN-only.
     */
    public boolean isPublic(String path) {
        return path.equals("/api/auth/register") || path.equals("/api/auth/login");
    }

    /** true kalau path ini domain Employee (Karyawan/Absensi/IzinCuti). */
    public boolean isEmployeeDomain(String path) {
        return path.startsWith("/api/karyawan") || path.startsWith("/api/absensi") || path.startsWith("/api/izin-cuti");
    }

    /** true kalau path ini domain Customer/Order (belanja). */
    public boolean isCustomerDomain(String path) {
        return path.startsWith("/api/customers") || path.startsWith("/api/orders");
    }

    /** Cari base URL backend berdasarkan prefix path request. Null kalau gak ketemu (404). */
    public String resolveBaseUrl(String path) {
        for (Map.Entry<String, String> entry : routes().entrySet()) {
            if (path.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
}
