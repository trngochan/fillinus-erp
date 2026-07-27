package com.fillinus.erp.module.sales.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateLeadRequest {
    @NotBlank(message = "Lead name is required")
    private String leadName;
    private String companyName;
    private String contactPerson;
    private String phone;
    private String email;
}
