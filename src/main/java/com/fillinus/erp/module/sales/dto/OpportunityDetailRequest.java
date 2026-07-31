package com.fillinus.erp.module.sales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/** SAL-002 Opportunity Detail — one Service/Product line item. */
@Data
public class OpportunityDetailRequest {
    @NotBlank(message = "Service/Product is required")
    private String serviceProduct;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    private String unit;
    private String remark;
}
