package com.per.category.service;

import org.springframework.data.domain.Pageable;

import com.per.category.document.CategoryDocument;
import com.per.common.response.PageResponse;

public interface CategorySearchService {

    PageResponse<CategoryDocument> search(String query, Pageable pageable);

    void index(CategoryDocument document);

    void delete(String id);

    void reindexAll();
}
