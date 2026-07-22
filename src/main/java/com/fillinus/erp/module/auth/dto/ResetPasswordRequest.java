package com.fillinus.erp.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * AUTH-003 Reset Password Request
 * Requires: token (from email link) + new password + confirm password
 * Password policy per AUTH-005 spec: min 12 chars, 1 uppercase, 1 special char
 */
@Data
public class ResetPasswordRequest {

    @NotBlank(message = "Token is required.")
    private String token;

    /**
     * Password policy from AUTH-005:
     * - Minimum 12 characters
     * - At least 1 uppercase letter
     * - At least 1 special character
     */
    @NotBlank(message = "New Password is required.")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{12,}$",
        message = "Password policy is not satisfied. Min 12 chars, 1 uppercase, 1 special character."
    )
    private String newPassword;

    @NotBlank(message = "Confirm Password is required.")
    private String confirmPassword;
}
