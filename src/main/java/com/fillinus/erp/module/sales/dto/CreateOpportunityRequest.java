package com.fillinus.erp.module.sales.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * SAL-002 Opportunity — used for both direct Create (Business-02) and Edit (Business-04).
 * "customer" is temporary free-text (no Customer master yet — SAL-007).
 */
@Data
public class CreateOpportunityRequest {
    @NotBlank(message = "Opportunity Name is required")
    private String opportunityName;

    @NotBlank(message = "Customer is required")
    private String customer;

    @NotNull(message = "Sales Rep is required")
    private Long salesRepId;

    /** PROSPECTING / QUALIFICATION / PROPOSAL / NEGOTIATION / CLOSED_WON / CLOSED_LOST */
    private String stage;

    private BigDecimal expectedRevenue;
    private String description;

    @NotEmpty(message = "Add at least one Service/Product detail")
    private List<@Valid OpportunityDetailRequest> details;
}
