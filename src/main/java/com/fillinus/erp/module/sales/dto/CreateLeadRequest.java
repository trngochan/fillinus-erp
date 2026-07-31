package com.fillinus.erp.module.sales.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * SAL-001 V1.1 — used for both Create and Edit.
 * `status` is only honored on Edit (New/In Progress/Rejected — Qualified is system-only,
 * set exclusively by Convert). It is ignored on Create (always starts as New).
 */
@Data
public class CreateLeadRequest {
    @NotBlank(message = "Lead name is required")
    private String leadName;

    @NotBlank(message = "Company is required")
    private String companyName;

    private String contactPerson;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    /** New Client / Existing Client / Referral / Digital Lead */
    private String source;

    @NotNull(message = "Sales Rep is required")
    private Long salesRepId;

    private String remark;

    /** Edit-only: New / In Progress / Rejected */
    private String status;
}
