package com.fillinus.erp.module.sales.service;

import com.fillinus.erp.module.sales.dto.OpportunityResponse;
import com.fillinus.erp.module.sales.dto.UpdateOpportunityDetailsRequest;
import com.fillinus.erp.module.sales.dto.UpdateOpportunityRequest;
import com.fillinus.erp.module.sales.entity.Lead;
import com.fillinus.erp.module.sales.entity.Opportunity;
import com.fillinus.erp.module.sales.repository.OpportunityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Opportunity Service — SAL-002
 * Salers can only see/edit opportunities assigned to them.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;

    /** Created when a Lead is converted (SAL-001 BUSINESS-06) — called from LeadService. */
    @Transactional
    public OpportunityResponse createFromLead(Lead lead, Long currentUserId) {
        Opportunity opp = Opportunity.builder()
                .opportunityId(generateOpportunityId())
                .leadId(lead.getId())
                .leadName(lead.getLeadName())
                .companyName(lead.getCompanyName())
                .contactPerson(lead.getContactPerson())
                .phone(lead.getPhone())
                .email(lead.getEmail())
                .assignedTo(currentUserId)
                .status("NEW")
                .build();
        opportunityRepository.save(opp);
        log.info("Opportunity created: {} from Lead {} by userId={}", opp.getOpportunityId(), lead.getLeadId(), currentUserId);
        return toResponse(opp);
    }

    /** Get MY opportunities (assigned to me) */
    public List<OpportunityResponse> getMyOpportunities(Long currentUserId) {
        return opportunityRepository.findByAssignedToOrderByCreatedAtDesc(currentUserId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /** View opportunity detail — can only view your own */
    public OpportunityResponse getOpportunity(Long id, Long currentUserId) {
        return toResponse(findOwnedOpportunity(id, currentUserId));
    }

    /** Edit contact fields — opportunityId/leadId/leadName stay immutable (copied at conversion) */
    @Transactional
    public OpportunityResponse updateOpportunity(Long id, UpdateOpportunityDetailsRequest request, Long currentUserId) {
        Opportunity opp = findOwnedOpportunity(id, currentUserId);
        opp.setCompanyName(request.getCompanyName());
        opp.setContactPerson(request.getContactPerson());
        opp.setPhone(request.getPhone());
        opp.setEmail(request.getEmail());
        opportunityRepository.save(opp);
        log.info("Opportunity updated: {} by userId={}", opp.getOpportunityId(), currentUserId);
        return toResponse(opp);
    }

    /** Update status: NEW / IN_PROGRESS / WON / LOST */
    @Transactional
    public OpportunityResponse updateStatus(Long id, UpdateOpportunityRequest request, Long currentUserId) {
        Opportunity opp = findOwnedOpportunity(id, currentUserId);
        opp.setStatus(request.getStatus());
        opportunityRepository.save(opp);
        log.info("Opportunity {} status -> {} by userId={}", opp.getOpportunityId(), request.getStatus(), currentUserId);
        return toResponse(opp);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────
    private Opportunity findOwnedOpportunity(Long id, Long currentUserId) {
        Opportunity opp = opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Opportunity not found: " + id));
        if (!opp.getAssignedTo().equals(currentUserId)) {
            throw new RuntimeException("You can only access your own opportunities.");
        }
        return opp;
    }

    private String generateOpportunityId() {
        long count = opportunityRepository.count() + 1;
        return String.format("OPP-%04d", count);
    }

    private OpportunityResponse toResponse(Opportunity opp) {
        return OpportunityResponse.builder()
                .id(opp.getId())
                .opportunityId(opp.getOpportunityId())
                .leadId(opp.getLeadId())
                .leadName(opp.getLeadName())
                .companyName(opp.getCompanyName())
                .contactPerson(opp.getContactPerson())
                .phone(opp.getPhone())
                .email(opp.getEmail())
                .assignedTo(opp.getAssignedTo())
                .status(opp.getStatus())
                .createdAt(opp.getCreatedAt())
                .updatedAt(opp.getUpdatedAt())
                .build();
    }
}