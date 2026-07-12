package com.pqvcf.shared.types;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Standard container representing paginated query response envelopes.
 *
 * @param <T> Element list type
 * @author PQVCF Research Team
 * @version 1.0.0
 */
public final class PageResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;

    public PageResponse(List<T> content, int page, int size, long totalElements) {
        this.content = content != null ? List.copyOf(content) : Collections.emptyList();
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
    }

    public List<T> getContent() { return content; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public boolean hasNext() { return page < totalPages - 1; }
    public boolean hasPrevious() { return page > 0; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PageResponse<?> that)) return false;
        return page == that.page && size == that.size && totalElements == that.totalElements && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, page, size, totalElements);
    }
}
