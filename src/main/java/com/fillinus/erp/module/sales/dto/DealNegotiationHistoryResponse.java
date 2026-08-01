package com.fillinus.erp.module.sales.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class DealNegotiationHistoryResponse {
    private Long id;
    private LocalDateTime discussionDate;
    private String updatedByName;
    private String discussion;
    private String customerFeedback;
    private String nextAction;
    private LocalDate nextFollowUpDate;
}
