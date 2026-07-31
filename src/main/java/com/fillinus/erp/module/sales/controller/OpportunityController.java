package com.fillinus.erp.module.sales.controller;

import com.fillinus.erp.common.ApiResponse;
import com.fillinus.erp.common.PageResponse;
import com.fillinus.erp.module.auth.repository.UserRepository;
import com.fillinus.erp.module.sales.dto.*;
import com.fillinus.erp.module.sales.service.OpportunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Opportunity Controller — SAL-002
 * Salers can view/create/edit/delete their own opportunities.
 */
@RestController
@RequestMapping("/opportunities")
@RequiredArgsConstructor
@Tag(name = "Opportunities", description = "SAL-002 Opportunity Management")
@SecurityRequirement(name = "bearerAuth")
public class OpportunityController {

    private final OpportunityService opportunityService;
    private final UserRepository userRepository;

    /** Business-02: Create Opportunity directly (not via Lead conversion) */
    @Operation(summary = "Create opportunity", description = "Direct creation — not via Lead conversion. Requires at least the Header fields.")
    @PostMapping
    public ResponseEntity<ApiResponse<OpportunityResponse>> createOpportunity(
            @Valid @RequestBody CreateOpportunityRequest request,
            Authentication auth) {
        Long userId = resolveUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok("Opportunity created successfully.", opportunityService.createDirect(request, userId)));
    }

    /** Get opportunities — SALE reps see their own only, ADMIN/MANAGER see all */
    @Operation(summary = "Get opportunities", description = "SALE reps see opportunities assigned to them only; ADMIN/MANAGER see all. Paginated + searchable by Opportunity No/Name/Customer.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OpportunityResponse>>> getMyOpportunities(
            Authentication auth,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long viewerSalesRepId = isPrivilegedRole(auth) ? null : resolveUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok("Success", opportunityService.getMyOpportunities(viewerSalesRepId, search, page, size)));
    }

    /** View opportunity detail */
    @Operation(summary = "Get opportunity by ID", description = "SALE reps can only view their own; ADMIN/MANAGER can view any.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OpportunityResponse>> getOpportunity(@PathVariable Long id, Authentication auth) {
        Long viewerSalesRepId = isPrivilegedRole(auth) ? null : resolveUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok("Success", opportunityService.getOpportunity(id, viewerSalesRepId)));
    }

    /** Business-04: Edit Header + Detail lines — blocked once converted to Quotation */
    @Operation(summary = "Update opportunity", description = "SALE reps can only update their own opportunities; ADMIN/MANAGER can update any. Blocked once converted to a Quotation.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OpportunityResponse>> updateOpportunity(
            @PathVariable Long id,
            @Valid @RequestBody CreateOpportunityRequest request,
            Authentication auth) {
        Long userId = resolveUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok("Opportunity updated.", opportunityService.updateOpportunity(id, request, userId, isPrivilegedRole(auth))));
    }

    /** Delete opportunity — blocked once converted to Quotation */
    @Operation(summary = "Delete opportunity", description = "SALE reps can only delete their own opportunities; ADMIN/MANAGER can delete any. Blocked once converted to a Quotation.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOpportunity(@PathVariable Long id, Authentication auth) {
        Long userId = resolveUserId(auth);
        opportunityService.deleteOpportunity(id, userId, isPrivilegedRole(auth));
        return ResponseEntity.ok(ApiResponse.ok("Opportunity deleted.", null));
    }

    private Long resolveUserId(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    /** ADMIN/MANAGER see all opportunities; other roles (SALE etc.) see only their own. */
    private boolean isPrivilegedRole(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"));
    }
}
