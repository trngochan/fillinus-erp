package com.fillinus.erp.module.auth.service;

import com.fillinus.erp.module.auth.dto.*;
import com.fillinus.erp.module.auth.entity.User;
import com.fillinus.erp.module.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User Service — handles AUTH-004 (My Profile) and AUTH-005 (Change Password).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ─── AUTH-004: Get My Profile ────────────────────────────────────────────
    public UserProfileResponse getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found."));

        return mapToProfileResponse(user);
    }

    // ─── AUTH-004: Update My Profile ────────────────────────────────────────
    /**
     * Editable fields: full_name, email, phone, avatar_url, date_of_birth, gender
     * Read-only fields (not updated here): username, role, department, position
     */
    @Transactional
    public UserProfileResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found."));

        // Check email uniqueness (AUTH-004 MP-005 validation)
        if (!user.getEmail().equalsIgnoreCase(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already in use by another account.");
            }
        }

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());

        userRepository.save(user);
        log.info("Profile updated for user: {}", username);
        return mapToProfileResponse(user);
    }

    // ─── AUTH-005: Change Password ───────────────────────────────────────────
    /**
     * Business flow (per AUTH-005 spec):
     * Current Password → validate match → check policy → confirm match → update
     */
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found."));

        // AUTH-005: Current Password must be correct
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current Password is incorrect.");
        }

        // AUTH-005: New Password must differ from Current Password
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new RuntimeException("New Password must be different from Current Password.");
        }

        // AUTH-005: Confirm Password must match New Password
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Confirm Password does not match.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", username);
    }

    // ─── Helper ─────────────────────────────────────────────────────────────
    private UserProfileResponse mapToProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .department(user.getDepartment() != null ? user.getDepartment().getName() : null)
                .position(user.getPosition() != null ? user.getPosition().getName() : null)
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .build();
    }
}
