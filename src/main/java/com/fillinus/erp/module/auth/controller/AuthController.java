package com.fillinus.erp.module.auth.controller;

import com.fillinus.erp.common.ApiResponse;
import com.fillinus.erp.module.auth.dto.*;
import com.fillinus.erp.module.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * Handles: AUTH-001 Login, AUTH-002 Forgot Password, AUTH-003 Reset Password
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "AUTH-001 Login | AUTH-002 Forgot Password | AUTH-003 Reset Password")
public class AuthController {

    private final AuthService authService;

    /**
     * AUTH-001: Login
     * POST /api/auth/login
     * Business flow: Input → Validate → Authenticate → JWT Token
     */
    @Operation(summary = "AUTH-001: Login", description = "Authenticate user and return JWT token")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful.", response));
    }

    /**
     * AUTH-002: Forgot Password
     * POST /api/auth/forgot-password
     * Business flow: Enter email → Send reset link
     */
    @Operation(summary = "AUTH-002: Forgot Password", description = "Send password reset link to email")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Password reset link has been sent to your email.", null));
    }

    /**
     * AUTH-003: Reset Password
     * POST /api/auth/reset-password
     * Business flow: Token + New Password → Validate → Update
     */
    @Operation(summary = "AUTH-003: Reset Password", description = "Reset password using token from email")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Password has been reset successfully.", null));
    }
}
