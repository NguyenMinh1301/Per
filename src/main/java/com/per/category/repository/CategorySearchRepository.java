package com.per.category.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.per.category.document.CategoryDocument;

public interface CategorySearchRepository
        extends ElasticsearchRepository<CategoryDocument, String> {}
