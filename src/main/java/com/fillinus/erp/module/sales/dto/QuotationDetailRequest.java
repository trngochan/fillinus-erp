package com.fillinus.erp.module.sales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

/** SAL-003 Quotation Detail line — one Product/Service row. */
@Data
public class QuotationDetailRequest {
    @NotBlank(message = "Product/Service is required")
    private String productService;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    private String unit;

    @PositiveOrZero(message = "Standard Price must be >= 0")
    private BigDecimal standardPrice;

    @NotNull(message = "Unit Price is required")
    @PositiveOrZero(message = "Unit Price must be >= 0")
    private BigDecimal unitPrice;

    private String remark;
}
