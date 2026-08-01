package com.fillinus.erp.module.sales.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

/** SAL-004 BR-003: one immutable timeline entry, appended via "Add History". */
@Data
public class AddNegotiationHistoryRequest {
    @NotBlank(message = "Discussion is required")
    private String discussion;

    private String customerFeedback;
    private String nextAction;
    private LocalDate nextFollowUpDate;
}
