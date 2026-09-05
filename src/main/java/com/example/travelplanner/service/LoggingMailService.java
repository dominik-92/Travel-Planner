package com.example.travelplanner.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingMailService implements MailService {

    private static final Logger log = LoggerFactory.getLogger(LoggingMailService.class);

    private final String baseUrl;

    public LoggingMailService(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public void sendPasswordReset(String email, String token, String language) {
        String link = baseUrl + "/reset-password.html?token=" + token;
        log.info("Password reset requested for {}. Reset link: {}", email, link);
    }
}
