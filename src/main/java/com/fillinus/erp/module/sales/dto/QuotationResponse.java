package com.fillinus.erp.module.sales.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class QuotationResponse {
    private Long id;
    private String quotationNo;
    private Long opportunityId;
    private String opportunityName;
    private String customer;
    private Long salesRepId;
    private String salesRepName;
    private LocalDate quotationDate;
    private LocalDate expiredDate;
    private String currency;
    private String status;
    private String remark;
    private BigDecimal totalAmount;
    private List<QuotationDetailResponse> details;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
