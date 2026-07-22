package com.fillinus.erp.module.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Password Reset Token — maps to `password_reset_tokens` (V5__create_password_reset_tokens.sql).
 * Used by AUTH-002 (Forgot Password) and AUTH-003 (Reset Password).
 */
@Entity
@Table(name = "password_reset_tokens")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Secure random UUID token sent via email */
    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    /** Token expires after configured minutes (default: 30 min) */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** Once used for password reset, marked true to prevent reuse */
    @Column(name = "is_used", nullable = false)
    @Builder.Default
    private Boolean isUsed = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !isUsed && !isExpired();
    }
}
