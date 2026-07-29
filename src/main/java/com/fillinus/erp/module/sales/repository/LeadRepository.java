package com.fillinus.erp.module.sales.repository;

import com.fillinus.erp.module.sales.entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {

    /**
     * BR-005: exclude soft-deleted; BR-004: exclude CONVERTED from main list.
     * Native query — Postgres enum columns (lead_status) need an explicit cast that
     * plain JPQL's CAST(... AS string) cannot express, only a native ::lead_status can.
     */
    @Query(value = "SELECT * FROM leads l WHERE l.is_deleted = false AND l.status <> 'CONVERTED' " +
           "AND (:search IS NULL OR LOWER(l.lead_name) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%')) " +
           "     OR LOWER(l.company_name) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%')) " +
           "     OR LOWER(l.lead_id) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%'))) " +
           "AND (:status IS NULL OR l.status = CAST(:status AS lead_status)) " +
           "ORDER BY l.created_at DESC", nativeQuery = true)
    List<Lead> findAllActive(@Param("search") String search, @Param("status") String status);

    Optional<Lead> findByLeadIdAndIsDeletedFalse(String leadId);

    /** For auto-generating the next Lead ID */
    @Query("SELECT COUNT(l) FROM Lead l")
    long countAll();

    boolean existsByLeadId(String leadId);
}
