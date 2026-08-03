package com.example.travelplanner.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String SECRET = "test-secret-key-for-testing-purposes-only-minimum-32-bytes-long";
    private static final long EXPIRATION = 3600000L;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION);
    }

    @Test
    void generateTokenReturnsNonNullString() {
        String token = jwtUtil.generateToken("john", "pl");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void generateTokenWithoutLanguageDefaultsToEn() {
        String token = jwtUtil.generateToken("john");
        assertEquals("en", jwtUtil.extractLanguage(token));
    }

    @Test
    void generateTokenStoresLanguageClaim() {
        String token = jwtUtil.generateToken("john", "es");

        assertEquals("john", jwtUtil.extractUsername(token));
        assertEquals("es", jwtUtil.extractLanguage(token));
    }

    @Test
    void extractUsernameReturnsCorrectSubject() {
        String token = jwtUtil.generateToken("john");

        String username = jwtUtil.extractUsername(token);

        assertEquals("john", username);
    }

    @Test
    void validateTokenReturnsTrueForValidToken() {
        String token = jwtUtil.generateToken("john");

        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void validateTokenReturnsFalseForMalformedToken() {
        assertFalse(jwtUtil.validateToken("not.a.valid.token"));
    }

    @Test
    void validateTokenReturnsFalseForTokenWithWrongSecret() {
        SecretKey otherKey = Keys.hmacShaKeyFor("another-secret-key-for-testing-purposes-only-minimum-32-byt".getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("john")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(otherKey)
                .compact();

        assertFalse(jwtUtil.validateToken(token));
    }

    @Test
    void validateTokenReturnsFalseForExpiredToken() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L);
        String token = jwtUtil.generateToken("john");

        assertFalse(jwtUtil.validateToken(token));
    }
}
