package com.example.travelplanner.service;

import com.example.travelplanner.model.User;
import com.example.travelplanner.repository.TripRepository;
import com.example.travelplanner.repository.UserRepository;
import com.example.travelplanner.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private MailService mailService;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("u1", "john", "john@test.com", "encodedPass", "2026-01-01T00:00:00Z", "pl");
    }

    @Test
    void registerSuccess() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtUtil.generateToken("john", "pl", 0)).thenReturn("jwt-token");

        Map<String, String> result = authService.register("john", "john@test.com", "password", "pl");

        assertEquals("jwt-token", result.get("token"));
        assertEquals("john", result.get("username"));
        assertEquals("pl", result.get("language"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerDefaultsToEnOnNullLanguage() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtUtil.generateToken("john", "en", 0)).thenReturn("jwt-token");

        Map<String, String> result = authService.register("john", "john@test.com", "password", null);

        assertEquals("en", result.get("language"));
    }

    @Test
    void registerDefaultsToEnOnInvalidLanguage() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtUtil.generateToken("john", "en", 0)).thenReturn("jwt-token");

        Map<String, String> result = authService.register("john", "john@test.com", "password", "fr");

        assertEquals("en", result.get("language"));
    }

    @Test
    void registerThrowsOnDuplicateUsername() {
        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> authService.register("john", "john@test.com", "password", "en"));
    }

    @Test
    void registerThrowsOnDuplicateEmail() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@test.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> authService.register("john", "john@test.com", "password", "en"));
    }

    @Test
    void loginSuccess() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPass")).thenReturn(true);
        when(jwtUtil.generateToken("john", "pl", 0)).thenReturn("jwt-token");

        Map<String, String> result = authService.login("john", "password");

        assertEquals("jwt-token", result.get("token"));
        assertEquals("john", result.get("username"));
        assertEquals("pl", result.get("language"));
    }

    @Test
    void loginThrowsOnInvalidPassword() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encodedPass")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.login("john", "wrong"));
    }

    @Test
    void updateLanguageSuccess() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtUtil.generateToken("john", "es", 0)).thenReturn("new-token");

        Map<String, String> result = authService.updateLanguage("john", "es");

        assertEquals("new-token", result.get("token"));
        assertEquals("es", result.get("language"));
        assertEquals("es", user.getLanguage());
        verify(userRepository).save(user);
    }

    @Test
    void updateLanguageThrowsOnInvalidLanguage() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.updateLanguage("john", "fr"));
    }

    @Test
    void updateLanguageThrowsOnNullLanguage() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.updateLanguage("john", null));
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

    @Test
    void changePasswordSuccess() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "encodedPass")).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("newEncoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtUtil.generateToken("john", "pl", 1)).thenReturn("new-token");

        Map<String, String> result = authService.changePassword("john", "old", "new");

        assertEquals("new-token", result.get("token"));
        assertEquals("newEncoded", user.getPassword());
        assertEquals(1, user.getPasswordVersion());
        assertNull(user.getResetToken());
        verify(userRepository).save(user);
    }

    @Test
    void changePasswordThrowsOnWrongCurrentPassword() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encodedPass")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> authService.changePassword("john", "wrong", "new"));
    }

    @Test
    void changePasswordThrowsOnBlankNewPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.changePassword("john", "old", " "));
    }

    @Test
    void forgotPasswordUnknownEmailReturnsGenericMessage() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        Map<String, String> result = authService.forgotPassword("unknown@test.com");

        assertNotNull(result.get("message"));
        verify(mailService, never()).sendPasswordReset(anyString(), anyString(), anyString());
    }

    @Test
    void forgotPasswordSetsTokenAndSendsEmail() {
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.forgotPassword("john@test.com");

        assertNotNull(user.getResetToken());
        assertNotNull(user.getResetTokenExpiresAt());
        verify(mailService).sendPasswordReset(eq("john@test.com"), anyString(), eq("pl"));
    }

    @Test
    void resetPasswordSuccess() {
        user.setResetToken("token");
        user.setResetTokenExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS).toString());
        when(userRepository.findByResetToken("token")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new")).thenReturn("newEncoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, String> result = authService.resetPassword("token", "new");

        assertEquals("newEncoded", user.getPassword());
        assertEquals(1, user.getPasswordVersion());
        assertNull(user.getResetToken());
        assertNull(user.getResetTokenExpiresAt());
        verify(userRepository).save(user);
    }

    @Test
    void resetPasswordThrowsOnExpiredToken() {
        user.setResetToken("token");
        user.setResetTokenExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS).toString());
        when(userRepository.findByResetToken("token")).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class,
                () -> authService.resetPassword("token", "new"));
    }

    @Test
    void resetPasswordThrowsOnUnknownToken() {
        when(userRepository.findByResetToken("nope")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> authService.resetPassword("nope", "new"));
    }

    @Test
    void updateProfileUpdatesEmailAndLanguage() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtUtil.generateToken("john", "es", 0)).thenReturn("new-token");

        Map<String, String> result = authService.updateProfile("john", "john2@test.com", "es", null);

        assertEquals("john2@test.com", result.get("email"));
        assertEquals("es", result.get("language"));
        assertEquals("john2@test.com", user.getEmail());
        assertEquals("es", user.getLanguage());
    }

    @Test
    void updateProfileThrowsOnDuplicateEmail() {
        User other = new User("u2", "jane", "jane@test.com", "pass", "2026-01-01T00:00:00Z");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("jane@test.com")).thenReturn(Optional.of(other));

        assertThrows(IllegalArgumentException.class,
                () -> authService.updateProfile("john", "jane@test.com", null, null));
    }

    @Test
    void updateProfileAllowsSameEmail() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtUtil.generateToken("john", "pl", 0)).thenReturn("new-token");

        Map<String, String> result = authService.updateProfile("john", "john@test.com", null, null);

        assertEquals("john@test.com", result.get("email"));
    }

    @Test
    void updateProfileUpdatesCurrency() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtUtil.generateToken("john", "pl", 0)).thenReturn("new-token");

        Map<String, String> result = authService.updateProfile("john", null, null, " eur ");

        assertEquals("EUR", result.get("currency"));
        assertEquals("EUR", user.getCurrency());
    }

    @Test
    void updateProfileRejectsInvalidCurrency() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class,
                () -> authService.updateProfile("john", null, null, "EURO"));
    }

    @Test
    void getProfileReturnsUserData() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        Map<String, String> result = authService.getProfile("john");

        assertEquals("john", result.get("username"));
        assertEquals("john@test.com", result.get("email"));
        assertEquals("pl", result.get("language"));
        assertEquals("PLN", result.get("currency"));
    }

    @Test
    void deleteAccountDeletesTripsAndUser() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(tripRepository.findByUserId("u1")).thenReturn(Collections.emptyList());

        authService.deleteAccount("john");

        verify(tripRepository).deleteAll(Collections.emptyList());
        verify(userRepository).delete(user);
    }
}
