package com.railway.reservation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.railway.reservation.dto.*;
import com.railway.reservation.entity.User;
import com.railway.reservation.exception.BadRequestException;
import com.railway.reservation.exception.ResourceNotFoundException;
import com.railway.reservation.exception.UnauthorizedException;
import com.railway.reservation.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final EmailService emailService;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${google.client.id:}")
    private String googleClientId;

    @Value("${google.client.secret:}")
    private String googleClientSecret;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       OtpService otpService,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.emailService = emailService;
        this.objectMapper = new ObjectMapper();

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    public boolean isGoogleAuthConfigured() {
        return googleClientId != null && !googleClientId.trim().isEmpty();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BadRequestException("An account with this email already exists");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(
                request.getName().trim(),
                request.getEmail().trim().toLowerCase(),
                request.getPhone().trim(),
                hashedPassword,
                "USER"
        );

        User savedUser = userRepository.save(user);
        String token = "AUTH_" + UUID.randomUUID().toString().replace("-", "");

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getPhone(),
                savedUser.getRole(),
                token
        );
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = "AUTH_" + UUID.randomUUID().toString().replace("-", "");

        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                token
        );
    }

    @Transactional
    public void sendPhoneOtp(PhoneOtpRequest request) {
        otpService.generateAndSendOtp(request.getPhone());
    }

    @Transactional
    public AuthResponse verifyPhoneOtpAndLogin(PhoneOtpVerifyRequest request) {
        boolean verified = otpService.verifyOtp(request.getPhone(), request.getOtp());
        if (!verified) {
            throw new UnauthorizedException("OTP verification failed.");
        }

        String normalizedPhone = request.getPhone().replaceAll("[^0-9]", "");
        if (normalizedPhone.length() == 12 && normalizedPhone.startsWith("91")) {
            normalizedPhone = normalizedPhone.substring(2);
        }

        final String phoneToSearch = normalizedPhone;
        User user = userRepository.findAll().stream()
                .filter(u -> u.getPhone() != null && u.getPhone().contains(phoneToSearch))
                .findFirst()
                .orElseGet(() -> {
                    // Auto-register new phone user
                    String userName = request.getName() != null && !request.getName().trim().isEmpty() ?
                            request.getName().trim() : "Passenger " + phoneToSearch.substring(Math.max(0, phoneToSearch.length() - 4));
                    String placeholderEmail = "user_" + phoneToSearch + "@railreserve.internal";
                    String randomPass = passwordEncoder.encode(UUID.randomUUID().toString());

                    User newUser = new User(userName, placeholderEmail, phoneToSearch, randomPass, "USER");
                    return userRepository.save(newUser);
                });

        String token = "AUTH_PHONE_" + UUID.randomUUID().toString().replace("-", "");
        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                token
        );
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("No account found registered with email: " + request.getEmail()));

        String resetCode = String.format("%06d", (int) (Math.random() * 900000) + 100000);
        String subject = "RailReserve - Password Reset Verification Code";
        String body = "Dear " + user.getName() + ",\n\nYour password reset verification code is: " + resetCode +
                "\n\nPlease use this code within 15 minutes to reset your password.\n\nWarm regards,\nRailReserve Team";

        emailService.sendEmail(user.getEmail(), subject, body);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("No account found registered with email: " + request.getEmail()));

        if (request.getResetToken() == null || request.getResetToken().trim().isEmpty()) {
            throw new BadRequestException("Invalid or missing reset token.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public AuthResponse googleLogin(GoogleAuthRequest request) {
        if (!isGoogleAuthConfigured()) {
            throw new BadRequestException("Google Login is not configured.");
        }

        if (request.getIdToken() == null || request.getIdToken().trim().isEmpty()) {
            throw new UnauthorizedException("Google Auth verification failed: ID token is missing.");
        }

        // Server-side validation via Google Tokeninfo endpoint
        String tokeninfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + request.getIdToken().trim();
        String verifiedEmail;
        String verifiedName;

        try {
            String tokeninfoResponse = restClient.get()
                    .uri(tokeninfoUrl)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, resp) -> {
                        throw new UnauthorizedException("Google Auth verification failed: Invalid Google ID token signature or expired.");
                    })
                    .body(String.class);

            JsonNode root = objectMapper.readTree(tokeninfoResponse);
            String audience = root.path("aud").asText();
            if (!googleClientId.trim().equals(audience)) {
                throw new UnauthorizedException("Google Auth verification failed: Token audience mismatch.");
            }

            boolean emailVerified = root.path("email_verified").asBoolean(false) || "true".equalsIgnoreCase(root.path("email_verified").asText());
            if (!emailVerified) {
                throw new UnauthorizedException("Google Auth verification failed: Google account email is not verified.");
            }

            verifiedEmail = root.path("email").asText().toLowerCase();
            verifiedName = root.path("name").asText("Google Passenger");
        } catch (UnauthorizedException ue) {
            throw ue;
        } catch (Exception e) {
            log.error("Google tokeninfo validation failed: {}", e.getMessage());
            throw new UnauthorizedException("Google Auth verification failed: Unable to verify ID token with Google.");
        }

        User user = userRepository.findByEmail(verifiedEmail).orElseGet(() -> {
            String randomPass = passwordEncoder.encode(UUID.randomUUID().toString());
            User newUser = new User(verifiedName, verifiedEmail, "9800000000", randomPass, "USER");
            return userRepository.save(newUser);
        });

        String token = "AUTH_GOOGLE_" + UUID.randomUUID().toString().replace("-", "");
        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                token
        );
    }

    public User getUserById(Long userId) {
        if (userId == null) return null;
        return userRepository.findById(userId).orElse(null);
    }
}
