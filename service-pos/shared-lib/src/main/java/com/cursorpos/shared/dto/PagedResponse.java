package com.cursorpos.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated response wrapper.
 * 
 * <p>
 * Used for endpoints that return paginated lists of data.
 * Includes pagination metadata for client-side navigation.
 * </p>
 * 
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>
 * Page&lt;User&gt; page = userRepository.findAll(pageable);
 * return PagedResponse.of(page);
 * </pre>
 * 
 * @param <T> the type of data items
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {

    /**
     * The list of items for the current page.
     */
    private List<T> content;

    /**
     * Current page number (0-indexed).
     */
    private int pageNumber;

    /**
     * Number of items per page.
     */
    private int pageSize;

    /**
     * Total number of items across all pages.
     */
    private long totalElements;

    /**
     * Total number of pages.
     */
    private int totalPages;

    /**
     * Indicates if this is the first page.
     */
    private boolean first;

    /**
     * Indicates if this is the last page.
     */
    private boolean last;

    /**
     * Indicates if there are more pages after this one.
     */
    private boolean hasNext;

    /**
     * Indicates if there are pages before this one.
     */
    private boolean hasPrevious;

    /**
     * Number of items in the current page.
     */
    private int numberOfElements;

    /**
     * Creates a PagedResponse from Spring Data Page object.
     * 
     * @param page the Spring Data Page
     * @param <R> the type of content
     * @return PagedResponse instance
     */
    public static <R> PagedResponse<R> of(org.springframework.data.domain.Page<R> page) {
        return PagedResponse.<R>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .numberOfElements(page.getNumberOfElements())
                .build();
    }

    /**
     * Creates an empty PagedResponse.
     * 
     * @param <R> the type of content
     * @return empty PagedResponse
     */
    public static <R> PagedResponse<R> empty() {
        return PagedResponse.<R>builder()
                .content(List.of())
                .pageNumber(0)
                .pageSize(0)
                .totalElements(0L)
                .totalPages(0)
                .first(true)
                .last(true)
                .hasNext(false)
                .hasPrevious(false)
                .numberOfElements(0)
                .build();
    }
}
