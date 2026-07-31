package com.fillinus.erp.module.sales.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Quotation entity — maps to the `quotations` table (V13). SAL-003.
 * Only ever created from an Opportunity (BR-001) — no standalone create flow.
 */
@Entity
@Table(name = "quotations")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quotation_no", nullable = false, unique = true, length = 50)
    private String quotationNo;

    @Column(name = "opportunity_id", nullable = false)
    private Long opportunityId;

    /** Inherited from Opportunity at creation time (BR-002) — read-only afterwards */
    @Column(name = "customer", nullable = false, length = 255)
    private String customer;

    @Column(name = "sales_rep_id", nullable = false)
    private Long salesRepId;

    @Column(name = "quotation_date", nullable = false)
    @Builder.Default
    private LocalDate quotationDate = LocalDate.now();

    @Column(name = "expired_date", nullable = false)
    private LocalDate expiredDate;

    /** VND / USD — Postgres native enum quotation_currency */
    @Column(name = "currency", nullable = false, length = 10)
    @ColumnTransformer(write = "?::quotation_currency")
    @Builder.Default
    private String currency = "VND";

    /** DRAFT / SENT / ACCEPTED / REJECTED / EXPIRED — Postgres native enum quotation_status (BR-007) */
    @Column(name = "status", nullable = false, length = 20)
    @ColumnTransformer(write = "?::quotation_status")
    @Builder.Default
    private String status = "DRAFT";

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Builder.Default
    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<QuotationDetail> details = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
