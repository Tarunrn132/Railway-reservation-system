package com.railway.reservation.service;

import com.railway.reservation.dto.ProviderStatusResponse;
import com.railway.reservation.entity.SmsLog;
import com.railway.reservation.repository.SmsLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    private final SmsLogRepository smsLogRepository;
    private final RestClient restClient;

    @Value("${sms.provider:mock}")
    private String smsProvider;

    @Value("${sms.api.key:}")
    private String smsApiKey;

    @Value("${sms.api.secret:}")
    private String smsApiSecret;

    @Value("${sms.sender.id:RAILRES}")
    private String smsSenderId;

    public SmsService(SmsLogRepository smsLogRepository) {
        this.smsLogRepository = smsLogRepository;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    public ProviderStatusResponse.ComponentStatus getProviderStatus() {
        if ("mock".equalsIgnoreCase(smsProvider) || smsApiKey == null || smsApiKey.trim().isEmpty()) {
            return new ProviderStatusResponse.ComponentStatus(smsProvider, "NOT_CONFIGURED", "SMS credentials not configured. Running in sandbox logging mode.");
        }
        return new ProviderStatusResponse.ComponentStatus(smsProvider, "CONNECTED", "SMS provider configured and ready.");
    }

    public boolean sendSms(String recipientPhone, String message) {
        if (recipientPhone == null || recipientPhone.trim().isEmpty()) {
            return false;
        }

        String normalizedPhone = recipientPhone.trim();
        String status = "QUEUED";

        try {
            if ("twilio".equalsIgnoreCase(smsProvider) && isConfigured()) {
                status = dispatchTwilioSms(normalizedPhone, message);
            } else if ("msg91".equalsIgnoreCase(smsProvider) && isConfigured()) {
                status = dispatchMsg91Sms(normalizedPhone, message);
            } else {
                // Mock / Sandbox logging mode
                log.info("Recording transactional SMS via mock provider to [MASKED-PHONE]");
                status = "SENT";
            }
        } catch (Exception e) {
            log.error("Failed to dispatch SMS: {}", e.getMessage());
            status = "FAILED";
        }

        // Persist transaction log
        SmsLog smsLog = new SmsLog(normalizedPhone, message, smsProvider, status);
        smsLogRepository.save(smsLog);
        return "SENT".equals(status);
    }

    private boolean isConfigured() {
        return smsApiKey != null && !smsApiKey.trim().isEmpty();
    }

    private String dispatchTwilioSms(String toPhone, String body) {
        String accountSid = smsApiKey.trim();
        String authToken = smsApiSecret != null ? smsApiSecret.trim() : "";
        String twilioUrl = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";

        String authHeader = "Basic " + Base64.getEncoder().encodeToString((accountSid + ":" + authToken).getBytes(StandardCharsets.UTF_8));

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("To", toPhone.startsWith("+") ? toPhone : "+91" + toPhone);
        formData.add("From", smsSenderId);
        formData.add("Body", body);

        try {
            log.info("Dispatching SMS via Twilio to [MASKED-PHONE]");
            restClient.post()
                    .uri(twilioUrl)
                    .header("Authorization", authHeader)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, resp) -> {
                        throw new RuntimeException("Twilio returned HTTP " + resp.getStatusCode());
                    })
                    .toBodilessEntity();
            return "SENT";
        } catch (Exception e) {
            log.error("Twilio SMS dispatch failed: {}", e.getMessage());
            return "FAILED";
        }
    }

    private String dispatchMsg91Sms(String toPhone, String body) {
        String msg91Url = "https://api.msg91.com/api/v5/flow/";
        try {
            log.info("Dispatching SMS via MSG91 to [MASKED-PHONE]");
            restClient.post()
                    .uri(msg91Url)
                    .header("authkey", smsApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"mobiles\":\"" + toPhone + "\",\"sender\":\"" + smsSenderId + "\",\"message\":\"" + body.replace("\"", "\\\"") + "\"}")
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, resp) -> {
                        throw new RuntimeException("MSG91 returned HTTP " + resp.getStatusCode());
                    })
                    .toBodilessEntity();
            return "SENT";
        } catch (Exception e) {
            log.error("MSG91 SMS dispatch failed: {}", e.getMessage());
            return "FAILED";
        }
    }
}
