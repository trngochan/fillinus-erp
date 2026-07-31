package com.fillinus.erp.module.sales.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Opportunity Detail — one Service/Product line item of an Opportunity (V12 migration).
 * "serviceProduct"/"unit" are temporary free-text fields — no Product/Service or Unit
 * master data exists yet (SAL-009 Product Info).
 */
@Entity
@Table(name = "opportunity_details")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpportunityDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private Opportunity opportunity;

    @Column(name = "service_product", nullable = false, length = 255)
    private String serviceProduct;

    @Column(name = "quantity", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal quantity = BigDecimal.ONE;

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(name = "remark", length = 500)
    private String remark;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
