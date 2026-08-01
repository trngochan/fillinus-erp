package com.fillinus.erp.module.sales.controller;

import com.fillinus.erp.common.ApiResponse;
import com.fillinus.erp.common.PageResponse;
import com.fillinus.erp.module.auth.repository.UserRepository;
import com.fillinus.erp.module.sales.dto.*;
import com.fillinus.erp.module.sales.service.DealNegotiationService;
import com.fillinus.erp.module.sales.service.QuotationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Quotation Controller — SAL-003.
 * BR-001: Quotation has no standalone "create" endpoint — it is only ever created
 * from an Opportunity via POST /opportunities/{id}/quotations.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Quotations", description = "SAL-003 Quotation Management")
@SecurityRequirement(name = "bearerAuth")
public class QuotationController {

    private final QuotationService quotationService;
    private final DealNegotiationService dealNegotiationService;
    private final UserRepository userRepository;

    @Operation(summary = "Create quotation from opportunity", description = "SALE reps can only quote their own Opportunity; ADMIN/MANAGER can quote any. BR-001: only allowed while the Opportunity is Open and not yet converted.")
    @PostMapping("/opportunities/{opportunityId}/quotations")
    public ResponseEntity<ApiResponse<QuotationResponse>> createFromOpportunity(
            @PathVariable Long opportunityId,
            @Valid @RequestBody CreateQuotationFromOpportunityRequest request,
            Authentication auth) {
        Long userId = resolveUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok("Quotation created successfully.", quotationService.createFromOpportunity(opportunityId, request, userId, isPrivilegedRole(auth))));
    }

    @Operation(summary = "Get quotations", description = "SALE reps see quotations assigned to them only; ADMIN/MANAGER see all. Paginated + searchable by Quotation No/Customer, filterable by Status.")
    @GetMapping("/quotations")
    public ResponseEntity<ApiResponse<PageResponse<QuotationResponse>>> getMyQuotations(
            Authentication auth,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long viewerSalesRepId = isPrivilegedRole(auth) ? null : resolveUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok("Success", quotationService.getMyQuotations(viewerSalesRepId, search, status, page, size)));
    }

    @Operation(summary = "Get quotation by ID", description = "SALE reps can only view their own; ADMIN/MANAGER can view any.")
    @GetMapping("/quotations/{id}")
    public ResponseEntity<ApiResponse<QuotationResponse>> getQuotation(@PathVariable Long id, Authentication auth) {
        Long viewerSalesRepId = isPrivilegedRole(auth) ? null : resolveUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok("Success", quotationService.getQuotation(id, viewerSalesRepId)));
    }

    @Operation(summary = "Update quotation", description = "SALE reps can only update their own; ADMIN/MANAGER can update any. BR-006: only allowed while Status = Draft.")
    @PutMapping("/quotations/{id}")
    public ResponseEntity<ApiResponse<QuotationResponse>> updateQuotation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateQuotationRequest request,
            Authentication auth) {
        Long userId = resolveUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok("Quotation updated.", quotationService.updateQuotation(id, request, userId, isPrivilegedRole(auth))));
    }

    @Operation(summary = "Delete quotation", description = "SALE reps can only delete their own; ADMIN/MANAGER can delete any. BR-011: soft delete, only allowed while Status = Draft.")
    @DeleteMapping("/quotations/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteQuotation(@PathVariable Long id, Authentication auth) {
        Long userId = resolveUserId(auth);
        quotationService.deleteQuotation(id, userId, isPrivilegedRole(auth));
        return ResponseEntity.ok(ApiResponse.ok("Quotation deleted.", null));
    }

    @Operation(summary = "Create Deal Negotiation", description = "SALE reps can only act on their own; ADMIN/MANAGER can act on any. BR-001 (SAL-004): only allowed while Status = Draft. Creates the Deal Negotiation + its first Negotiation History entry, and sets Quotation Status = Sent.")
    @PostMapping("/quotations/{id}/deal-negotiation")
    public ResponseEntity<ApiResponse<DealNegotiationResponse>> createDealNegotiation(
            @PathVariable Long id,
            @Valid @RequestBody CreateDealNegotiationRequest request,
            Authentication auth) {
        Long userId = resolveUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok("Deal Negotiation created.", dealNegotiationService.createFromQuotation(id, request, userId, isPrivilegedRole(auth))));
    }

    private Long resolveUserId(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    /** ADMIN/MANAGER see all quotations; other roles (SALE etc.) see only their own. */
    private boolean isPrivilegedRole(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"));
    }
}
