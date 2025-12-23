package com.per.made_in.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.per.made_in.document.MadeInDocument;

public interface MadeInSearchRepository extends ElasticsearchRepository<MadeInDocument, String> {}
