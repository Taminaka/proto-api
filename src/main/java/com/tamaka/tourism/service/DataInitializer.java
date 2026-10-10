package com.tamaka.tourism.service;

import com.tamaka.tourism.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final AuthService authService;

    @Override
    public void run(String... args) {
        String adminEmail = System.getenv().getOrDefault("ADMIN_EMAIL", "admin@tamaka.com");
        String adminPassword = System.getenv().getOrDefault("ADMIN_PASSWORD", "Admin123!");
        String adminFullName = System.getenv().getOrDefault("ADMIN_FULL_NAME", "System Administrator");
        authService.createAdminIfNotExists(adminEmail, adminPassword, adminFullName);
    }
}