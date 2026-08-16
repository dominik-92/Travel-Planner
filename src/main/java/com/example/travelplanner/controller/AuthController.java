package com.example.travelplanner.controller;

import com.example.travelplanner.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String email = payload.get("email");
        String password = payload.get("password");
        String language = payload.get("language");

        log.info("Register attempt for username: {}", username);

        if (username == null || email == null || password == null) {
            log.warn("Register failed: missing fields for username: {}", username);
            return ResponseEntity.badRequest().body(Map.of("error", "Missing fields"));
        }

        try {
            Map<String, String> result = authService.register(username, email, password, language);
            log.info("Register successful for username: {}", username);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("Register failed for username: {}: {}", username, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Register error for username: {}: {}", username, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/auth/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");

        log.info("Login attempt for username: {}", username);

        if (username == null || password == null) {
            log.warn("Login failed: missing fields for username: {}", username);
            return ResponseEntity.badRequest().body(Map.of("error", "Missing fields"));
        }

        try {
            Map<String, String> result = authService.login(username, password);
            log.info("Login successful for username: {}", username);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("Login failed for username: {}: {}", username, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Login error for username: {}: {}", username, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }

    @PutMapping("/user/language")
    public ResponseEntity<Map<String, String>> updateLanguage(@RequestBody Map<String, String> payload, Authentication authentication) {
        String language = payload.get("language");

        if (language == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing language field"));
        }

        try {
            Map<String, String> result = authService.updateLanguage(authentication.getName(), language);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
