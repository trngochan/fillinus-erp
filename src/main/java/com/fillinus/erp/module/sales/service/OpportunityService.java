package com.fillinus.erp.module.sales.service;

import com.fillinus.erp.common.PageResponse;
import com.fillinus.erp.module.auth.entity.User;
import com.fillinus.erp.module.auth.repository.UserRepository;
import com.fillinus.erp.module.sales.dto.*;
import com.fillinus.erp.module.sales.entity.Lead;
import com.fillinus.erp.module.sales.entity.Opportunity;
import com.fillinus.erp.module.sales.entity.OpportunityDetail;
import com.fillinus.erp.module.sales.repository.LeadRepository;
import com.fillinus.erp.module.sales.repository.OpportunityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Opportunity Service — SAL-002
 * Salers can only see/edit opportunities assigned to them (sales_rep_id).
 * "customer"/detail "serviceProduct"/"unit" are temp free-text fields — no Customer or
 * Product/Unit master data exists yet (SAL-006/007/009 not built).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final UserRepository userRepository;
    private final LeadRepository leadRepository;

    /**
     * Created when a Lead is converted (SAL-001 V1.1 BUSINESS-06). "projectType" has no
     * dedicated Opportunity column (SAL-002 defines none) — folded into description.
     * Per SAL-002 BR-02 / Lead V1.1 FLOW-06 step 7: Opportunity Detail is collected in this
     * same Convert step (not auto-created) — the request must carry at least one Detail line.
     */
    @Transactional
    public OpportunityResponse createFromLead(Lead lead, Long currentUserId, ConvertLeadRequest request) {
        validateUniqueName(request.getOpportunityName(), null);
        String description = "Project Type: " + request.getProjectType()
                + (request.getDescription() != null && !request.getDescription().isBlank() ? "\n" + request.getDescription() : "");
        Opportunity opp = Opportunity.builder()
                .opportunityId(generateOpportunityId())
                .leadId(lead.getId())
                .opportunityName(request.getOpportunityName())
                .customer(lead.getCompanyName())
                .salesRepId(request.getSalesRepId())
                .stage(request.getStage() != null ? request.getStage() : "PROSPECTING")
                .expectedRevenue(request.getExpectedDealValue() != null ? request.getExpectedDealValue() : BigDecimal.ZERO)
                .description(description)
                .lifecycleStatus("OPEN")
                .build();
        applyDetails(opp, request.getDetails());
        opportunityRepository.save(opp);
        log.info("Opportunity created: {} from Lead {} by userId={}", opp.getOpportunityId(), lead.getLeadId(), currentUserId);
        return toResponse(opp);
    }

    /** Business-02: direct create (not via Lead conversion) */
    @Transactional
    public OpportunityResponse createDirect(CreateOpportunityRequest request, Long currentUserId) {
        validateUniqueName(request.getOpportunityName(), null);
        Opportunity opp = Opportunity.builder()
                .opportunityId(generateOpportunityId())
                .opportunityName(request.getOpportunityName())
                .customer(request.getCustomer())
                .salesRepId(request.getSalesRepId())
                .stage(request.getStage() != null ? request.getStage() : "PROSPECTING")
                .expectedRevenue(request.getExpectedRevenue() != null ? request.getExpectedRevenue() : BigDecimal.ZERO)
                .description(request.getDescription())
                .lifecycleStatus("OPEN")
                .build();
        applyDetails(opp, request.getDetails());
        opportunityRepository.save(opp);
        log.info("Opportunity created directly: {} by userId={}", opp.getOpportunityId(), currentUserId);
        return toResponse(opp);
    }

    /**
     * List opportunities, paginated + searchable. {@code viewerSalesRepId} is the visibility
     * filter resolved by the controller from the caller's role: the caller's own userId for a
     * SALE rep (mine-only), or {@code null} for ADMIN/MANAGER (see all).
     */
    public PageResponse<OpportunityResponse> getMyOpportunities(Long viewerSalesRepId, String search, int page, int size) {
        String searchParam = (search != null && search.isBlank()) ? null : search;
        Page<Opportunity> result = opportunityRepository.findMine(viewerSalesRepId, searchParam, PageRequest.of(page, size));
        return PageResponse.of(result.map(this::toResponse));
    }

    /** View opportunity detail. {@code viewerSalesRepId} null (ADMIN/MANAGER) skips the ownership check. */
    public OpportunityResponse getOpportunity(Long id, Long viewerSalesRepId) {
        Opportunity opp = opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Opportunity not found: " + id));
        if (viewerSalesRepId != null && !opp.getSalesRepId().equals(viewerSalesRepId)) {
            throw new RuntimeException("You can only access your own opportunities.");
        }
        return toResponse(opp);
    }

    /**
     * Business-04: Edit Header + Detail lines. BR-01/BR-06: only allowed while the
     * Opportunity has not been converted to a Quotation yet. {@code privileged} (ADMIN/MANAGER)
     * bypasses the ownership check, same as list/view.
     */
    @Transactional
    public OpportunityResponse updateOpportunity(Long id, CreateOpportunityRequest request, Long currentUserId, boolean privileged) {
        Opportunity opp = findOwnedOpportunity(id, currentUserId, privileged);
        if (!"OPEN".equals(opp.getLifecycleStatus())) {
            throw new RuntimeException("This opportunity has already been converted to quotation.");
        }
        validateUniqueName(request.getOpportunityName(), id);
        opp.setOpportunityName(request.getOpportunityName());
        opp.setCustomer(request.getCustomer());
        opp.setSalesRepId(request.getSalesRepId());
        if (request.getStage() != null) opp.setStage(request.getStage());
        if (request.getExpectedRevenue() != null) opp.setExpectedRevenue(request.getExpectedRevenue());
        opp.setDescription(request.getDescription());
        applyDetails(opp, request.getDetails());
        opportunityRepository.save(opp);
        log.info("Opportunity updated: {} by userId={}", opp.getOpportunityId(), currentUserId);
        return toResponse(opp);
    }

    /**
     * Business-05 (implied by Popup Delete): Soft delete is not modeled on this entity yet — hard-remove test-only rows is out of scope; kept for future Quotation-lock parity.
     * {@code privileged} (ADMIN/MANAGER) bypasses the ownership check, same as list/view.
     */
    @Transactional
    public void deleteOpportunity(Long id, Long currentUserId, boolean privileged) {
        Opportunity opp = findOwnedOpportunity(id, currentUserId, privileged);
        if (!"OPEN".equals(opp.getLifecycleStatus())) {
            throw new RuntimeException("This opportunity has already been converted to quotation.");
        }
        opportunityRepository.delete(opp);
        log.info("Opportunity deleted: {} by userId={}", opp.getOpportunityId(), currentUserId);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────
    private void applyDetails(Opportunity opp, List<OpportunityDetailRequest> detailRequests) {
        opp.getDetails().clear();
        if (detailRequests == null) return;
        for (OpportunityDetailRequest d : detailRequests) {
            opp.getDetails().add(OpportunityDetail.builder()
                    .opportunity(opp)
                    .serviceProduct(d.getServiceProduct())
                    .quantity(d.getQuantity())
                    .unit(d.getUnit())
                    .remark(d.getRemark())
                    .build());
        }
    }

    private Opportunity findOwnedOpportunity(Long id, Long currentUserId, boolean privileged) {
        Opportunity opp = opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Opportunity not found: " + id));
        if (!privileged && !opp.getSalesRepId().equals(currentUserId)) {
            throw new RuntimeException("You can only access your own opportunities.");
        }
        return opp;
    }

    private String generateOpportunityId() {
        long count = opportunityRepository.count() + 1;
        return String.format("OPP-%04d", count);
    }

    /**
     * BUG_SALE-001 #11: a Lead can convert into many Opportunities, so a name collision is
     * easy to hit by accident (e.g. re-using the same Convert popup default) — block it.
     * {@code excludeId} is the Opportunity's own id on update (skip comparing against itself).
     */
    private void validateUniqueName(String opportunityName, Long excludeId) {
        boolean taken = excludeId == null
                ? opportunityRepository.existsByOpportunityNameIgnoreCase(opportunityName)
                : opportunityRepository.existsByOpportunityNameIgnoreCaseAndIdNot(opportunityName, excludeId);
        if (taken) {
            throw new RuntimeException("An Opportunity named \"" + opportunityName + "\" already exists. Please use a different name.");
        }
    }

    private OpportunityResponse toResponse(Opportunity opp) {
        String salesRepName = userRepository.findById(opp.getSalesRepId()).map(User::getFullName).orElse(null);
        String leadName = opp.getLeadId() != null
                ? leadRepository.findById(opp.getLeadId()).map(Lead::getLeadName).orElse(null)
                : null;
        List<OpportunityDetailResponse> details = opp.getDetails().stream()
                .map(d -> OpportunityDetailResponse.builder()
                        .id(d.getId())
                        .serviceProduct(d.getServiceProduct())
                        .quantity(d.getQuantity())
                        .unit(d.getUnit())
                        .remark(d.getRemark())
                        .build())
                .collect(Collectors.toList());
        return OpportunityResponse.builder()
                .id(opp.getId())
                .opportunityId(opp.getOpportunityId())
                .leadId(opp.getLeadId())
                .leadName(leadName)
                .opportunityName(opp.getOpportunityName())
                .customer(opp.getCustomer())
                .salesRepId(opp.getSalesRepId())
                .salesRepName(salesRepName)
                .stage(opp.getStage())
                .expectedRevenue(opp.getExpectedRevenue())
                .description(opp.getDescription())
                .lifecycleStatus(opp.getLifecycleStatus())
                .details(details)
                .hasQuotation(!"OPEN".equals(opp.getLifecycleStatus()))
                .createdAt(opp.getCreatedAt())
                .updatedAt(opp.getUpdatedAt())
                .build();
    }
}
