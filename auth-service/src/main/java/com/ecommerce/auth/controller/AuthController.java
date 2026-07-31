package com.ecommerce.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.auth.dto.AuthResponse;
import com.ecommerce.auth.dto.ChangePasswordRequest;
import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.auth.dto.RegisterRequest;
import com.ecommerce.auth.dto.RegisterResponse;
import com.ecommerce.auth.dto.ResetPasswordRequest;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Publik - siapapun bisa register, tapi selalu jadi ROLE_USER.
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request);
            return ResponseEntity.ok(new RegisterResponse(user.getId(), user.getUsername(), user.getRole()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    // ADMIN-only - "add employee". EMPLOYEE gak lolos @PreAuthorize ini,
    // jadi gak bisa add employee lain.
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/employees")
    public ResponseEntity<?> createEmployee(@RequestBody RegisterRequest request) {
        try {
            User user = authService.createEmployee(request);
            return ResponseEntity.ok(new RegisterResponse(user.getId(), user.getUsername(), user.getRole()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Ganti password sendiri - role apapun, harus login (userId dari token).
    @PutMapping("/password")
    public ResponseEntity<?> changeOwnPassword(@RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        try {
            authService.changeOwnPassword(Long.valueOf(authentication.getName()), request);
            return ResponseEntity.ok("Password berhasil diganti.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Reset password user manapun - ADMIN-only, gak perlu tau password lama.
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{id}/password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id, @RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(id, request);
            return ResponseEntity.ok("Password user id " + id + " berhasil direset.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
