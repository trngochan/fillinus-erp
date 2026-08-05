package com.fillinus.erp.module.sales.repository;

import com.fillinus.erp.module.sales.entity.Opportunity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    /**
     * Salers only see their own Opportunities; ADMIN/MANAGER see all (salesRepId = null —
     * resolved by the controller from the JWT role claim). Search matches Opportunity
     * No/Name/Customer (SAL-002 Search Area). Native query for consistency + explicit
     * countQuery required for Pageable native queries (Spring Data can't derive one).
     */
    @Query(value = "SELECT * FROM opportunities o WHERE (:salesRepId IS NULL OR o.sales_rep_id = :salesRepId) " +
           "AND (:search IS NULL OR LOWER(o.opportunity_id) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%')) " +
           "     OR LOWER(o.opportunity_name) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%')) " +
           "     OR LOWER(o.customer) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%'))) " +
           "ORDER BY o.created_at DESC",
           countQuery = "SELECT COUNT(*) FROM opportunities o WHERE (:salesRepId IS NULL OR o.sales_rep_id = :salesRepId) " +
           "AND (:search IS NULL OR LOWER(o.opportunity_id) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%')) " +
           "     OR LOWER(o.opportunity_name) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%')) " +
           "     OR LOWER(o.customer) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%')))",
           nativeQuery = true)
    Page<Opportunity> findMine(@Param("salesRepId") Long salesRepId, @Param("search") String search, Pageable pageable);

    /** BUG_SALE-001 #11: prevent two Opportunities from sharing the exact same name. */
    boolean existsByOpportunityNameIgnoreCase(String opportunityName);
    boolean existsByOpportunityNameIgnoreCaseAndIdNot(String opportunityName, Long id);
}
