package com.example.travelplanner.controller;

import com.example.travelplanner.service.AuthService;
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

        if (username == null || email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing fields"));
        }

        try {
            Map<String, String> result = authService.register(username, email, password, language);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/auth/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing fields"));
        }

        try {
            Map<String, String> result = authService.login(username, password);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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
