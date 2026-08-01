package com.fillinus.erp.module.sales.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DealNegotiationResponse {
    private Long id;
    private String negotiationNo;
    private Long quotationId;
    private String quotationNo;
    private Long opportunityId;
    private String opportunityName;
    private String customer;
    private Long salesRepId;
    private String salesRepName;
    private LocalDate meetingDate;
    private String communicationChannel;
    private String contactPerson;
    private String internalNote;
    private String status;
    private BigDecimal dealAmount;
    private List<DealNegotiationHistoryResponse> history;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
