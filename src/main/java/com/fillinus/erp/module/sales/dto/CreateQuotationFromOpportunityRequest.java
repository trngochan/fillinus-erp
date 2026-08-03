package com.fillinus.erp.module.sales.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * SAL-003 BR-001: Quotation can only be created from an Opportunity.
 * BR-002/BR-003: Product/Quantity/Unit are inherited from the Opportunity Detail lines —
 * {@code detailPrices} carries the Unit Price the user enters per line (Flow 02 "Input
 * Detail Information" / EDT016), matched back to each line by opportunityDetailId.
 */
@Data
public class CreateQuotationFromOpportunityRequest {
    @NotNull(message = "Expired Date is required")
    private LocalDate expiredDate;

    /** VND / USD — defaults to VND if omitted */
    private String currency;

    @Size(max = 500, message = "Remark must be at most 500 characters")
    private String remark;

    @NotEmpty(message = "Enter a Unit Price for every detail line")
    private List<@Valid QuotationDetailPriceInput> detailPrices;
}
