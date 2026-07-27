package com.fillinus.erp.module.sales.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OpportunityResponse {
    private Long id;
    private String opportunityId;
    private Long leadId;
    private String leadName;
    private String companyName;
    private String contactPerson;
    private String phone;
    private String email;
    private Long assignedTo;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
