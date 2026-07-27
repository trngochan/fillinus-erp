package com.fillinus.erp.module.sales.repository;

import com.fillinus.erp.module.sales.entity.Opportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    /** Return only opportunities assigned to this saler */
    List<Opportunity> findByAssignedToOrderByCreatedAtDesc(Long assignedTo);

    boolean existsByLeadId(Long leadId);
}
