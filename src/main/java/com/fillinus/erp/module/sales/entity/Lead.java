package com.fillinus.erp.module.sales.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Lead entity — maps to the `leads` table (V8__create_leads.sql, V1.1 fields in V11).
 * SAL-001 Lead Management V1.1 — BR-001 to BR-007
 */
@Entity
@Table(name = "leads")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** BR-001: Auto-generated Lead ID e.g. LEAD-0001 */
    @Column(name = "lead_id", nullable = false, unique = true, length = 50)
    private String leadId;

    @Column(name = "lead_name", nullable = false, length = 255)
    private String leadName;

    @Column(name = "company_name", length = 255)
    private String companyName;

    @Column(name = "contact_person", length = 255)
    private String contactPerson;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    /**
     * V1.1 BR-003: NEW / IN_PROGRESS / QUALIFIED / REJECTED — Postgres native enum lead_status.
     * QUALIFIED/REJECTED were added to the existing enum type in V10 (kept UPPER_CASE to match
     * the pre-existing NEW/IN_PROGRESS labels already in the DB — spec's "Qualified"/"Rejected"
     * casing is just the display label, not the stored value).
     */
    @Column(name = "status", nullable = false, length = 20)
    @ColumnTransformer(write = "?::lead_status")
    @Builder.Default
    private String status = "NEW";

    /** V1.1: New Client / Existing Client / Referral / Digital Lead — Postgres native enum lead_source */
    @Column(name = "source", length = 30)
    @ColumnTransformer(write = "?::lead_source")
    private String source;

    /** V1.1: Sales Rep responsible for this Lead (FK -> users.id) */
    @Column(name = "sales_rep_id")
    private Long salesRepId;

    /** V1.1: free-text remark/note */
    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    /** BR-005: Soft delete */
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    /** BR-006: Audit — who created this lead */
    @Column(name = "created_by")
    private Long createdBy;

    /** BR-006: Audit — who last updated this lead */
    @Column(name = "updated_by")
    private Long updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
