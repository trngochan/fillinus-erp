package com.fillinus.erp.module.auth.dto;

import lombok.Builder;
import lombok.Data;

/** AUTH-001 Login Response — returns JWT token and user info */
@Data
@Builder
public class LoginResponse {
    private String  accessToken;
    private String  tokenType;
    private Long    expiresIn;   // seconds
    private String  username;
    private String  fullName;
    private String  role;
}
