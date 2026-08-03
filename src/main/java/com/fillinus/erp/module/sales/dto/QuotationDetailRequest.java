package com.fillinus.erp.module.sales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/** SAL-003 Quotation Detail line — one Product/Service row. */
@Data
public class QuotationDetailRequest {
    @NotBlank(message = "Product/Service is required")
    @Size(max = 255, message = "Product/Service must be at most 255 characters")
    private String productService;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    @Size(max = 50, message = "Unit must be at most 50 characters")
    private String unit;

    @PositiveOrZero(message = "Standard Price must be >= 0")
    private BigDecimal standardPrice;

    @NotNull(message = "Unit Price is required")
    @PositiveOrZero(message = "Unit Price must be >= 0")
    private BigDecimal unitPrice;

    @Size(max = 500, message = "Remark must be at most 500 characters")
    private String remark;
}
