package com.fillinus.erp.module.sales.controller;

import com.fillinus.erp.common.ApiResponse;
import com.fillinus.erp.common.PageResponse;
import com.fillinus.erp.module.auth.repository.UserRepository;
import com.fillinus.erp.module.sales.dto.*;
import com.fillinus.erp.module.sales.service.LeadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Lead Controller — SAL-001
 * All endpoints require authentication (SALE role via JWT).
 */
@RestController
@RequestMapping("/leads")
@RequiredArgsConstructor
@Tag(name = "Leads", description = "SAL-001 Lead Management")
@SecurityRequirement(name = "bearerAuth")
public class LeadController {

    private final LeadService leadService;
    private final UserRepository userRepository;

    /** BUSINESS-01: Search leads */
    @Operation(summary = "Search leads", description = "All roles see every Lead (View All); the Sales Rep param is an optional filter, not an ownership scope. Filter by search term, status and/or Sales Rep. Returns active non-converted leads, paginated.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<LeadResponse>>> getLeads(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long salesRepId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.ok("Success", leadService.getLeads(search, status, salesRepId, page, size)));
    }

    /** BUSINESS-03: View lead detail */
    @Operation(summary = "Get lead by ID", description = "View All — any authenticated user can view any Lead. Edit/Delete/Convert remain ownership-restricted.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeadResponse>> getLead(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Success", leadService.getLead(id, null)));
    }

    /** BUSINESS-02: Create lead manually */
    @Operation(summary = "Create lead", description = "Manually create a lead. Lead ID is auto-generated (BR-001). Default status = NEW.")
    @PostMapping
    public ResponseEntity<ApiResponse<LeadResponse>> createLead(
            @Valid @RequestBody CreateLeadRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("Lead created successfully.", leadService.createLead(request, resolveUserId(auth))));
    }

    /** BUSINESS-04: Edit lead */
    @Operation(summary = "Update lead", description = "SALE reps can only update their own; ADMIN/MANAGER can update any. Lead ID (leadId) is immutable (BR-001). Updated_by is logged (BR-006).")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LeadResponse>> updateLead(
            @PathVariable Long id,
            @Valid @RequestBody CreateLeadRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("Lead updated successfully.", leadService.updateLead(id, request, resolveUserId(auth), isPrivilegedRole(auth))));
    }

    /** BUSINESS-05: Soft delete lead (BR-005) */
    @Operation(summary = "Delete lead", description = "SALE reps can only delete their own; ADMIN/MANAGER can delete any. Soft-deletes a lead. Data kept for audit trail (BR-005, BR-006).")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLead(@PathVariable Long id, Authentication auth) {
        leadService.deleteLead(id, resolveUserId(auth), isPrivilegedRole(auth));
        return ResponseEntity.ok(ApiResponse.ok("Lead deleted.", null));
    }

    /** BUG_SALE-001 #4: bulk delete */
    @Operation(summary = "Bulk delete leads", description = "Soft-deletes multiple leads at once. Same ownership rule as single delete — SALE reps can only delete their own; ADMIN/MANAGER can delete any.")
    @PostMapping("/bulk-delete")
    public ResponseEntity<ApiResponse<Void>> bulkDeleteLeads(@RequestBody List<Long> ids, Authentication auth) {
        leadService.bulkDeleteLeads(ids, resolveUserId(auth), isPrivilegedRole(auth));
        return ResponseEntity.ok(ApiResponse.ok(ids.size() + " lead(s) deleted.", null));
    }

    /** BUSINESS-06: Convert lead to Opportunity (V1.1: collects Opportunity Name/Project Type/Expected Deal Value/Sales Rep) */
    @Operation(summary = "Convert lead to Opportunity",
               description = "SALE reps can only convert their own; ADMIN/MANAGER can convert any. Creates Opportunity assigned to the given Sales Rep. Marks lead QUALIFIED. Convert is allowed multiple times unless the lead is Rejected.")
    @PostMapping("/{id}/convert")
    public ResponseEntity<ApiResponse<OpportunityResponse>> convertLead(
            @PathVariable Long id,
            @Valid @RequestBody ConvertLeadRequest request,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("Lead converted to Opportunity.", leadService.convertLead(id, request, resolveUserId(auth), isPrivilegedRole(auth))));
    }

    /** Import leads from Excel */
    @Operation(summary = "Import leads from Excel (.xlsx)", description = "Columns: Lead Name | Company | Contact Person | Phone | Email")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<LeadResponse>>> importLeads(
            @RequestPart("file") MultipartFile file,
            Authentication auth) throws IOException {
        return ResponseEntity.ok(ApiResponse.ok("Imported successfully.", leadService.importFromExcel(file, resolveUserId(auth))));
    }

    /** Download blank Excel import template */
    @Operation(summary = "Download Excel import template")
    @GetMapping("/import/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        byte[] bytes = leadService.generateExcelTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=lead_import_template.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────
    private Long resolveUserId(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    /** ADMIN/MANAGER see/act on all leads; other roles (SALE etc.) are scoped to their own. */
    private boolean isPrivilegedRole(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"));
    }
}
