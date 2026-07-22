package com.fillinus.erp.module.auth.service;

import com.fillinus.erp.config.JwtUtil;
import com.fillinus.erp.module.auth.dto.*;
import com.fillinus.erp.module.auth.entity.PasswordResetToken;
import com.fillinus.erp.module.auth.entity.User;
import com.fillinus.erp.module.auth.repository.PasswordResetTokenRepository;
import com.fillinus.erp.module.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Authentication Service — handles all AUTH-001 to AUTH-003 logic.
 * AUTH-004 / AUTH-005 are handled by UserService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.password-reset-token-expiry-minutes}")
    private int resetTokenExpiryMinutes;

    // ─── AUTH-001: Login ────────────────────────────────────────────────────
    /**
     * Business flow (per AUTH-001 spec):
     * Input → Validate → Authentication → Load Permission → Load Menu → Dashboard
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Username does not exist."));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new RuntimeException("Account is inactive.");
        }

        // AUTH-001 validation: password must match
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password.");
        }

        user.setLastLoginAt(LocalDateTime.now());
        user.setRememberMe(request.isRememberMe());
        userRepository.save(user);

        String role = user.getRole() != null ? user.getRole().getName() : "EMPLOYEE";
        String token = jwtUtil.generateToken(user.getUsername(), role);

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationMs() / 1000)
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(role)
                .build();
    }

    // ─── AUTH-002: Forgot Password ──────────────────────────────────────────
    /**
     * Business flow (per AUTH-002 spec):
     * Enter email → validate email exists → generate token → send reset email
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email not found."));

        // Invalidate all old tokens for this user
        tokenRepository.invalidateAllByUserId(user.getId());

        // Generate new secure token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusMinutes(resetTokenExpiryMinutes))
                .build();
        tokenRepository.save(resetToken);

        // Send email with reset link
        sendResetEmail(user.getEmail(), user.getFullName(), token);
        log.info("Password reset email sent to: {}", user.getEmail());
    }

    // ─── AUTH-003: Reset Password ───────────────────────────────────────────
    /**
     * Business flow (per AUTH-003 spec):
     * Enter token + new password → validate token → validate policy → update password
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Confirm Password does not match.");
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid or expired token."));

        if (!resetToken.isValid()) {
            throw new RuntimeException("Token has expired or already been used.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setIsUsed(true);
        tokenRepository.save(resetToken);

        log.info("Password reset successful for user: {}", user.getUsername());
    }

    // ─── Helper ─────────────────────────────────────────────────────────────
    private void sendResetEmail(String to, String name, String token) {
        String resetLink = baseUrl + "/reset-password?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("FILLINUS ERP - Password Reset Request");
        message.setText(String.format(
                "Hello %s,\n\n" +
                "You requested to reset your password.\n" +
                "Click the link below to reset it (valid for %d minutes):\n\n" +
                "%s\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "FILLINUS ERP Team",
                name, resetTokenExpiryMinutes, resetLink
        ));
        mailSender.send(message);
    }
}
