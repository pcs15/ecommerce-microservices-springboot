package com.ecommerce.controller;

import com.ecommerce.dto.OrderRequest;
import com.ecommerce.entity.Order;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    // ADMIN lihat semua riwayat transaksi, ROLE_USER cuma lihat order miliknya
    // sendiri (ownership check, bukan cuma role check).
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping
    public List<Order> getAllOrders(Authentication authentication) {
        if (isAdmin(authentication)) {
            return orderRepository.findAll();
        }
        return orderRepository.findByUserId(authentication.getName());
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            Authentication authentication) {
        try {
            Order order = orderService.createOrder(request, authorizationHeader, authentication.getName());
            return ResponseEntity.ok(order);
        } catch (WebClientResponseException e) {
            // Terusin status asli dari Customer/Product Service (mis. 403
            // ownership gagal, 404 not found) - bukan digeneralisir jadi 400.
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
