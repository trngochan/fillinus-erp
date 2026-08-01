package com.fillinus.erp.module.sales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * SAL-004 BR-001/5.3: created from a Quotation via "Create Deal Negotiation".
 * BR-002 header fields + 5.3.2 Initial Negotiation History (the first timeline entry).
 */
@Data
public class CreateDealNegotiationRequest {
    @NotNull(message = "Meeting Date is required")
    private LocalDate meetingDate;

    @NotBlank(message = "Communication Channel is required")
    private String communicationChannel;

    private String contactPerson;
    private String internalNote;

    @NotBlank(message = "Discussion is required")
    private String discussion;

    private String customerFeedback;
    private String nextAction;
    private LocalDate nextFollowUpDate;
}
