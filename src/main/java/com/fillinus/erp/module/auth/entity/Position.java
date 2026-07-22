package com.fillinus.erp.module.auth.entity;

import com.fillinus.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Position entity — maps to the `positions` table (V3__create_positions.sql).
 * Belongs to a Department; referenced by User.
 */
@Entity
@Table(name = "positions")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Position extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "code", unique = true, length = 50)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
