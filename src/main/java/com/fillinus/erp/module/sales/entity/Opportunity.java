package com.fillinus.erp.module.sales.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Opportunity entity — maps to the `opportunities` table (V8__create_opportunities.sql).
 * SAL-002 Opportunity — created when a Lead is converted.
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

    /** Source lead — for traceability (BR-006) */
    @Column(name = "lead_id", nullable = false)
    private Long leadId;

    /** Data copied from lead at time of conversion */
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

    /** The saler who converted the lead */
    @Column(name = "assigned_to", nullable = false)
    private Long assignedTo;

    /** NEW / IN_PROGRESS / WON / LOST — Postgres native enum opportunity_status */
    @Column(name = "status", nullable = false, length = 20)
    @ColumnTransformer(write = "?::opportunity_status")
    @Builder.Default
    private String status = "NEW";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
