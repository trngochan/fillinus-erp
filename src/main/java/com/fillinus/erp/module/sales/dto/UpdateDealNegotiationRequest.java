package com.fillinus.erp.module.sales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/** SAL-004 BR-002: only the negotiation header fields — Status is read-only. */
@Data
public class UpdateDealNegotiationRequest {
    @NotNull(message = "Meeting Date is required")
    private LocalDate meetingDate;

    @NotBlank(message = "Communication Channel is required")
    private String communicationChannel;

    private String contactPerson;
    private String internalNote;
}
