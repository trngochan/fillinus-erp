package com.fillinus.erp.module.sales.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Quotation Detail — one Product/Service line item of a Quotation (V13 migration).
 * "productService"/"unit"/"standardPrice" are temporary manual fields — no Product Info
 * master data exists yet (SAL-009). "amount" is always server-recalculated (BR-004).
 */
@Entity
@Table(name = "quotation_details")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id", nullable = false)
    private Quotation quotation;

    @Column(name = "product_service", nullable = false, length = 255)
    private String productService;

    @Column(name = "quantity", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal quantity = BigDecimal.ONE;

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(name = "standard_price", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal standardPrice = BigDecimal.ZERO;

    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal unitPrice = BigDecimal.ZERO;

    /** BR-004: Amount = Quantity x Unit Price — always server-calculated, never trusted from client */
    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "remark", length = 500)
    private String remark;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
