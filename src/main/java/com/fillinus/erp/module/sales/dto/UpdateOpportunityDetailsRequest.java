package com.fillinus.erp.module.sales.dto;

import lombok.Data;

/** SAL-002: editable contact fields on an Opportunity (leadName/status are managed separately). */
@Data
public class UpdateOpportunityDetailsRequest {
    private String companyName;
    private String contactPerson;
    private String phone;
    private String email;
}