package com.per.brand.service;

import org.springframework.data.domain.Pageable;

import com.per.brand.document.BrandDocument;
import com.per.common.response.PageResponse;

public interface BrandSearchService {

    PageResponse<BrandDocument> search(String query, Pageable pageable);

    void index(BrandDocument document);

    void delete(String id);

    void reindexAll();
}
