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

    /** BR-005: exclude soft-deleted; BR-004: exclude CONVERTED from main list */
    @Query("SELECT l FROM Lead l WHERE l.isDeleted = false AND l.status <> 'CONVERTED' " +
           "AND (:search IS NULL OR LOWER(l.leadName) LIKE LOWER(CONCAT('%',:search,'%')) " +
           "     OR LOWER(l.companyName) LIKE LOWER(CONCAT('%',:search,'%')) " +
           "     OR LOWER(l.leadId) LIKE LOWER(CONCAT('%',:search,'%'))) " +
           "AND (:status IS NULL OR l.status = :status) " +
           "ORDER BY l.createdAt DESC")
    List<Lead> findAllActive(@Param("search") String search, @Param("status") String status);

    Optional<Lead> findByLeadIdAndIsDeletedFalse(String leadId);

    /** For auto-generating the next Lead ID */
    @Query("SELECT COUNT(l) FROM Lead l")
    long countAll();

    boolean existsByLeadId(String leadId);
}
