package com.ecommerce.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecommerce.auth.dto.AuthResponse;
import com.ecommerce.auth.dto.ChangePasswordRequest;
import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.auth.dto.RegisterRequest;
import com.ecommerce.auth.dto.ResetPasswordRequest;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // Jalur publik (POST /api/auth/register) - role dipaksa ROLE_USER, field
    // role dari request diabaikan. Ini cegah siapapun self-promote jadi
    // EMPLOYEE/ADMIN cuma dengan register biasa.
    public User register(RegisterRequest request) {
        return createUser(request, "ROLE_USER");
    }

    // Jalur ADMIN-only (POST /api/auth/employees) - role dipaksa ROLE_EMPLOYEE.
    public User createEmployee(RegisterRequest request) {
        return createUser(request, "ROLE_EMPLOYEE");
    }

    private User createUser(RegisterRequest request, String forcedRole) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username sudah dipakai!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(forcedRole);

        return userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Username atau password salah!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Username atau password salah!");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getRole());
    }

    // Ganti password sendiri - siapapun (USER/EMPLOYEE/ADMIN), wajib verify
    // oldPassword dulu.
    public void changeOwnPassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan!"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Password lama salah!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // Reset password user manapun - ADMIN-only (dicek lewat @PreAuthorize di
    // controller), gak perlu tau password lama.
    public void resetPassword(Long targetUserId, ResetPasswordRequest request) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan!"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
