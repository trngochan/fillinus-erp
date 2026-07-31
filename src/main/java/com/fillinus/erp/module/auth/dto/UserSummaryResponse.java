package com.fillinus.erp.module.auth.dto;

import lombok.Builder;
import lombok.Data;

/** Minimal user info for dropdown/lookup use (e.g. Sales Rep selector) — never expose password/email here. */
@Data
@Builder
public class UserSummaryResponse {
    private Long id;
    private String fullName;
}
