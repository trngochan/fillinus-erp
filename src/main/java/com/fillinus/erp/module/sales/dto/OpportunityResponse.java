package com.fillinus.erp.module.sales.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OpportunityResponse {
    private Long id;
    private String opportunityId;
    private Long leadId;
    private String opportunityName;
    private String customer;
    private Long salesRepId;
    private String salesRepName;
    private String stage;
    private BigDecimal expectedRevenue;
    private String description;
    private String lifecycleStatus;
    private List<OpportunityDetailResponse> details;
    /** Whether a Quotation already exists for this Opportunity — Edit/Create Quotation are blocked when true */
    private boolean hasQuotation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
