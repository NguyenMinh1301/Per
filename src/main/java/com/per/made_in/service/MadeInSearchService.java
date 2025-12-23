package com.per.made_in.service;

import org.springframework.data.domain.Pageable;

import com.per.common.response.PageResponse;
import com.per.made_in.document.MadeInDocument;

public interface MadeInSearchService {

    PageResponse<MadeInDocument> search(String query, Pageable pageable);

    void index(MadeInDocument document);

    void delete(String id);

    void reindexAll();
}
