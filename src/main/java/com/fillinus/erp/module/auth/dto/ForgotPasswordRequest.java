package com.fillinus.erp.module.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AUTH-002 Forgot Password Request
 * Business flow: Enter email → system sends reset link to that email
 */
@Data
public class ForgotPasswordRequest {

    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email format.")
    private String email;
}
