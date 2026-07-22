package com.fillinus.erp.module.auth.entity;

import com.fillinus.erp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Role entity — maps to the `roles` table (V1__create_roles.sql).
 * Referenced by User for access control.
 */
@Entity
@Table(name = "roles")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
