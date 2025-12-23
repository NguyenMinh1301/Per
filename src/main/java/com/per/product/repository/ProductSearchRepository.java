package com.per.product.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.per.product.document.ProductDocument;

public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {}
