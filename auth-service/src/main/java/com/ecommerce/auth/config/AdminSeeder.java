package com.ecommerce.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Bootstrap 1 akun ROLE_ADMIN pas Auth Service pertama kali start, karena
 * gak ada lagi jalur publik buat bikin akun ADMIN/EMPLOYEE (register publik
 * dipaksa ROLE_USER). Tanpa ini gak ada cara masuk sebagai admin sama sekali.
 */
@Component
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default-username}")
    private String defaultAdminUsername;

    @Value("${admin.default-password}")
    private String defaultAdminPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByUsername(defaultAdminUsername)) {
            return;
        }

        User admin = new User();
        admin.setUsername(defaultAdminUsername);
        admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
        admin.setRole("ROLE_ADMIN");
        userRepository.save(admin);
    }
}
