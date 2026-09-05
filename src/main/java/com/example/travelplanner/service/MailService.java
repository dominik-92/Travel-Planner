package com.example.travelplanner.service;

public interface MailService {
    void sendPasswordReset(String email, String token, String language);
}
