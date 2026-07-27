package com.fillinus.erp.module.sales.controller;

import com.fillinus.erp.common.ApiResponse;
import com.fillinus.erp.module.auth.repository.UserRepository;
import com.fillinus.erp.module.sales.dto.*;
import com.fillinus.erp.module.sales.service.LeadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Opportunity Controller — SAL-002
 * Salers can view and update their own opportunities.
 */
@RestController
@RequestMapping("/opportunities")
@RequiredArgsConstructor
@Tag(name = "Opportunities", description = "SAL-002 Opportunity Management")
@SecurityRequirement(name = "bearerAuth")
public class OpportunityController {

    private final LeadService leadService;
    private final UserRepository userRepository;

    /** Get MY opportunities (assigned to me) */
    @Operation(summary = "Get my opportunities", description = "Returns opportunities assigned to the current logged-in saler only.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<OpportunityResponse>>> getMyOpportunities(Authentication auth) {
        Long userId = resolveUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok("Success", leadService.getMyOpportunities(userId)));
    }

    /** Update opportunity status */
    @Operation(summary = "Update opportunity status", description = "Update status: NEW / IN_PROGRESS / WON / LOST. Can only update your own opportunities.")
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OpportunityResponse>> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateOpportunityRequest request,
            Authentication auth) {
        Long userId = resolveUserId(auth);
        return ResponseEntity.ok(ApiResponse.ok("Status updated.", leadService.updateOpportunityStatus(id, request, userId)));
    }

    private Long resolveUserId(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}
