package com.example.travelplanner.service;

import com.example.travelplanner.model.User;
import com.example.travelplanner.repository.UserRepository;
import com.example.travelplanner.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
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

        String token = jwtUtil.generateToken(username, lang);
        return Map.of("token", token, "username", username, "language", lang);
    }

    public Map<String, String> login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String language = user.getLanguage() != null ? user.getLanguage() : "en";
        String token = jwtUtil.generateToken(username, language);
        return Map.of("token", token, "username", username, "language", language);
    }

    public Map<String, String> updateLanguage(String username, String language) {
        if (language == null || !language.matches("pl|en|es")) {
            throw new IllegalArgumentException("Unsupported language");
        }

        User user = findByUsername(username);
        user.setLanguage(language);
        userRepository.save(user);

        String token = jwtUtil.generateToken(username, language);
        return Map.of("token", token, "username", username, "language", language);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
