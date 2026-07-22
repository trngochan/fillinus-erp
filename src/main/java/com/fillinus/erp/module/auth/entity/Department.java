package com.fillinus.erp.module.auth.entity;

import com.fillinus.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Department entity — maps to the `departments` table (V2__create_departments.sql).
 * Referenced by User and Position.
 */
@Entity
@Table(name = "departments")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "code", unique = true, length = 50)
    private String code;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
