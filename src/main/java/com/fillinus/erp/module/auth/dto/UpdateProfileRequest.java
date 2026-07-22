package com.fillinus.erp.module.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * AUTH-004 Update Profile Request
 * Editable fields per spec (MP-004 to MP-008 are editable; MP-009,010,011,012 read-only)
 */
@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Full Name is required.")
    @Size(max = 150, message = "Full Name must not exceed 150 characters.")
    private String fullName;  // MP-004

    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email format.")
    @Size(max = 255)
    private String email;     // MP-005

    @Size(max = 15, message = "Phone number must not exceed 15 characters.")
    @Pattern(regexp = "^[0-9+\\-() ]*$", message = "Invalid phone number format.")
    private String phone;     // MP-006

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateOfBirth; // MP-007

    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Gender must be MALE, FEMALE, or OTHER.")
    private String gender;    // MP-008
}
