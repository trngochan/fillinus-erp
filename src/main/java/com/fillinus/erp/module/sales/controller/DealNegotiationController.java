package com.fillinus.erp.module.sales.controller;

import com.fillinus.erp.common.ApiResponse;
import com.fillinus.erp.common.PageResponse;
import com.fillinus.erp.module.auth.repository.UserRepository;
import com.fillinus.erp.module.sales.dto.*;
import com.fillinus.erp.module.sales.service.DealNegotiationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Deal Negotiation Controller — SAL-004.
 * BR-001: no standalone create endpoint here — Deal Negotiation is only ever created from a
 * Quotation via {@link QuotationController#createDealNegotiation}.
 */
@RestController
@RequestMapping("/deal-negotiations")
@RequiredArgsConstructor
@Tag(name = "Deal Negotiations", description = "SAL-004 Deal Negotiation Management")
@SecurityRequirement(name = "bearerAuth")
public class DealNegotiationController {

    private final DealNegotiationService dealNegotiationService;
    private final UserRepository userRepository;

    @Operation(summary = "Get deal negotiations", description = "SALE reps see their own only; ADMIN/MANAGER see all. Paginated + searchable by Negotiation No/Quotation No/Customer, filterable by Meeting Date range.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DealNegotiationResponse>>> getMyDealNegotiations(
            Authentication auth,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate meetingDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate meetingDateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long viewerSalesRepId = isPrivilegedRole(auth) ? null : resolveUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok("Success", dealNegotiationService.getMyDealNegotiations(viewerSalesRepId, search, meetingDateFrom, meetingDateTo, page, size)));
    }

    @Operation(summary = "Get deal negotiation by ID", description = "SALE reps can only view their own; ADMIN/MANAGER can view any. Includes the full Negotiation Timeline.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DealNegotiationResponse>> getDealNegotiation(@PathVariable Long id, Authentication auth) {
        Long viewerSalesRepId = isPrivilegedRole(auth) ? null : resolveUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok("Success", dealNegotiationService.getDealNegotiation(id, viewerSalesRepId)));
    }

    @Operation(summary = "Update deal negotiation header", description = "SALE reps can only update their own; ADMIN/MANAGER can update any. BC-006: only allowed while Status = Negotiating.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DealNegotiationResponse>> updateDealNegotiation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDealNegotiationRequest request,
            Authentication auth) {
        Long userId = resolveUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok("Deal Negotiation updated.", dealNegotiationService.updateDealNegotiation(id, request, userId, isPrivilegedRole(auth))));
    }

    @Operation(summary = "Add Negotiation History", description = "BR-003: appends a new, immutable timeline entry. Only allowed while Status = Negotiating.")
    @PostMapping("/{id}/history")
    public ResponseEntity<ApiResponse<DealNegotiationResponse>> addHistory(
            @PathVariable Long id,
            @Valid @RequestBody AddNegotiationHistoryRequest request,
            Authentication auth) {
        Long userId = resolveUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok("Negotiation History added.", dealNegotiationService.addHistory(id, request, userId, isPrivilegedRole(auth))));
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
