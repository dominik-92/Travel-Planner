package com.example.travelplanner.service;

import com.example.travelplanner.model.Trip;
import com.example.travelplanner.model.User;
import com.example.travelplanner.repository.TripRepository;
import com.example.travelplanner.repository.UserRepository;
import com.example.travelplanner.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final MailService mailService;

    @Value("${app.reset-password.token-ttl-minutes:15}")
    private long resetTokenTtlMinutes;

    public AuthService(UserRepository userRepository, TripRepository tripRepository,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil, MailService mailService) {
        this.userRepository = userRepository;
        this.tripRepository = tripRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.mailService = mailService;
    }

    public Map<String, String> register(String username, String email, String password, String language) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        String lang = (language != null && language.matches("pl|en|es")) ? language : "en";

        User user = new User(
                System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6),
                username,
                email,
                passwordEncoder.encode(password),
                Instant.now().toString(),
                lang
        );
        userRepository.save(user);

        String token = jwtUtil.generateToken(username, lang, user.getPasswordVersion());
        return Map.of("token", token, "username", username, "language", lang);
    }

    public Map<String, String> login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String language = user.getLanguage() != null ? user.getLanguage() : "en";
        String token = jwtUtil.generateToken(username, language, user.getPasswordVersion());
        return Map.of("token", token, "username", username, "language", language);
    }

    public Map<String, String> updateLanguage(String username, String language) {
        if (language == null || !language.matches("pl|en|es")) {
            throw new IllegalArgumentException("Unsupported language");
        }

        User user = findByUsername(username);
        user.setLanguage(language);
        userRepository.save(user);

        String token = jwtUtil.generateToken(username, language, user.getPasswordVersion());
        return Map.of("token", token, "username", username, "language", language);
    }

    public Map<String, String> changePassword(String username, String currentPassword, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password is required");
        }

        User user = findByUsername(username);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordVersion(user.getPasswordVersion() + 1);
        user.setResetToken(null);
        user.setResetTokenExpiresAt(null);
        userRepository.save(user);

        String token = jwtUtil.generateToken(username, user.getLanguage(), user.getPasswordVersion());
        return Map.of("token", token, "message", "Password changed");
    }

    public Map<String, String> forgotPassword(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return Map.of("message", "If the email exists, a reset link has been sent");
        }

        String token = generateResetToken();
        user.setResetToken(token);
        user.setResetTokenExpiresAt(Instant.now().plus(resetTokenTtlMinutes, ChronoUnit.MINUTES).toString());
        userRepository.save(user);

        mailService.sendPasswordReset(user.getEmail(), token, user.getLanguage());
        return Map.of("message", "If the email exists, a reset link has been sent");
    }

    public Map<String, String> resetPassword(String token, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password is required");
        }

        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        if (user.getResetTokenExpiresAt() == null
                || Instant.parse(user.getResetTokenExpiresAt()).isBefore(Instant.now())) {
            throw new IllegalArgumentException("Invalid or expired reset token");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordVersion(user.getPasswordVersion() + 1);
        user.setResetToken(null);
        user.setResetTokenExpiresAt(null);
        userRepository.save(user);

        return Map.of("message", "Password has been reset");
    }

    public Map<String, String> updateProfile(String username, String email, String language, String currency) {
        User user = findByUsername(username);

        if (email != null && !email.isBlank()) {
            userRepository.findByEmail(email).ifPresent(existing -> {
                if (!existing.getId().equals(user.getId())) {
                    throw new IllegalArgumentException("Email already exists");
                }
            });
            user.setEmail(email);
        }

        if (language != null) {
            if (!language.matches("pl|en|es")) {
                throw new IllegalArgumentException("Unsupported language");
            }
            user.setLanguage(language);
        }

        if (currency != null && !currency.isBlank()) {
            String normalized = currency.trim().toUpperCase();
            if (!normalized.matches("[A-Z]{3}")) {
                throw new IllegalArgumentException("Unsupported currency");
            }
            user.setCurrency(normalized);
        }

        userRepository.save(user);

        String effectiveLanguage = user.getLanguage() != null ? user.getLanguage() : "en";
        String effectiveEmail = user.getEmail() != null ? user.getEmail() : "";
        String effectiveCurrency = user.getCurrency() != null ? user.getCurrency() : "PLN";
        String token = jwtUtil.generateToken(username, effectiveLanguage, user.getPasswordVersion());
        return Map.of("token", token, "username", username, "email", effectiveEmail,
                "language", effectiveLanguage, "currency", effectiveCurrency);
    }

    public Map<String, String> getProfile(String username) {
        User user = findByUsername(username);
        String email = user.getEmail() != null ? user.getEmail() : "";
        String language = user.getLanguage() != null ? user.getLanguage() : "en";
        String currency = user.getCurrency() != null ? user.getCurrency() : "PLN";
        return Map.of("username", user.getUsername(), "email", email, "language", language,
                "currency", currency);
    }

    public void deleteAccount(String username) {
        User user = findByUsername(username);
        List<Trip> trips = tripRepository.findByUserId(user.getId());
        tripRepository.deleteAll(trips);
        userRepository.delete(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private String generateResetToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
