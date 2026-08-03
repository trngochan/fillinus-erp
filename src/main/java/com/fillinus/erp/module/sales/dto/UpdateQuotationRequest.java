package com.fillinus.erp.module.sales.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * SAL-003 BR-006: only editable while Status = Draft. Editable fields: Quotation Date,
 * Expired Date, Currency, Remark, Detail lines. Quotation No/Opportunity/Customer/Sales
 * Rep/Status are NOT editable here.
 */
@Data
public class UpdateQuotationRequest {
    @NotNull(message = "Quotation Date is required")
    private LocalDate quotationDate;

    @NotNull(message = "Expired Date is required")
    private LocalDate expiredDate;

    private String currency;

    @Size(max = 500, message = "Remark must be at most 500 characters")
    private String remark;

    @NotEmpty(message = "Enter at least one Detail line")
    private List<@Valid QuotationDetailRequest> details;
}
