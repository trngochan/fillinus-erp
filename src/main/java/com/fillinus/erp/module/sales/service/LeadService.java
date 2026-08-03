package com.fillinus.erp.module.sales.service;

import com.fillinus.erp.common.PageResponse;
import com.fillinus.erp.module.auth.entity.User;
import com.fillinus.erp.module.auth.repository.UserRepository;
import com.fillinus.erp.module.sales.dto.*;
import com.fillinus.erp.module.sales.entity.Lead;
import com.fillinus.erp.module.sales.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadService {

    private final LeadRepository leadRepository;
    private final OpportunityService opportunityService;
    private final UserRepository userRepository;

    // ─── BUSINESS-01: Search Leads ────────────────────────────────────────────
    public PageResponse<LeadResponse> getLeads(String search, String status, Long salesRepId, int page, int size) {
        String searchParam = (search != null && search.isBlank()) ? null : search;
        String statusParam = (status != null && (status.isBlank() || status.equalsIgnoreCase("ALL"))) ? null : status;
        Page<Lead> result = leadRepository.findAllActive(searchParam, statusParam, salesRepId, PageRequest.of(page, size));
        return PageResponse.of(result.map(this::toResponse));
    }

    // ─── BUSINESS-02: Create Lead ─────────────────────────────────────────────
    @Transactional
    public LeadResponse createLead(CreateLeadRequest request, Long currentUserId) {
        validateUniquePhoneEmail(request.getPhone(), request.getEmail(), null);
        String leadId = generateLeadId();
        Lead lead = Lead.builder()
                .leadId(leadId)
                .leadName(request.getLeadName())
                .companyName(request.getCompanyName())
                .contactPerson(request.getContactPerson())
                .phone(request.getPhone())
                .email(request.getEmail())
                .source(request.getSource())
                .salesRepId(request.getSalesRepId())
                .remark(request.getRemark())
                .status("NEW")
                .isDeleted(false)
                .createdBy(currentUserId)
                .updatedBy(currentUserId)
                .build();
        leadRepository.save(lead);
        log.info("Lead created: {} by userId={}", leadId, currentUserId);
        return toResponse(lead);
    }

    // ─── BUSINESS-03: View Lead ───────────────────────────────────────────────
    /** {@code viewerSalesRepId} null (ADMIN/MANAGER) skips the ownership check. */
    public LeadResponse getLead(Long id, Long viewerSalesRepId) {
        Lead lead = findActiveLead(id);
        if (viewerSalesRepId != null && !viewerSalesRepId.equals(lead.getSalesRepId())) {
            throw new RuntimeException("You can only access your own leads.");
        }
        return toResponse(lead);
    }

    // ─── BUSINESS-04: Edit Lead ───────────────────────────────────────────────
    private static final List<String> USER_EDITABLE_STATUSES = List.of("NEW", "IN_PROGRESS", "REJECTED");

    /** {@code privileged} (ADMIN/MANAGER) bypasses the ownership check. */
    @Transactional
    public LeadResponse updateLead(Long id, CreateLeadRequest request, Long currentUserId, boolean privileged) {
        Lead lead = findOwnedLead(id, currentUserId, privileged);
        validateUniquePhoneEmail(request.getPhone(), request.getEmail(), id);
        // Lead ID (leadId) is immutable per BR-001
        lead.setLeadName(request.getLeadName());
        lead.setCompanyName(request.getCompanyName());
        lead.setContactPerson(request.getContactPerson());
        lead.setPhone(request.getPhone());
        lead.setEmail(request.getEmail());
        lead.setSource(request.getSource());
        lead.setSalesRepId(request.getSalesRepId());
        lead.setRemark(request.getRemark());
        if (request.getStatus() != null) {
            if (!USER_EDITABLE_STATUSES.contains(request.getStatus())) {
                throw new RuntimeException("Status must be one of: New, In Progress, Rejected. Qualified is set automatically by Convert.");
            }
            lead.setStatus(request.getStatus());
        }
        lead.setUpdatedBy(currentUserId);
        leadRepository.save(lead);
        log.info("Lead updated: {} by userId={}", lead.getLeadId(), currentUserId);
        return toResponse(lead);
    }

    // ─── BUSINESS-05: Soft Delete Lead ────────────────────────────────────────
    /** {@code privileged} (ADMIN/MANAGER) bypasses the ownership check. */
    @Transactional
    public void deleteLead(Long id, Long currentUserId, boolean privileged) {
        Lead lead = findOwnedLead(id, currentUserId, privileged);
        lead.setIsDeleted(true);  // BR-005: soft delete only
        lead.setUpdatedBy(currentUserId);
        leadRepository.save(lead);
        log.info("Lead soft-deleted: {} by userId={}", lead.getLeadId(), currentUserId);
    }

    // ─── BUSINESS-06: Convert Lead to Opportunity ────────────────────────────
    /** {@code privileged} (ADMIN/MANAGER) bypasses the ownership check. */
    @Transactional
    public OpportunityResponse convertLead(Long id, ConvertLeadRequest request, Long currentUserId, boolean privileged) {
        Lead lead = findOwnedLead(id, currentUserId, privileged);

        // V1.1: Rejected leads can never be converted; otherwise convert is allowed any
        // number of times — one Lead can produce many Opportunities (BR-004).
        if ("REJECTED".equals(lead.getStatus())) {
            throw new RuntimeException("Lead " + lead.getLeadId() + " is Rejected and cannot be converted.");
        }

        OpportunityResponse opp = opportunityService.createFromLead(lead, currentUserId, request);

        // V1.1: Convert -> Qualified (lead stays visible/re-convertible, does not disappear)
        lead.setStatus("QUALIFIED");
        lead.setUpdatedBy(currentUserId);
        leadRepository.save(lead);

        log.info("Lead {} converted to Opportunity {} by userId={}", lead.getLeadId(), opp.getOpportunityId(), currentUserId);
        return opp;
    }

    // ─── Excel Import ─────────────────────────────────────────────────────────
    @Transactional
    public List<LeadResponse> importFromExcel(MultipartFile file, Long currentUserId) throws IOException {
        List<LeadResponse> imported = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // skip header row 0
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String leadName = getCellString(row, 0);
                if (leadName == null || leadName.isBlank()) continue; // skip empty rows

                CreateLeadRequest req = new CreateLeadRequest();
                req.setLeadName(leadName);
                req.setCompanyName(getCellString(row, 1));
                req.setContactPerson(getCellString(row, 2));
                req.setPhone(getCellString(row, 3));
                req.setEmail(getCellString(row, 4));
                req.setSalesRepId(currentUserId); // template has no Sales Rep column — default to the importer
                imported.add(createLead(req, currentUserId));
            }
        }
        log.info("Excel import: {} leads imported by userId={}", imported.size(), currentUserId);
        return imported;
    }

    // ─── Excel Template ───────────────────────────────────────────────────────
    public byte[] generateExcelTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Leads");
            Row header = sheet.createRow(0);
            String[] cols = {"Lead Name *", "Company", "Contact Person", "Phone", "Email"};
            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
                sheet.setColumnWidth(i, 6000);
            }
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────
    private Lead findActiveLead(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead not found: " + id));
        if (Boolean.TRUE.equals(lead.getIsDeleted())) {
            throw new RuntimeException("Lead not found: " + id);
        }
        return lead;
    }

    /** {@code privileged} (ADMIN/MANAGER) bypasses the ownership check. */
    private Lead findOwnedLead(Long id, Long currentUserId, boolean privileged) {
        Lead lead = findActiveLead(id);
        if (!privileged && !currentUserId.equals(lead.getSalesRepId())) {
            throw new RuntimeException("You can only access your own leads.");
        }
        return lead;
    }

    private LeadResponse toResponse(Lead lead) {
        String salesRepName = lead.getSalesRepId() != null
                ? userRepository.findById(lead.getSalesRepId()).map(User::getFullName).orElse(null)
                : null;
        return LeadResponse.builder()
                .id(lead.getId())
                .leadId(lead.getLeadId())
                .leadName(lead.getLeadName())
                .companyName(lead.getCompanyName())
                .contactPerson(lead.getContactPerson())
                .phone(lead.getPhone())
                .email(lead.getEmail())
                .status(lead.getStatus())
                .source(lead.getSource())
                .salesRepId(lead.getSalesRepId())
                .salesRepName(salesRepName)
                .remark(lead.getRemark())
                .createdBy(lead.getCreatedBy())
                .createdAt(lead.getCreatedAt())
                .updatedAt(lead.getUpdatedAt())
                .build();
    }

    /** V1.1: Phone/Email must not collide with another active (non-deleted) lead */
    private void validateUniquePhoneEmail(String phone, String email, Long excludeId) {
        boolean phoneTaken = excludeId == null
                ? leadRepository.existsByPhoneAndIsDeletedFalse(phone)
                : leadRepository.existsByPhoneAndIsDeletedFalseAndIdNot(phone, excludeId);
        if (phoneTaken) throw new RuntimeException("Phone number is already in use.");

        boolean emailTaken = excludeId == null
                ? leadRepository.existsByEmailAndIsDeletedFalse(email)
                : leadRepository.existsByEmailAndIsDeletedFalseAndIdNot(email, excludeId);
        if (emailTaken) throw new RuntimeException("Email is already in use.");
    }

    /** BR-001: Auto-generate Lead ID in format LEAD-0001 */
    private String generateLeadId() {
        long count = leadRepository.countAll() + 1;
        String candidate;
        do {
            candidate = String.format("LEAD-%04d", count++);
        } while (leadRepository.existsByLeadId(candidate));
        return candidate;
    }

    private String getCellString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> null;
        };
    }
}
