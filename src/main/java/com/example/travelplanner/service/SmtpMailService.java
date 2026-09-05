package com.example.travelplanner.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;

public class SmtpMailService implements MailService {

    private static final Logger log = LoggerFactory.getLogger(SmtpMailService.class);

    private final JavaMailSender mailSender;
    private final String baseUrl;
    private final String from;

    public SmtpMailService(JavaMailSender mailSender, String baseUrl, String from) {
        this.mailSender = mailSender;
        this.baseUrl = baseUrl;
        this.from = from;
    }

    @Override
    @Async
    public void sendPasswordReset(String email, String token, String language) {
        String link = baseUrl + "/reset-password.html?token=" + token;
        log.info("Password reset requested for {}. Reset link: {}", email, link);

        SimpleMailMessage message = new SimpleMailMessage();
        if (from != null && !from.isBlank()) {
            message.setFrom(from);
        }
        message.setTo(email);
        message.setSubject(subject(language));
        message.setText(body(language, link));

        try {
            mailSender.send(message);
            log.info("Password reset email sent to {}", email);
        } catch (Exception e) {
            log.warn("Failed to send password reset email to {}: {}", email, e.getMessage(), e);
        }
    }

    private String subject(String language) {
        return switch (language) {
            case "pl" -> "Reset hasła - Travel Planner";
            case "es" -> "Restablecer contraseña - Travel Planner";
            default -> "Password reset - Travel Planner";
        };
    }

    private String body(String language, String link) {
        String intro = switch (language) {
            case "pl" -> "Aby zresetować hasło, kliknij poniższy link (ważny 15 minut):";
            case "es" -> "Para restablecer tu contraseña, haz clic en el siguiente enlace (válido 15 minutos):";
            default -> "To reset your password, click the link below (valid for 15 minutes):";
        };
        return intro + "\n\n" + link;
    }
}
