package com.railway.reservation.service;

import com.railway.reservation.entity.OtpVerification;
import com.railway.reservation.exception.BadRequestException;
import com.railway.reservation.repository.OtpVerificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    private final OtpVerificationRepository otpRepository;
    private final SmsService smsService;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final int OTP_VALIDITY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;

    public OtpService(OtpVerificationRepository otpRepository, SmsService smsService, PasswordEncoder passwordEncoder) {
        this.otpRepository = otpRepository;
        this.smsService = smsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void generateAndSendOtp(String phone) {
        String normalizedPhone = normalizePhone(phone);

        // 1. Rate limiting & Resend Cooldown check
        otpRepository.findTopByPhoneAndVerifiedFalseOrderByCreatedAtDesc(normalizedPhone).ifPresent(latest -> {
            LocalDateTime cooldownEnd = latest.getCreatedAt().plusSeconds(RESEND_COOLDOWN_SECONDS);
            if (LocalDateTime.now().isBefore(cooldownEnd)) {
                throw new BadRequestException("Please wait 60 seconds before requesting another OTP.");
            }
        });

        // 2. Hourly rate limit check (max 10 requests per hour)
        List<OtpVerification> recentOtps = otpRepository.findByPhoneAndCreatedAtAfter(
                normalizedPhone, LocalDateTime.now().minusHours(1));
        if (recentOtps.size() >= 10) {
            throw new BadRequestException("OTP request limit exceeded for this number. Please try again later.");
        }

        // 3. Cryptographically secure 6-digit OTP generation
        int codeInt = 100000 + secureRandom.nextInt(900000);
        String plainOtp = String.valueOf(codeInt);
        String otpHash = passwordEncoder.encode(plainOtp);

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES);
        OtpVerification verification = new OtpVerification(normalizedPhone, otpHash, expiresAt);
        otpRepository.save(verification);

        // 4. Send transactional SMS via backend provider
        String smsText = "Your RailReserve login verification OTP is " + plainOtp + ". Valid for 5 minutes. Do not share with anyone.";
        smsService.sendSms(normalizedPhone, smsText);
        log.info("OTP generated and dispatched to phone ending in {}", normalizedPhone.substring(Math.max(0, normalizedPhone.length() - 4)));
    }

    @Transactional
    public boolean verifyOtp(String phone, String inputOtp) {
        String normalizedPhone = normalizePhone(phone);

        OtpVerification verification = otpRepository.findTopByPhoneAndVerifiedFalseOrderByCreatedAtDesc(normalizedPhone)
                .orElseThrow(() -> new BadRequestException("No active OTP request found for this phone number. Please request a new OTP."));

        if (verification.isExpired()) {
            throw new BadRequestException("OTP has expired. Please request a new OTP.");
        }

        if (verification.getAttempts() >= MAX_ATTEMPTS) {
            throw new BadRequestException("Maximum OTP verification attempts exceeded. Please request a new OTP.");
        }

        verification.setAttempts(verification.getAttempts() + 1);

        if (!passwordEncoder.matches(inputOtp, verification.getOtpHash())) {
            otpRepository.save(verification);
            int remaining = MAX_ATTEMPTS - verification.getAttempts();
            throw new BadRequestException("Invalid OTP. " + remaining + " attempts remaining.");
        }

        // OTP is valid - mark as verified and invalidate
        verification.setVerified(true);
        otpRepository.save(verification);
        return true;
    }

    private String normalizePhone(String phone) {
        if (phone == null) return "";
        String clean = phone.replaceAll("[^0-9]", "");
        if (clean.length() == 12 && clean.startsWith("91")) {
            clean = clean.substring(2);
        }
        return clean;
    }
}
