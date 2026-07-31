package com.fillinus.erp.module.sales.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OpportunityDetailResponse {
    private Long id;
    private String serviceProduct;
    private BigDecimal quantity;
    private String unit;
    private String remark;
}
