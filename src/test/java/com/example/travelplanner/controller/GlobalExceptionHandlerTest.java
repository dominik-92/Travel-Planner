package com.example.travelplanner.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleSecurityExceptionReturns403() {
        ResponseEntity<Map<String, String>> response = handler.handleSecurityException(new SecurityException("Access denied"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied", response.getBody().get("error"));
    }

    @Test
    void handleNotFoundReturns404() {
        ResponseEntity<Map<String, String>> response = handler.handleNotFound(new NoSuchElementException("Trip not found"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Trip not found", response.getBody().get("error"));
    }

    @Test
    void handleBadRequestReturns400() {
        ResponseEntity<Map<String, String>> response = handler.handleBadRequest(new IllegalArgumentException("Invalid input"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid input", response.getBody().get("message"));
        assertNull(response.getBody().get("code"));
    }

    @Test
    void handleBadRequestSplitsCodeAndMessage() {
        ResponseEntity<Map<String, String>> response = handler.handleBadRequest(new IllegalArgumentException("END_DATE_BEFORE_START|End date must not be before start date"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("END_DATE_BEFORE_START", response.getBody().get("code"));
        assertEquals("End date must not be before start date", response.getBody().get("message"));
    }
}
