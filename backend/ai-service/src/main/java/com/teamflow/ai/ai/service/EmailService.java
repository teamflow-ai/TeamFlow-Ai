package com.teamflow.ai.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends transactional email through Spring Mail.
 *
 * <p>Every send is best-effort: a notification that fails to email is still
 * delivered in-app (see {@link NotificationService}), so an SMTP outage must never
 * surface as a failed API request. Callers do not need to catch anything here.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${teamflow.mail.from}")
    private String fromAddress;

    public void send(String toAddress, String subject, String body) {
        if (toAddress == null || toAddress.isBlank()) {
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toAddress);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.debug("Sent email '{}' to {}", subject, toAddress);
        } catch (MailException ex) {
            log.warn("Failed to send email '{}' to {}: {}", subject, toAddress, ex.getMessage());
        }
    }
}
