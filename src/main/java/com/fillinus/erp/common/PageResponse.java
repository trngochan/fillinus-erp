package com.fillinus.erp.common;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Standard paginated list wrapper — every list endpoint in FILLINUS ERP returns this
 * (wrapped inside ApiResponse.data) instead of a raw array. Page size is restricted
 * to 10/20/50/100 at the FE Pagination component; BE just echoes back what was requested.
 */
@Data
@Builder
public class PageResponse<T> {
    private List<T> content;
    private int page;          // 0-based
    private int pageSize;
    private long totalElements;
    private int totalPages;

    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
