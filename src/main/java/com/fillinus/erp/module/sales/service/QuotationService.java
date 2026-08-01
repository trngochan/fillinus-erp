package com.fillinus.erp.module.sales.service;

import com.fillinus.erp.common.PageResponse;
import com.fillinus.erp.module.auth.entity.User;
import com.fillinus.erp.module.auth.repository.UserRepository;
import com.fillinus.erp.module.sales.dto.*;
import com.fillinus.erp.module.sales.entity.Opportunity;
import com.fillinus.erp.module.sales.entity.OpportunityDetail;
import com.fillinus.erp.module.sales.entity.Quotation;
import com.fillinus.erp.module.sales.entity.QuotationDetail;
import com.fillinus.erp.module.sales.repository.OpportunityRepository;
import com.fillinus.erp.module.sales.repository.QuotationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Quotation Service — SAL-003.
 * BR-001: Quotation can ONLY be created from an Opportunity (no standalone create).
 * "productService"/"unit"/"standardPrice" are temp manual fields — no Product Info
 * master data exists yet (SAL-009). One Opportunity produces at most one Quotation,
 * per SAL-002 BR-01 ("chỉ được chuyển thành Quotation một lần") — Opportunity flips
 * to lifecycleStatus=CONVERTED once its Quotation is created.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QuotationService {

    private final QuotationRepository quotationRepository;
    private final OpportunityRepository opportunityRepository;
    private final UserRepository userRepository;

    private static final List<String> EDITABLE_STATUSES = List.of("DRAFT");

    /**
     * BR-001/BR-002: create from Opportunity, inheriting Customer/Sales Rep and copying Detail
     * lines. {@code privileged} (ADMIN/MANAGER) bypasses the Opportunity ownership check.
     */
    @Transactional
    public QuotationResponse createFromOpportunity(Long opportunityId, CreateQuotationFromOpportunityRequest request, Long currentUserId, boolean privileged) {
        Opportunity opp = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new RuntimeException("Opportunity not found: " + opportunityId));
        if (!privileged && !opp.getSalesRepId().equals(currentUserId)) {
            throw new RuntimeException("You can only create a Quotation from your own Opportunity.");
        }
        if (!"OPEN".equals(opp.getLifecycleStatus())) {
            throw new RuntimeException("This opportunity has already been converted to quotation.");
        }
        if (opp.getDetails().isEmpty()) {
            throw new RuntimeException("Add at least one Service/Product detail to the Opportunity before creating a Quotation.");
        }

        Map<Long, QuotationDetailPriceInput> priceByDetailId = request.getDetailPrices().stream()
                .collect(Collectors.toMap(QuotationDetailPriceInput::getOpportunityDetailId, p -> p));

        Quotation quotation = Quotation.builder()
                .quotationNo(generateQuotationNo())
                .opportunityId(opp.getId())
                .customer(opp.getCustomer())
                .salesRepId(opp.getSalesRepId())
                .quotationDate(LocalDate.now())
                .expiredDate(request.getExpiredDate())
                .currency(request.getCurrency() != null ? request.getCurrency() : "VND")
                .status("DRAFT")
                .remark(request.getRemark())
                .createdBy(currentUserId)
                .updatedBy(currentUserId)
                .build();

        for (OpportunityDetail od : opp.getDetails()) {
            QuotationDetailPriceInput price = priceByDetailId.get(od.getId());
            if (price == null) {
                throw new RuntimeException("Missing Unit Price for detail line: " + od.getServiceProduct());
            }
            BigDecimal standardPrice = price.getStandardPrice() != null ? price.getStandardPrice() : BigDecimal.ZERO;
            BigDecimal unitPrice = price.getUnitPrice();
            quotation.getDetails().add(QuotationDetail.builder()
                    .quotation(quotation)
                    .productService(od.getServiceProduct())
                    .quantity(od.getQuantity())
                    .unit(od.getUnit())
                    .standardPrice(standardPrice)
                    .unitPrice(unitPrice)
                    .amount(od.getQuantity().multiply(unitPrice))
                    .remark(od.getRemark())
                    .build());
        }
        recalculateTotal(quotation);
        quotationRepository.save(quotation);

        opp.setLifecycleStatus("CONVERTED");
        opportunityRepository.save(opp);

        log.info("Quotation created: {} from Opportunity {} by userId={}", quotation.getQuotationNo(), opp.getOpportunityId(), currentUserId);
        return toResponse(quotation);
    }

    /**
     * List quotations, paginated + searchable. {@code viewerSalesRepId} is the visibility
     * filter resolved by the controller from the caller's role: the caller's own userId for a
     * SALE rep (mine-only), or {@code null} for ADMIN/MANAGER (see all).
     */
    public PageResponse<QuotationResponse> getMyQuotations(Long viewerSalesRepId, String search, String status, int page, int size) {
        String searchParam = (search != null && search.isBlank()) ? null : search;
        String statusParam = (status != null && (status.isBlank() || status.equalsIgnoreCase("ALL"))) ? null : status;
        Page<Quotation> result = quotationRepository.findMine(viewerSalesRepId, searchParam, statusParam, PageRequest.of(page, size));
        result.getContent().forEach(this::applyExpiryIfNeeded);
        return PageResponse.of(result.map(this::toResponse));
    }

    /** View quotation detail. {@code viewerSalesRepId} null (ADMIN/MANAGER) skips the ownership check. */
    public QuotationResponse getQuotation(Long id, Long viewerSalesRepId) {
        Quotation quotation = quotationRepository.findById(id)
                .filter(q -> !Boolean.TRUE.equals(q.getIsDeleted()))
                .orElseThrow(() -> new RuntimeException("Quotation not found: " + id));
        if (viewerSalesRepId != null && !quotation.getSalesRepId().equals(viewerSalesRepId)) {
            throw new RuntimeException("You can only access your own quotations.");
        }
        applyExpiryIfNeeded(quotation);
        return toResponse(quotation);
    }

    /** BR-006: only editable while Status = Draft. {@code privileged} (ADMIN/MANAGER) bypasses the ownership check. */
    @Transactional
    public QuotationResponse updateQuotation(Long id, UpdateQuotationRequest request, Long currentUserId, boolean privileged) {
        Quotation quotation = findOwnedQuotation(id, currentUserId, privileged);
        applyExpiryIfNeeded(quotation);
        if (!EDITABLE_STATUSES.contains(quotation.getStatus())) {
            throw new RuntimeException("Only a Draft quotation can be edited.");
        }
        quotation.setQuotationDate(request.getQuotationDate());
        quotation.setExpiredDate(request.getExpiredDate());
        if (request.getCurrency() != null) quotation.setCurrency(request.getCurrency());
        quotation.setRemark(request.getRemark());
        quotation.setUpdatedBy(currentUserId);

        quotation.getDetails().clear();
        if (request.getDetails() != null) {
            for (QuotationDetailRequest d : request.getDetails()) {
                BigDecimal amount = d.getQuantity().multiply(d.getUnitPrice());
                quotation.getDetails().add(QuotationDetail.builder()
                        .quotation(quotation)
                        .productService(d.getProductService())
                        .quantity(d.getQuantity())
                        .unit(d.getUnit())
                        .standardPrice(d.getStandardPrice() != null ? d.getStandardPrice() : BigDecimal.ZERO)
                        .unitPrice(d.getUnitPrice())
                        .amount(amount)
                        .remark(d.getRemark())
                        .build());
            }
        }
        recalculateTotal(quotation);
        quotationRepository.save(quotation);
        log.info("Quotation updated: {} by userId={}", quotation.getQuotationNo(), currentUserId);
        return toResponse(quotation);
    }

    /** BR-011: soft delete, only allowed while Draft. {@code privileged} (ADMIN/MANAGER) bypasses the ownership check. */
    @Transactional
    public void deleteQuotation(Long id, Long currentUserId, boolean privileged) {
        Quotation quotation = findOwnedQuotation(id, currentUserId, privileged);
        if (!EDITABLE_STATUSES.contains(quotation.getStatus())) {
            throw new RuntimeException("Only a Draft quotation can be deleted.");
        }
        quotation.setIsDeleted(true);
        quotation.setUpdatedBy(currentUserId);
        quotationRepository.save(quotation);
        log.info("Quotation soft-deleted: {} by userId={}", quotation.getQuotationNo(), currentUserId);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────
    /** BR-010: auto-expire Draft/Sent quotations once past their Expired Date. */
    private void applyExpiryIfNeeded(Quotation quotation) {
        if (("DRAFT".equals(quotation.getStatus()) || "SENT".equals(quotation.getStatus()))
                && quotation.getExpiredDate().isBefore(LocalDate.now())) {
            quotation.setStatus("EXPIRED");
            quotationRepository.save(quotation);
        }
    }

    private void recalculateTotal(Quotation quotation) {
        BigDecimal total = quotation.getDetails().stream()
                .map(QuotationDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        quotation.setTotalAmount(total);
    }

    private Quotation findOwnedQuotation(Long id, Long currentUserId, boolean privileged) {
        Quotation quotation = quotationRepository.findById(id)
                .filter(q -> !Boolean.TRUE.equals(q.getIsDeleted()))
                .orElseThrow(() -> new RuntimeException("Quotation not found: " + id));
        if (!privileged && !quotation.getSalesRepId().equals(currentUserId)) {
            throw new RuntimeException("You can only access your own quotations.");
        }
        return quotation;
    }

    private String generateQuotationNo() {
        long count = quotationRepository.count() + 1;
        return String.format("QUO-%04d", count);
    }

    private QuotationResponse toResponse(Quotation quotation) {
        String salesRepName = userRepository.findById(quotation.getSalesRepId()).map(User::getFullName).orElse(null);
        String opportunityName = opportunityRepository.findById(quotation.getOpportunityId())
                .map(Opportunity::getOpportunityName).orElse(null);
        List<QuotationDetailResponse> details = quotation.getDetails().stream()
                .map(d -> QuotationDetailResponse.builder()
                        .id(d.getId())
                        .productService(d.getProductService())
                        .quantity(d.getQuantity())
                        .unit(d.getUnit())
                        .standardPrice(d.getStandardPrice())
                        .unitPrice(d.getUnitPrice())
                        .amount(d.getAmount())
                        .remark(d.getRemark())
                        .build())
                .collect(Collectors.toList());
        return QuotationResponse.builder()
                .id(quotation.getId())
                .quotationNo(quotation.getQuotationNo())
                .opportunityId(quotation.getOpportunityId())
                .opportunityName(opportunityName)
                .customer(quotation.getCustomer())
                .salesRepId(quotation.getSalesRepId())
                .salesRepName(salesRepName)
                .quotationDate(quotation.getQuotationDate())
                .expiredDate(quotation.getExpiredDate())
                .currency(quotation.getCurrency())
                .status(quotation.getStatus())
                .remark(quotation.getRemark())
                .totalAmount(quotation.getTotalAmount())
                .details(details)
                .createdAt(quotation.getCreatedAt())
                .updatedAt(quotation.getUpdatedAt())
                .build();
    }
}
