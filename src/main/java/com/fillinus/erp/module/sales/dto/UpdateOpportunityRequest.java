package com.fillinus.erp.module.sales.dto;

import lombok.Data;

@Data
public class UpdateOpportunityRequest {
    private String status; // NEW / IN_PROGRESS / WON / LOST
}
