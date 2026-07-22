package com.fillinus.erp.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * AUTH-005 Change Password Request
 * Validations per 6.Validation sheet:
 *   - Current Password: required, must match existing
 *   - New Password: required, policy min 12 chars + uppercase + special
 *   - Confirm Password: required, must match new password
 *   - New Password must differ from Current Password
 */
@Data
public class ChangePasswordRequest {

    @NotBlank(message = "Current Password is required.")
    private String currentPassword;  // CP001

    @NotBlank(message = "New Password is required.")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{12,}$",
        message = "Password policy is not satisfied. Min 12 chars, 1 uppercase, 1 special character."
    )
    private String newPassword;      // CP002

    @NotBlank(message = "Confirm Password is required.")
    private String confirmPassword;  // CP003
}
