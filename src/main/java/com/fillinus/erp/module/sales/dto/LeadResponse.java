package com.fillinus.erp.module.sales.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LeadResponse {
    private Long id;
    private String leadId;
    private String leadName;
    private String companyName;
    private String contactPerson;
    private String phone;
    private String email;
    private String status;
    private String source;
    private Long salesRepId;
    private String salesRepName;
    private String remark;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
