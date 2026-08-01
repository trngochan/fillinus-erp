package com.fillinus.erp.module.sales.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Deal Result entity — maps to `deal_results` (V14). SAL-005.
 * Only ever created from a Deal Negotiation via "Complete Negotiation" (BR-001).
 * BC-001/BC-006: one result per negotiation, immutable once saved — no update/delete flow.
 */
@Entity
@Table(name = "deal_results")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "deal_negotiation_id", nullable = false, unique = true)
    private Long dealNegotiationId;

    @Column(name = "quotation_id", nullable = false)
    private Long quotationId;

    @Column(name = "opportunity_id", nullable = false)
    private Long opportunityId;

    @Column(name = "customer", nullable = false, length = 255)
    private String customer;

    @Column(name = "sales_rep_id", nullable = false)
    private Long salesRepId;

    @Column(name = "deal_amount", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal dealAmount = BigDecimal.ZERO;

    /** WON / LOST — Postgres native enum deal_result_type */
    @Column(name = "result", nullable = false, length = 10)
    @ColumnTransformer(write = "?::deal_result_type")
    private String result;

    @Column(name = "comment", length = 1000)
    private String comment;

    @Column(name = "decision_by")
    private Long decisionBy;

    @CreationTimestamp
    @Column(name = "decision_date", updatable = false)
    private LocalDateTime decisionDate;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
