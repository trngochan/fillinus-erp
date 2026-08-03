package com.fillinus.erp.module.sales.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * SAL-001 V1.1 Convert Lead -> Opportunity popup fields.
 * Note: "projectType" (Agency/Label/Others) has no dedicated column on Opportunity
 * (SAL-002 spec doesn't define one) — it is folded into the Opportunity description.
 * Per SAL-002 BR-02 / Lead V1.1 FLOW-06 step 7: Opportunity Detail is collected in this
 * same Convert step (not auto-created) — an Opportunity must have at least one Detail line.
 */
@Data
public class ConvertLeadRequest {
    @NotBlank(message = "Opportunity Name is required")
    @Size(max = 255, message = "Opportunity Name must be at most 255 characters")
    private String opportunityName;

    @NotBlank(message = "Project Type is required")
    private String projectType; // Agency / Label / Others

    @PositiveOrZero(message = "Expected Deal Value must be >= 0")
    private BigDecimal expectedDealValue;

    @NotNull(message = "Sales Rep is required")
    private Long salesRepId;

    @NotEmpty(message = "Add at least one Service/Product detail")
    private List<@Valid OpportunityDetailRequest> details;
}
