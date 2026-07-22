package com.fillinus.erp.module.auth.entity;

import com.fillinus.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * User entity — maps to the `users` table (V4__create_users.sql).
 *
 * Fields sourced from:
 *   - AUTH-001 Login:          username, password, remember_me
 *   - AUTH-004 My Profile:     full_name, email, phone, avatar_url,
 *                               date_of_birth, gender, role, department, position
 *   - AUTH-005 Change Password: password (bcrypt hashed)
 */
@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── AUTH-001 Login ───────────────────────────────────────
    /** Max 100 chars per AUTH-001 spec (TXT001) */
    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    /** BCrypt hashed; max 255 chars per AUTH-005 spec (CP001-CP003) */
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /** Remember Me checkbox — AUTH-001 CHK001 */
    @Column(name = "remember_me", nullable = false)
    @Builder.Default
    private Boolean rememberMe = false;

    // ── AUTH-004 My Profile ──────────────────────────────────
    /** Max 150 chars per AUTH-004 spec (MP-004) */
    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    /** Max 255 chars, email format per AUTH-004 spec (MP-005) */
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    /** Max 15 chars per AUTH-004 spec (MP-006) */
    @Column(name = "phone", length = 15)
    private String phone;

    /** Image URL — JPG/PNG per AUTH-004 spec (MP-001) */
    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    /** Date of birth — dd/MM/yyyy format per AUTH-004 spec (MP-007) */
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    /** MALE / FEMALE / OTHER per AUTH-004 spec (MP-008) */
    @Column(name = "gender", length = 10)
    private String gender;

    // ── Relationships (read-only in profile — MP-010, MP-011, MP-012) ──
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;

    // ── Account status ───────────────────────────────────────
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "failed_attempts", nullable = false)
    @Builder.Default
    private Integer failedAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
}
