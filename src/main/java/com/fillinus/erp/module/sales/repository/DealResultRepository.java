package com.fillinus.erp.module.sales.repository;

import com.fillinus.erp.module.sales.entity.DealResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DealResultRepository extends JpaRepository<DealResult, Long> {

    /**
     * Salers only see their own Deal Results; ADMIN/MANAGER see all (salesRepId = null).
     * Search matches Negotiation No/Quotation No/Opportunity Name/Customer (SAL-005 Search
     * Area); result filters by Deal Result (SEA006).
     */
    @Query(value = "SELECT dr.* FROM deal_results dr " +
           "JOIN deal_negotiations dn ON dn.id = dr.deal_negotiation_id " +
           "JOIN quotations q ON q.id = dr.quotation_id " +
           "JOIN opportunities o ON o.id = dr.opportunity_id " +
           "WHERE dr.is_deleted = false AND (:salesRepId IS NULL OR dr.sales_rep_id = :salesRepId) " +
           "AND (:search IS NULL OR LOWER(dn.negotiation_no) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%')) " +
           "     OR LOWER(q.quotation_no) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%')) " +
           "     OR LOWER(o.opportunity_name) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%')) " +
           "     OR LOWER(dr.customer) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%'))) " +
           "AND (:result IS NULL OR dr.result = CAST(:result AS deal_result_type)) " +
           "ORDER BY dr.decision_date DESC",
           countQuery = "SELECT COUNT(*) FROM deal_results dr " +
           "JOIN deal_negotiations dn ON dn.id = dr.deal_negotiation_id " +
           "JOIN quotations q ON q.id = dr.quotation_id " +
           "JOIN opportunities o ON o.id = dr.opportunity_id " +
           "WHERE dr.is_deleted = false AND (:salesRepId IS NULL OR dr.sales_rep_id = :salesRepId) " +
           "AND (:search IS NULL OR LOWER(dn.negotiation_no) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%')) " +
           "     OR LOWER(q.quotation_no) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%')) " +
           "     OR LOWER(o.opportunity_name) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%')) " +
           "     OR LOWER(dr.customer) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%'))) " +
           "AND (:result IS NULL OR dr.result = CAST(:result AS deal_result_type))",
           nativeQuery = true)
    Page<DealResult> findMine(@Param("salesRepId") Long salesRepId, @Param("search") String search,
                               @Param("result") String result, Pageable pageable);

    Optional<DealResult> findByDealNegotiationIdAndIsDeletedFalse(Long dealNegotiationId);
}
