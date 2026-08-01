package com.fillinus.erp.module.sales.controller;

import com.fillinus.erp.common.ApiResponse;
import com.fillinus.erp.common.PageResponse;
import com.fillinus.erp.module.auth.repository.UserRepository;
import com.fillinus.erp.module.sales.dto.CreateDealResultRequest;
import com.fillinus.erp.module.sales.dto.DealResultResponse;
import com.fillinus.erp.module.sales.service.DealResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Deal Result Controller — SAL-005 (Deal Won/Lost).
 * BR-001: created FROM a Deal Negotiation via "Complete Negotiation" — no standalone create.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Deal Results", description = "SAL-005 Deal Won/Lost Management")
@SecurityRequirement(name = "bearerAuth")
public class DealResultController {

    private final DealResultService dealResultService;
    private final UserRepository userRepository;

    @Operation(summary = "Complete Negotiation (record Deal Won/Lost)", description = "SALE reps can only act on their own; ADMIN/MANAGER can act on any. BC-001/BC-006: one result per negotiation, only allowed while Status = Negotiating. Cascades to Quotation (Accepted/Rejected) and Opportunity (Closed Won/Closed Lost).")
    @PostMapping("/deal-negotiations/{id}/deal-result")
    public ResponseEntity<ApiResponse<DealResultResponse>> createDealResult(
            @PathVariable Long id,
            @Valid @RequestBody CreateDealResultRequest request,
            Authentication auth) {
        Long userId = resolveUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok("Deal Result recorded.", dealResultService.createFromNegotiation(id, request, userId, isPrivilegedRole(auth))));
    }

    @Operation(summary = "Get deal results", description = "SALE reps see their own only; ADMIN/MANAGER see all. Paginated + searchable by Negotiation No/Quotation No/Opportunity/Customer, filterable by Deal Result.")
    @GetMapping("/deal-results")
    public ResponseEntity<ApiResponse<PageResponse<DealResultResponse>>> getMyDealResults(
            Authentication auth,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String result,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long viewerSalesRepId = isPrivilegedRole(auth) ? null : resolveUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok("Success", dealResultService.getMyDealResults(viewerSalesRepId, search, result, page, size)));
    }

    @Operation(summary = "Get deal result by ID", description = "SALE reps can only view their own; ADMIN/MANAGER can view any. Read-only (BR-007).")
    @GetMapping("/deal-results/{id}")
    public ResponseEntity<ApiResponse<DealResultResponse>> getDealResult(@PathVariable Long id, Authentication auth) {
        Long viewerSalesRepId = isPrivilegedRole(auth) ? null : resolveUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok("Success", dealResultService.getDealResult(id, viewerSalesRepId)));
    }

    private Long resolveUserId(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    private boolean isPrivilegedRole(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"));
    }
}
