package com.fillinus.erp.module.sales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** SAL-005 BR-003/BE-001: Deal Result is required, must be WON or LOST. */
@Data
public class CreateDealResultRequest {
    @NotBlank(message = "Deal Result is required")
    @Pattern(regexp = "WON|LOST", message = "Deal Result must be WON or LOST")
    private String result;

    private String comment;
}
