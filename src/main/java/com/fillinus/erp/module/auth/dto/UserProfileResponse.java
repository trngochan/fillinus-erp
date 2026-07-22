package com.fillinus.erp.module.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * AUTH-004 My Profile Response
 * Fields per spec sheet 4.Screen Item (MP-001 to MP-012)
 */
@Data
@Builder
public class UserProfileResponse {
    private Long   id;
    private String username;       // MP-009 — Read Only
    private String fullName;       // MP-004
    private String email;          // MP-005
    private String phone;          // MP-006
    private String avatarUrl;      // MP-001

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dateOfBirth; // MP-007

    private String gender;         // MP-008
    private String department;     // MP-010 — Read Only (FK)
    private String position;       // MP-011 — Read Only (FK)
    private String role;           // MP-012 — Read Only (FK)
}
