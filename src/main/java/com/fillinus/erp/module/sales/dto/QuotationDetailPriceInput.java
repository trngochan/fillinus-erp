package com.fillinus.erp.module.sales.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

/**
 * SAL-003 BR-002/BR-003: Product/Quantity/Unit are inherited as-is from the Opportunity
 * Detail line (identified by {@code opportunityDetailId}) — only price is entered here.
 */
@Data
public class QuotationDetailPriceInput {
    @NotNull(message = "opportunityDetailId is required")
    private Long opportunityDetailId;

    @PositiveOrZero(message = "Standard Price must be >= 0")
    private BigDecimal standardPrice;

    @NotNull(message = "Unit Price is required")
    @PositiveOrZero(message = "Unit Price must be >= 0")
    private BigDecimal unitPrice;
}
