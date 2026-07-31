package com.fillinus.erp.module.sales.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Opportunity entity — maps to the `opportunities` table (redesigned in V12).
 * SAL-002 Opportunity — created directly OR from a converted Lead (leadId nullable).
 * "customer" is a temporary free-text field — no Customer master exists yet (SAL-007).
 */
@Entity
@Table(name = "opportunities")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Opportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "opportunity_id", nullable = false, unique = true, length = 50)
    private String opportunityId;

    /** Source lead — nullable, Opportunity can be created directly (Business-02) */
    @Column(name = "lead_id")
    private Long leadId;

    @Column(name = "opportunity_name", nullable = false, length = 255)
    private String opportunityName;

    /** Temp free-text — no Customer master yet (SAL-007 Existing Client) */
    @Column(name = "customer", nullable = false, length = 255)
    private String customer;

    /** Sales Rep responsible for this Opportunity (FK -> users.id) */
    @Column(name = "sales_rep_id", nullable = false)
    private Long salesRepId;

    /** PROSPECTING / QUALIFICATION / PROPOSAL / NEGOTIATION / CLOSED_WON / CLOSED_LOST */
    @Column(name = "stage", nullable = false, length = 20)
    @ColumnTransformer(write = "?::opportunity_stage")
    @Builder.Default
    private String stage = "PROSPECTING";

    @Column(name = "expected_revenue", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal expectedRevenue = BigDecimal.ZERO;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** OPEN / CONVERTED / CLOSED — Postgres native enum opportunity_lifecycle_status */
    @Column(name = "lifecycle_status", nullable = false, length = 20)
    @ColumnTransformer(write = "?::opportunity_lifecycle_status")
    @Builder.Default
    private String lifecycleStatus = "OPEN";

    @Builder.Default
    @OneToMany(mappedBy = "opportunity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OpportunityDetail> details = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
