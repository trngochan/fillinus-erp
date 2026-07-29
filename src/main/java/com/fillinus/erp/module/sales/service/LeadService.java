package com.fillinus.erp.module.sales.service;

import com.fillinus.erp.module.auth.entity.User;
import com.fillinus.erp.module.auth.repository.UserRepository;
import com.fillinus.erp.module.sales.dto.*;
import com.fillinus.erp.module.sales.entity.Lead;
import com.fillinus.erp.module.sales.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
    public List<LeadResponse> getLeads(String search, String status) {
        String searchParam = (search != null && search.isBlank()) ? null : search;
        String statusParam = (status != null && (status.isBlank() || status.equalsIgnoreCase("ALL"))) ? null : status;
        return leadRepository.findAllActive(searchParam, statusParam)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ─── BUSINESS-02: Create Lead ─────────────────────────────────────────────
    @Transactional
    public LeadResponse createLead(CreateLeadRequest request, Long currentUserId) {
        String leadId = generateLeadId();
        Lead lead = Lead.builder()
                .leadId(leadId)
                .leadName(request.getLeadName())
                .companyName(request.getCompanyName())
                .contactPerson(request.getContactPerson())
                .phone(request.getPhone())
                .email(request.getEmail())
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
    public LeadResponse getLead(Long id) {
        Lead lead = findActiveLead(id);
        return toResponse(lead);
    }

    // ─── BUSINESS-04: Edit Lead ───────────────────────────────────────────────
    @Transactional
    public LeadResponse updateLead(Long id, CreateLeadRequest request, Long currentUserId) {
        Lead lead = findActiveLead(id);
        // Lead ID (leadId) is immutable per BR-001
        lead.setLeadName(request.getLeadName());
        lead.setCompanyName(request.getCompanyName());
        lead.setContactPerson(request.getContactPerson());
        lead.setPhone(request.getPhone());
        lead.setEmail(request.getEmail());
        lead.setUpdatedBy(currentUserId);
        leadRepository.save(lead);
        log.info("Lead updated: {} by userId={}", lead.getLeadId(), currentUserId);
        return toResponse(lead);
    }

    // ─── BUSINESS-05: Soft Delete Lead ────────────────────────────────────────
    @Transactional
    public void deleteLead(Long id, Long currentUserId) {
        Lead lead = findActiveLead(id);
        lead.setIsDeleted(true);  // BR-005: soft delete only
        lead.setUpdatedBy(currentUserId);
        leadRepository.save(lead);
        log.info("Lead soft-deleted: {} by userId={}", lead.getLeadId(), currentUserId);
    }

    // ─── BUSINESS-06: Convert Lead to Opportunity ────────────────────────────
    @Transactional
    public OpportunityResponse convertLead(Long id, Long currentUserId) {
        Lead lead = findActiveLead(id);

        // BR-004: Cannot convert twice
        if ("CONVERTED".equals(lead.getStatus())) {
            throw new RuntimeException("Lead " + lead.getLeadId() + " has already been converted.");
        }

        OpportunityResponse opp = opportunityService.createFromLead(lead, currentUserId);

        // Mark lead as CONVERTED — disappears from lead list
        lead.setStatus("CONVERTED");
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

    private LeadResponse toResponse(Lead lead) {
        return LeadResponse.builder()
                .id(lead.getId())
                .leadId(lead.getLeadId())
                .leadName(lead.getLeadName())
                .companyName(lead.getCompanyName())
                .contactPerson(lead.getContactPerson())
                .phone(lead.getPhone())
                .email(lead.getEmail())
                .status(lead.getStatus())
                .createdBy(lead.getCreatedBy())
                .createdAt(lead.getCreatedAt())
                .updatedAt(lead.getUpdatedAt())
                .build();
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
