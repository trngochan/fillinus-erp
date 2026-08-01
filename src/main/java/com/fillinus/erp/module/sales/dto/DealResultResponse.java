package com.fillinus.erp.module.sales.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class DealResultResponse {
    private Long id;
    private Long dealNegotiationId;
    private String negotiationNo;
    private Long quotationId;
    private String quotationNo;
    private Long opportunityId;
    private String opportunityName;
    private String customer;
    private Long salesRepId;
    private String salesRepName;
    private BigDecimal dealAmount;
    private String result;
    private String comment;
    private String decisionByName;
    private LocalDateTime decisionDate;
    private LocalDateTime createdAt;
}
