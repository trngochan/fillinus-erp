package com.fillinus.erp.module.sales.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class QuotationDetailResponse {
    private Long id;
    private String productService;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal standardPrice;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private String remark;
}
