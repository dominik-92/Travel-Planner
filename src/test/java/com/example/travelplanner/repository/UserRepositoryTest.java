package com.example.travelplanner.repository;

import com.example.travelplanner.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @MockitoBean
    private RestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User user = new User("u1", "john", "john@test.com", "encodedPass", "2026-01-01T00:00:00Z");
        userRepository.save(user);
    }

    @Test
    void findByUsernameReturnsUser() {
        Optional<User> result = userRepository.findByUsername("john");

        assertTrue(result.isPresent());
        assertEquals("john", result.get().getUsername());
        assertEquals("john@test.com", result.get().getEmail());
    }

    @Test
    void findByUsernameReturnsEmptyForUnknown() {
        Optional<User> result = userRepository.findByUsername("unknown");

        assertTrue(result.isEmpty());
    }

    @Test
    void existsByUsernameReturnsTrue() {
        assertTrue(userRepository.existsByUsername("john"));
    }

    @Test
    void existsByUsernameReturnsFalse() {
        assertFalse(userRepository.existsByUsername("unknown"));
    }

    @Test
    void existsByEmailReturnsTrue() {
        assertTrue(userRepository.existsByEmail("john@test.com"));
    }

    @Test
    void existsByEmailReturnsFalse() {
        assertFalse(userRepository.existsByEmail("unknown@test.com"));
    }
}
