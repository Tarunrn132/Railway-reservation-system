package com.railway.reservation.service;

import com.railway.reservation.dto.ProviderStatusResponse;
import com.railway.reservation.entity.EmailLog;
import com.railway.reservation.repository.EmailLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final EmailLogRepository emailLogRepository;
    private final RestClient restClient;

    @Value("${email.provider:mock}")
    private String emailProvider;

    @Value("${email.api.key:}")
    private String emailApiKey;

    @Value("${spring.mail.username:no-reply@railreserve.example.com}")
    private String fromEmail;

    public EmailService(EmailLogRepository emailLogRepository) {
        this.emailLogRepository = emailLogRepository;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    public ProviderStatusResponse.ComponentStatus getProviderStatus() {
        if ("mock".equalsIgnoreCase(emailProvider) || emailApiKey == null || emailApiKey.trim().isEmpty()) {
            return new ProviderStatusResponse.ComponentStatus(emailProvider, "NOT_CONFIGURED", "Email credentials not configured. Running in sandbox logging mode.");
        }
        return new ProviderStatusResponse.ComponentStatus(emailProvider, "CONNECTED", "Email provider configured and ready.");
    }

    public boolean sendEmail(String recipientEmail, String subject, String body) {
        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            return false;
        }

        String normalizedEmail = recipientEmail.trim();
        String status = "QUEUED";

        try {
            if ("sendgrid".equalsIgnoreCase(emailProvider) && isConfigured()) {
                status = dispatchSendGridEmail(normalizedEmail, subject, body);
            } else {
                // Mock / Sandbox logging mode
                log.info("Recording transactional email to [MASKED-EMAIL] with subject '{}' via {}", subject, emailProvider);
                status = "SENT";
            }
        } catch (Exception e) {
            log.error("Failed to dispatch email: {}", e.getMessage());
            status = "FAILED";
        }

        EmailLog emailLog = new EmailLog(normalizedEmail, subject, body, emailProvider, status);
        emailLogRepository.save(emailLog);
        return "SENT".equals(status);
    }

    private boolean isConfigured() {
        return emailApiKey != null && !emailApiKey.trim().isEmpty();
    }

    private String dispatchSendGridEmail(String toEmail, String subject, String body) {
        String sendGridUrl = "https://api.sendgrid.com/v3/mail/send";
        String payload = "{"
                + "\"personalizations\":[{\"to\":[{\"email\":\"" + toEmail + "\"}]}],"
                + "\"from\":{\"email\":\"" + fromEmail + "\",\"name\":\"RailReserve\"},"
                + "\"subject\":\"" + subject.replace("\"", "\\\"") + "\","
                + "\"content\":[{\"type\":\"text/plain\",\"value\":\"" + body.replace("\"", "\\\"").replace("\n", "\\n") + "\"}]"
                + "}";

        try {
            log.info("Dispatching email via SendGrid to [MASKED-EMAIL]");
            restClient.post()
                    .uri(sendGridUrl)
                    .header("Authorization", "Bearer " + emailApiKey.trim())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, resp) -> {
                        throw new RuntimeException("SendGrid returned HTTP " + resp.getStatusCode());
                    })
                    .toBodilessEntity();
            return "SENT";
        } catch (Exception e) {
            log.error("SendGrid email dispatch failed: {}", e.getMessage());
            return "FAILED";
        }
    }
}
