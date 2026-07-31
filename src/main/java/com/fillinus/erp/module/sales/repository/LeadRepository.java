package com.fillinus.erp.module.sales.repository;

import com.fillinus.erp.module.sales.entity.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {

    /**
     * BR-005: exclude soft-deleted only — V1.1 no longer hides Qualified/Rejected leads
     * from the main grid (Status filter dropdown lets users filter to any status).
     * Native query — Postgres enum columns (lead_status) need an explicit cast that
     * plain JPQL's CAST(... AS string) cannot express, only a native ::lead_status can.
     * Pageable native query requires an explicit countQuery (Spring Data can't derive one).
     */
    @Query(value = "SELECT * FROM leads l WHERE l.is_deleted = false " +
           "AND (:search IS NULL OR LOWER(l.lead_name) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%')) " +
           "     OR LOWER(l.company_name) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%')) " +
           "     OR LOWER(l.lead_id) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%'))) " +
           "AND (:status IS NULL OR l.status = CAST(:status AS lead_status)) " +
           "ORDER BY l.created_at DESC",
           countQuery = "SELECT COUNT(*) FROM leads l WHERE l.is_deleted = false " +
           "AND (:search IS NULL OR LOWER(l.lead_name) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%')) " +
           "     OR LOWER(l.company_name) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%')) " +
           "     OR LOWER(l.lead_id) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%'))) " +
           "AND (:status IS NULL OR l.status = CAST(:status AS lead_status))",
           nativeQuery = true)
    Page<Lead> findAllActive(@Param("search") String search, @Param("status") String status, Pageable pageable);

    Optional<Lead> findByLeadIdAndIsDeletedFalse(String leadId);

    /** For auto-generating the next Lead ID */
    @Query("SELECT COUNT(l) FROM Lead l")
    long countAll();

    boolean existsByLeadId(String leadId);

    /** V1.1 uniqueness validation — phone/email must not collide with another active lead */
    boolean existsByPhoneAndIsDeletedFalseAndIdNot(String phone, Long id);
    boolean existsByEmailAndIsDeletedFalseAndIdNot(String email, Long id);
    boolean existsByPhoneAndIsDeletedFalse(String phone);
    boolean existsByEmailAndIsDeletedFalse(String email);
}
