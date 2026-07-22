package com.fillinus.erp.module.auth.controller;

import com.fillinus.erp.common.ApiResponse;
import com.fillinus.erp.module.auth.dto.*;
import com.fillinus.erp.module.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * User Controller
 * Handles: AUTH-004 My Profile (GET/UPDATE), AUTH-005 Change Password
 * All endpoints require Bearer JWT token.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "AUTH-004 My Profile | AUTH-005 Change Password")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    /**
     * AUTH-004: Get My Profile
     * GET /api/users/me
     */
    @Operation(summary = "AUTH-004: Get My Profile", description = "Get current user's profile information")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(Authentication auth) {
        UserProfileResponse profile = userService.getProfile(auth.getName());
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    /**
     * AUTH-004: Update My Profile
     * PUT /api/users/me
     * Editable: full_name, email, phone, date_of_birth, gender
     */
    @Operation(summary = "AUTH-004: Update My Profile", description = "Update editable profile fields")
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            Authentication auth,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse updated = userService.updateProfile(auth.getName(), request);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully.", updated));
    }

    /**
     * AUTH-005: Change Password
     * PUT /api/users/me/change-password
     */
    @Operation(summary = "AUTH-005: Change Password", description = "Change password for logged-in user")
    @PutMapping("/me/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication auth,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(auth.getName(), request);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully.", null));
    }
}
