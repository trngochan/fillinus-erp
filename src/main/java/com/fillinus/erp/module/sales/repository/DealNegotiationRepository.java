package com.fillinus.erp.module.sales.repository;

import com.fillinus.erp.module.sales.entity.DealNegotiation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DealNegotiationRepository extends JpaRepository<DealNegotiation, Long> {

    /**
     * Salers only see their own Deal Negotiations; ADMIN/MANAGER see all (salesRepId = null).
     * Search matches Negotiation No/Customer/Quotation No (SAL-004 Search Area); Meeting Date
     * range filters DN-S-004/005.
     */
    @Query(value = "SELECT dn.* FROM deal_negotiations dn JOIN quotations q ON q.id = dn.quotation_id " +
           "WHERE dn.is_deleted = false AND (:salesRepId IS NULL OR dn.sales_rep_id = :salesRepId) " +
           "AND (:search IS NULL OR LOWER(dn.negotiation_no) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%')) " +
           "     OR LOWER(dn.customer) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%')) " +
           "     OR LOWER(q.quotation_no) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%'))) " +
           "AND (:meetingDateFrom IS NULL OR dn.meeting_date >= :meetingDateFrom) " +
           "AND (:meetingDateTo IS NULL OR dn.meeting_date <= :meetingDateTo) " +
           "ORDER BY dn.created_at DESC",
           countQuery = "SELECT COUNT(*) FROM deal_negotiations dn JOIN quotations q ON q.id = dn.quotation_id " +
           "WHERE dn.is_deleted = false AND (:salesRepId IS NULL OR dn.sales_rep_id = :salesRepId) " +
           "AND (:search IS NULL OR LOWER(dn.negotiation_no) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%')) " +
           "     OR LOWER(dn.customer) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%')) " +
           "     OR LOWER(q.quotation_no) LIKE LOWER(CONCAT('%',CAST(:search AS text),'%'))) " +
           "AND (:meetingDateFrom IS NULL OR dn.meeting_date >= :meetingDateFrom) " +
           "AND (:meetingDateTo IS NULL OR dn.meeting_date <= :meetingDateTo)",
           nativeQuery = true)
    Page<DealNegotiation> findMine(@Param("salesRepId") Long salesRepId, @Param("search") String search,
                                    @Param("meetingDateFrom") LocalDate meetingDateFrom, @Param("meetingDateTo") LocalDate meetingDateTo,
                                    Pageable pageable);

    Optional<DealNegotiation> findByQuotationIdAndIsDeletedFalse(Long quotationId);
}
