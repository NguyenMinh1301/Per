package com.per.brand.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.per.brand.document.BrandDocument;

public interface BrandSearchRepository extends ElasticsearchRepository<BrandDocument, String> {}
