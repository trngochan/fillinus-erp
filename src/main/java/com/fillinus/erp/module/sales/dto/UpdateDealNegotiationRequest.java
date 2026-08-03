package com.fillinus.erp.module.sales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/** SAL-004 BR-002: only the negotiation header fields — Status is read-only. */
@Data
public class UpdateDealNegotiationRequest {
    @NotNull(message = "Meeting Date is required")
    private LocalDate meetingDate;

    @NotBlank(message = "Communication Channel is required")
    @Size(max = 50, message = "Communication Channel must be at most 50 characters")
    private String communicationChannel;

    @Size(max = 200, message = "Contact Person must be at most 200 characters")
    private String contactPerson;

    private String internalNote;
}
