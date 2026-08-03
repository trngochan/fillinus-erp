package com.fillinus.erp.module.sales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/** SAL-002 Opportunity Detail — one Service/Product line item. */
@Data
public class OpportunityDetailRequest {
    @NotBlank(message = "Service/Product is required")
    @Size(max = 255, message = "Service/Product must be at most 255 characters")
    private String serviceProduct;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    @Size(max = 50, message = "Unit must be at most 50 characters")
    private String unit;

    @Size(max = 500, message = "Remark must be at most 500 characters")
    private String remark;
}
