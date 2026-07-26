package com.example.travelplanner.service;

import com.example.travelplanner.model.User;
import com.example.travelplanner.repository.UserRepository;
import com.example.travelplanner.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("u1", "john", "john@test.com", "encodedPass", "2026-01-01T00:00:00Z");
    }

    @Test
    void registerSuccess() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtUtil.generateToken("john")).thenReturn("jwt-token");

        Map<String, String> result = authService.register("john", "john@test.com", "password");

        assertEquals("jwt-token", result.get("token"));
        assertEquals("john", result.get("username"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerThrowsOnDuplicateUsername() {
        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> authService.register("john", "john@test.com", "password"));
    }

    @Test
    void registerThrowsOnDuplicateEmail() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@test.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> authService.register("john", "john@test.com", "password"));
    }

    @Test
    void loginSuccess() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPass")).thenReturn(true);
        when(jwtUtil.generateToken("john")).thenReturn("jwt-token");

        Map<String, String> result = authService.login("john", "password");

        assertEquals("jwt-token", result.get("token"));
        assertEquals("john", result.get("username"));
    }

    @Test
    void loginThrowsOnInvalidPassword() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encodedPass")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.login("john", "wrong"));
    }

    @Test
    void loginThrowsOnUnknownUser() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.login("unknown", "password"));
    }

    @Test
    void findByUsernameReturnsUser() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        User result = authService.findByUsername("john");

        assertEquals("john", result.getUsername());
    }

    @Test
    void findByUsernameThrowsWhenNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.findByUsername("unknown"));
    }
}
