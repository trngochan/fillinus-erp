package com.fillinus.erp.module.sales.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Deal Negotiation entity — maps to `deal_negotiations` (V14). SAL-004.
 * Only ever created from a Quotation (BR-001) — no standalone create flow.
 */
@Entity
@Table(name = "deal_negotiations")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealNegotiation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "negotiation_no", nullable = false, unique = true, length = 30)
    private String negotiationNo;

    @Column(name = "quotation_id", nullable = false)
    private Long quotationId;

    @Column(name = "opportunity_id", nullable = false)
    private Long opportunityId;

    /** Inherited from Quotation at creation time (BR-001) — read-only afterwards */
    @Column(name = "customer", nullable = false, length = 255)
    private String customer;

    @Column(name = "sales_rep_id", nullable = false)
    private Long salesRepId;

    @Column(name = "meeting_date", nullable = false)
    private LocalDate meetingDate;

    @Column(name = "communication_channel", nullable = false, length = 50)
    private String communicationChannel;

    @Column(name = "contact_person", length = 200)
    private String contactPerson;

    @Column(name = "internal_note", columnDefinition = "TEXT")
    private String internalNote;

    /** NEGOTIATING / WON / LOST — Postgres native enum deal_negotiation_status */
    @Column(name = "status", nullable = false, length = 20)
    @ColumnTransformer(write = "?::deal_negotiation_status")
    @Builder.Default
    private String status = "NEGOTIATING";

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Builder.Default
    @OneToMany(mappedBy = "dealNegotiation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DealNegotiationHistory> history = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
