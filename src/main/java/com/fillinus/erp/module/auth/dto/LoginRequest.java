package com.fillinus.erp.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AUTH-001 Login Request
 * Fields: username (TXT001), password (TXT002)
 * Validations per 6.Validation sheet:
 *   - Username: required
 *   - Password: required
 */
@Data
public class LoginRequest {

    @NotBlank(message = "Username is required.")
    private String username;

    @NotBlank(message = "Password is required.")
    private String password;

    private boolean rememberMe;
}
