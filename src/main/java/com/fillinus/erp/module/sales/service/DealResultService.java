package com.fillinus.erp.module.sales.service;

import com.fillinus.erp.common.PageResponse;
import com.fillinus.erp.module.auth.entity.User;
import com.fillinus.erp.module.auth.repository.UserRepository;
import com.fillinus.erp.module.sales.dto.CreateDealResultRequest;
import com.fillinus.erp.module.sales.dto.DealResultResponse;
import com.fillinus.erp.module.sales.entity.DealNegotiation;
import com.fillinus.erp.module.sales.entity.DealResult;
import com.fillinus.erp.module.sales.entity.Opportunity;
import com.fillinus.erp.module.sales.entity.Quotation;
import com.fillinus.erp.module.sales.repository.DealNegotiationRepository;
import com.fillinus.erp.module.sales.repository.DealResultRepository;
import com.fillinus.erp.module.sales.repository.OpportunityRepository;
import com.fillinus.erp.module.sales.repository.QuotationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Deal Result Service — SAL-005.
 * BR-001: only ever created from a Deal Negotiation via "Complete Negotiation". BC-001/BC-006:
 * one result per negotiation, immutable once saved — no update/delete (Flow 3.4: "Không xóa ở
 * phase 1"). Client auto-creation (BR-004) and Project creation (PAC-001) are out of scope —
 * no Customer/Client master (SAL-007) or Project module exists yet in this codebase.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DealResultService {

    private final DealResultRepository dealResultRepository;
    private final DealNegotiationRepository dealNegotiationRepository;
    private final QuotationRepository quotationRepository;
    private final OpportunityRepository opportunityRepository;
    private final UserRepository userRepository;

    /**
     * BR-004/BR-005: records the final Won/Lost decision and cascades the status updates to
     * Quotation (Accepted/Rejected) and Opportunity (Closed Won/Closed Lost).
     * {@code privileged} (ADMIN/MANAGER) bypasses the Deal Negotiation ownership check.
     */
    @Transactional
    public DealResultResponse createFromNegotiation(Long dealNegotiationId, CreateDealResultRequest request, Long currentUserId, boolean privileged) {
        DealNegotiation negotiation = dealNegotiationRepository.findById(dealNegotiationId)
                .filter(n -> !Boolean.TRUE.equals(n.getIsDeleted()))
                .orElseThrow(() -> new RuntimeException("Deal Negotiation not found: " + dealNegotiationId));
        if (!privileged && !negotiation.getSalesRepId().equals(currentUserId)) {
            throw new RuntimeException("You can only access your own deals.");
        }
        if (!"NEGOTIATING".equals(negotiation.getStatus())) {
            throw new RuntimeException("This deal has already been completed.");
        }
        if (dealResultRepository.findByDealNegotiationIdAndIsDeletedFalse(dealNegotiationId).isPresent()) {
            throw new RuntimeException("A Deal Result has already been recorded for this negotiation.");
        }

        Quotation quotation = quotationRepository.findById(negotiation.getQuotationId())
                .orElseThrow(() -> new RuntimeException("Quotation not found: " + negotiation.getQuotationId()));
        Opportunity opportunity = opportunityRepository.findById(negotiation.getOpportunityId())
                .orElseThrow(() -> new RuntimeException("Opportunity not found: " + negotiation.getOpportunityId()));

        boolean won = "WON".equals(request.getResult());

        DealResult dealResult = DealResult.builder()
                .dealNegotiationId(negotiation.getId())
                .quotationId(quotation.getId())
                .opportunityId(opportunity.getId())
                .customer(negotiation.getCustomer())
                .salesRepId(negotiation.getSalesRepId())
                .dealAmount(quotation.getTotalAmount())
                .result(request.getResult())
                .comment(request.getComment())
                .decisionBy(currentUserId)
                .build();
        dealResultRepository.save(dealResult);

        negotiation.setStatus(won ? "WON" : "LOST");
        negotiation.setUpdatedBy(currentUserId);
        dealNegotiationRepository.save(negotiation);

        quotation.setStatus(won ? "ACCEPTED" : "REJECTED");
        quotation.setUpdatedBy(currentUserId);
        quotationRepository.save(quotation);

        opportunity.setStage(won ? "CLOSED_WON" : "CLOSED_LOST");
        opportunityRepository.save(opportunity);

        log.info("Deal Result recorded: negotiation={} result={} by userId={}", negotiation.getNegotiationNo(), request.getResult(), currentUserId);
        return toResponse(dealResult, negotiation, quotation, opportunity);
    }

    /** List, paginated + searchable/filterable by Deal Result. {@code viewerSalesRepId} null (ADMIN/MANAGER) sees all. */
    public PageResponse<DealResultResponse> getMyDealResults(Long viewerSalesRepId, String search, String result, int page, int size) {
        String searchParam = (search != null && search.isBlank()) ? null : search;
        String resultParam = (result != null && (result.isBlank() || result.equalsIgnoreCase("ALL"))) ? null : result;
        Page<DealResult> results = dealResultRepository.findMine(viewerSalesRepId, searchParam, resultParam, PageRequest.of(page, size));
        return PageResponse.of(results.map(this::toResponse));
    }

    /** View detail. {@code viewerSalesRepId} null (ADMIN/MANAGER) skips the ownership check. */
    public DealResultResponse getDealResult(Long id, Long viewerSalesRepId) {
        DealResult dealResult = dealResultRepository.findById(id)
                .filter(r -> !Boolean.TRUE.equals(r.getIsDeleted()))
                .orElseThrow(() -> new RuntimeException("Deal Result not found: " + id));
        if (viewerSalesRepId != null && !dealResult.getSalesRepId().equals(viewerSalesRepId)) {
            throw new RuntimeException("You can only access your own deals.");
        }
        return toResponse(dealResult);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────
    private DealResultResponse toResponse(DealResult dealResult) {
        DealNegotiation negotiation = dealNegotiationRepository.findById(dealResult.getDealNegotiationId()).orElse(null);
        Quotation quotation = quotationRepository.findById(dealResult.getQuotationId()).orElse(null);
        Opportunity opportunity = opportunityRepository.findById(dealResult.getOpportunityId()).orElse(null);
        return toResponse(dealResult, negotiation, quotation, opportunity);
    }

    private DealResultResponse toResponse(DealResult dealResult, DealNegotiation negotiation, Quotation quotation, Opportunity opportunity) {
        String salesRepName = userRepository.findById(dealResult.getSalesRepId()).map(User::getFullName).orElse(null);
        String decisionByName = dealResult.getDecisionBy() != null
                ? userRepository.findById(dealResult.getDecisionBy()).map(User::getFullName).orElse(null) : null;
        return DealResultResponse.builder()
                .id(dealResult.getId())
                .dealNegotiationId(dealResult.getDealNegotiationId())
                .negotiationNo(negotiation != null ? negotiation.getNegotiationNo() : null)
                .quotationId(dealResult.getQuotationId())
                .quotationNo(quotation != null ? quotation.getQuotationNo() : null)
                .opportunityId(dealResult.getOpportunityId())
                .opportunityName(opportunity != null ? opportunity.getOpportunityName() : null)
                .customer(dealResult.getCustomer())
                .salesRepId(dealResult.getSalesRepId())
                .salesRepName(salesRepName)
                .dealAmount(dealResult.getDealAmount())
                .result(dealResult.getResult())
                .comment(dealResult.getComment())
                .decisionByName(decisionByName)
                .decisionDate(dealResult.getDecisionDate())
                .createdAt(dealResult.getCreatedAt())
                .build();
    }
}
