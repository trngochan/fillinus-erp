package com.fillinus.erp.module.sales.repository;

import com.fillinus.erp.module.sales.entity.DealNegotiationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DealNegotiationHistoryRepository extends JpaRepository<DealNegotiationHistory, Long> {
    List<DealNegotiationHistory> findByDealNegotiation_IdOrderByDiscussionDateDesc(Long dealNegotiationId);
}
