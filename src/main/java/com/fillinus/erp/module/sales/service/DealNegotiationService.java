package com.fillinus.erp.module.sales.service;

import com.fillinus.erp.common.PageResponse;
import com.fillinus.erp.module.auth.entity.User;
import com.fillinus.erp.module.auth.repository.UserRepository;
import com.fillinus.erp.module.sales.dto.*;
import com.fillinus.erp.module.sales.entity.DealNegotiation;
import com.fillinus.erp.module.sales.entity.DealNegotiationHistory;
import com.fillinus.erp.module.sales.entity.Opportunity;
import com.fillinus.erp.module.sales.entity.Quotation;
import com.fillinus.erp.module.sales.repository.DealNegotiationRepository;
import com.fillinus.erp.module.sales.repository.OpportunityRepository;
import com.fillinus.erp.module.sales.repository.QuotationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Deal Negotiation Service — SAL-004.
 * BR-001: only ever created from a Quotation (no standalone create). Editing the header and
 * appending history is only allowed while Status = Negotiating (locked once a Deal Result —
 * SAL-005 — is recorded).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DealNegotiationService {

    private final DealNegotiationRepository dealNegotiationRepository;
    private final QuotationRepository quotationRepository;
    private final OpportunityRepository opportunityRepository;
    private final UserRepository userRepository;

    /**
     * BR-001: creates the Deal Negotiation + its first Negotiation History entry, and flips
     * the Quotation Status Draft -> Sent. {@code privileged} (ADMIN/MANAGER) bypasses the
     * Quotation ownership check.
     */
    @Transactional
    public DealNegotiationResponse createFromQuotation(Long quotationId, CreateDealNegotiationRequest request, Long currentUserId, boolean privileged) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .filter(q -> !Boolean.TRUE.equals(q.getIsDeleted()))
                .orElseThrow(() -> new RuntimeException("Quotation not found: " + quotationId));
        if (!privileged && !quotation.getSalesRepId().equals(currentUserId)) {
            throw new RuntimeException("You can only access your own quotations.");
        }
        if (!"DRAFT".equals(quotation.getStatus())) {
            throw new RuntimeException("Deal Negotiation can only be created from a Draft quotation.");
        }
        if (request.getNextFollowUpDate() != null && request.getNextFollowUpDate().isBefore(request.getMeetingDate())) {
            throw new RuntimeException("Next Follow-up Date must be greater than or equal to Meeting Date.");
        }

        DealNegotiation negotiation = DealNegotiation.builder()
                .negotiationNo(generateNegotiationNo())
                .quotationId(quotation.getId())
                .opportunityId(quotation.getOpportunityId())
                .customer(quotation.getCustomer())
                .salesRepId(quotation.getSalesRepId())
                .meetingDate(request.getMeetingDate())
                .communicationChannel(request.getCommunicationChannel())
                .contactPerson(request.getContactPerson())
                .internalNote(request.getInternalNote())
                .status("NEGOTIATING")
                .createdBy(currentUserId)
                .updatedBy(currentUserId)
                .build();
        negotiation.getHistory().add(DealNegotiationHistory.builder()
                .dealNegotiation(negotiation)
                .updatedBy(currentUserId)
                .discussion(request.getDiscussion())
                .customerFeedback(request.getCustomerFeedback())
                .nextAction(request.getNextAction())
                .nextFollowUpDate(request.getNextFollowUpDate())
                .build());
        dealNegotiationRepository.save(negotiation);

        quotation.setStatus("SENT");
        quotation.setUpdatedBy(currentUserId);
        quotationRepository.save(quotation);

        log.info("Deal Negotiation created: {} from Quotation {} by userId={}", negotiation.getNegotiationNo(), quotation.getQuotationNo(), currentUserId);
        return toResponse(negotiation);
    }

    /** List, paginated + searchable. {@code viewerSalesRepId} null (ADMIN/MANAGER) sees all. */
    public PageResponse<DealNegotiationResponse> getMyDealNegotiations(Long viewerSalesRepId, String search, LocalDate meetingDateFrom, LocalDate meetingDateTo, int page, int size) {
        String searchParam = (search != null && search.isBlank()) ? null : search;
        Page<DealNegotiation> result = dealNegotiationRepository.findMine(viewerSalesRepId, searchParam, meetingDateFrom, meetingDateTo, PageRequest.of(page, size));
        return PageResponse.of(result.map(this::toResponse));
    }

    /** View detail (incl. full Negotiation Timeline). {@code viewerSalesRepId} null skips the ownership check. */
    public DealNegotiationResponse getDealNegotiation(Long id, Long viewerSalesRepId) {
        DealNegotiation negotiation = findOwnedNegotiation(id, viewerSalesRepId, viewerSalesRepId == null);
        return toResponse(negotiation);
    }

    /** BR-002: edit header fields. Locked once Status != Negotiating (BC-006). */
    @Transactional
    public DealNegotiationResponse updateDealNegotiation(Long id, UpdateDealNegotiationRequest request, Long currentUserId, boolean privileged) {
        DealNegotiation negotiation = findOwnedNegotiation(id, currentUserId, privileged);
        if (!"NEGOTIATING".equals(negotiation.getStatus())) {
            throw new RuntimeException("This deal has already been completed and can no longer be edited.");
        }
        negotiation.setMeetingDate(request.getMeetingDate());
        negotiation.setCommunicationChannel(request.getCommunicationChannel());
        negotiation.setContactPerson(request.getContactPerson());
        negotiation.setInternalNote(request.getInternalNote());
        negotiation.setUpdatedBy(currentUserId);
        dealNegotiationRepository.save(negotiation);
        log.info("Deal Negotiation updated: {} by userId={}", negotiation.getNegotiationNo(), currentUserId);
        return toResponse(negotiation);
    }

    /** BR-003: append a new, immutable Negotiation History entry. Locked once Status != Negotiating. */
    @Transactional
    public DealNegotiationResponse addHistory(Long id, AddNegotiationHistoryRequest request, Long currentUserId, boolean privileged) {
        DealNegotiation negotiation = findOwnedNegotiation(id, currentUserId, privileged);
        if (!"NEGOTIATING".equals(negotiation.getStatus())) {
            throw new RuntimeException("This deal has already been completed and can no longer be edited.");
        }
        if (request.getNextFollowUpDate() != null && request.getNextFollowUpDate().isBefore(negotiation.getMeetingDate())) {
            throw new RuntimeException("Next Follow-up Date must be greater than or equal to Meeting Date.");
        }
        negotiation.getHistory().add(DealNegotiationHistory.builder()
                .dealNegotiation(negotiation)
                .updatedBy(currentUserId)
                .discussion(request.getDiscussion())
                .customerFeedback(request.getCustomerFeedback())
                .nextAction(request.getNextAction())
                .nextFollowUpDate(request.getNextFollowUpDate())
                .build());
        negotiation.setUpdatedBy(currentUserId);
        dealNegotiationRepository.save(negotiation);
        log.info("Negotiation History added to {} by userId={}", negotiation.getNegotiationNo(), currentUserId);
        return toResponse(negotiation);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────
    DealNegotiation findOwnedNegotiation(Long id, Long currentUserId, boolean privileged) {
        DealNegotiation negotiation = dealNegotiationRepository.findById(id)
                .filter(n -> !Boolean.TRUE.equals(n.getIsDeleted()))
                .orElseThrow(() -> new RuntimeException("Deal Negotiation not found: " + id));
        if (!privileged && !negotiation.getSalesRepId().equals(currentUserId)) {
            throw new RuntimeException("You can only access your own deals.");
        }
        return negotiation;
    }

    private String generateNegotiationNo() {
        long count = dealNegotiationRepository.count() + 1;
        return String.format("NEG-%04d", count);
    }

    DealNegotiationResponse toResponse(DealNegotiation negotiation) {
        String salesRepName = userRepository.findById(negotiation.getSalesRepId()).map(User::getFullName).orElse(null);
        Quotation quotation = quotationRepository.findById(negotiation.getQuotationId()).orElse(null);
        String opportunityName = opportunityRepository.findById(negotiation.getOpportunityId())
                .map(Opportunity::getOpportunityName).orElse(null);
        List<DealNegotiationHistoryResponse> history = negotiation.getHistory().stream()
                .sorted(Comparator.comparing(DealNegotiationHistory::getDiscussionDate).reversed())
                .map(h -> DealNegotiationHistoryResponse.builder()
                        .id(h.getId())
                        .discussionDate(h.getDiscussionDate())
                        .updatedByName(userRepository.findById(h.getUpdatedBy()).map(User::getFullName).orElse(null))
                        .discussion(h.getDiscussion())
                        .customerFeedback(h.getCustomerFeedback())
                        .nextAction(h.getNextAction())
                        .nextFollowUpDate(h.getNextFollowUpDate())
                        .build())
                .collect(Collectors.toList());
        return DealNegotiationResponse.builder()
                .id(negotiation.getId())
                .negotiationNo(negotiation.getNegotiationNo())
                .quotationId(negotiation.getQuotationId())
                .quotationNo(quotation != null ? quotation.getQuotationNo() : null)
                .opportunityId(negotiation.getOpportunityId())
                .opportunityName(opportunityName)
                .customer(negotiation.getCustomer())
                .salesRepId(negotiation.getSalesRepId())
                .salesRepName(salesRepName)
                .meetingDate(negotiation.getMeetingDate())
                .communicationChannel(negotiation.getCommunicationChannel())
                .contactPerson(negotiation.getContactPerson())
                .internalNote(negotiation.getInternalNote())
                .status(negotiation.getStatus())
                .dealAmount(quotation != null ? quotation.getTotalAmount() : null)
                .history(history)
                .createdAt(negotiation.getCreatedAt())
                .updatedAt(negotiation.getUpdatedAt())
                .build();
    }
}
