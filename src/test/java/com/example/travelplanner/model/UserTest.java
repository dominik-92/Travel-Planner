package com.example.travelplanner.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void parameterizedConstructorSetsAllFields() {
        User user = new User("u1", "john", "john@test.com", "hashedPass", "2026-01-01T00:00:00Z");

        assertEquals("u1", user.getId());
        assertEquals("john", user.getUsername());
        assertEquals("john@test.com", user.getEmail());
        assertEquals("hashedPass", user.getPassword());
        assertEquals("2026-01-01T00:00:00Z", user.getCreatedAt());
    }

    @Test
    void gettersAndSetters() {
        User user = new User();
        user.setId("u2");
        user.setUsername("jane");
        user.setEmail("jane@test.com");
        user.setPassword("secret");
        user.setCreatedAt("2026-06-01T00:00:00Z");

        assertEquals("u2", user.getId());
        assertEquals("jane", user.getUsername());
        assertEquals("jane@test.com", user.getEmail());
        assertEquals("secret", user.getPassword());
        assertEquals("2026-06-01T00:00:00Z", user.getCreatedAt());
    }
}
