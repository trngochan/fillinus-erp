package com.fillinus.erp.module.sales.repository;

import com.fillinus.erp.module.sales.entity.Quotation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, Long> {

    /**
     * Salers only see their own Quotations; ADMIN/MANAGER see all (salesRepId = null —
     * resolved by the controller from the JWT role claim). Search matches Quotation No/Customer;
     * status filters by exact Quotation Status (SCH006).
     */
    @Query(value = "SELECT * FROM quotations q WHERE q.is_deleted = false AND (:salesRepId IS NULL OR q.sales_rep_id = :salesRepId) " +
           "AND (:search IS NULL OR LOWER(q.quotation_no) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%')) " +
           "     OR LOWER(q.customer) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%'))) " +
           "AND (:status IS NULL OR q.status = CAST(:status AS quotation_status)) " +
           "ORDER BY q.created_at DESC",
           countQuery = "SELECT COUNT(*) FROM quotations q WHERE q.is_deleted = false AND (:salesRepId IS NULL OR q.sales_rep_id = :salesRepId) " +
           "AND (:search IS NULL OR LOWER(q.quotation_no) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%')) " +
           "     OR LOWER(q.customer) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%'))) " +
           "AND (:status IS NULL OR q.status = CAST(:status AS quotation_status))",
           nativeQuery = true)
    Page<Quotation> findMine(@Param("salesRepId") Long salesRepId, @Param("search") String search,
                              @Param("status") String status, Pageable pageable);

    boolean existsByOpportunityIdAndIsDeletedFalse(Long opportunityId);
}
