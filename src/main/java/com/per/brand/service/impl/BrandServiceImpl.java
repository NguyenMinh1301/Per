package com.per.brand.service.impl;

import java.util.UUID;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.per.brand.dto.request.BrandCreateRequest;
import com.per.brand.dto.request.BrandUpdateRequest;
import com.per.brand.dto.response.BrandResponse;
import com.per.brand.entity.Brand;
import com.per.brand.mapper.BrandDocumentMapper;
import com.per.brand.mapper.BrandMapper;
import com.per.brand.repository.BrandRepository;
import com.per.brand.service.BrandService;
import com.per.common.config.cache.CacheEvictionHelper;
import com.per.common.config.cache.CacheNames;
import com.per.common.config.kafka.KafkaTopicNames;
import com.per.common.event.BrandIndexEvent;
import com.per.common.exception.ApiErrorCode;
import com.per.common.exception.ApiException;
import com.per.common.response.PageResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;
    private final BrandDocumentMapper documentMapper;
    private final CacheEvictionHelper cacheEvictionHelper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.BRANDS,
            key =
                    "'list:' + (#query ?: 'all') + ':p' + #pageable.pageNumber + ':s' + #pageable.pageSize",
            sync = true)
    public PageResponse<BrandResponse> getBrands(String query, Pageable pageable) {
        Page<Brand> page;

        if (query == null || query.isBlank()) {
            page = brandRepository.findAll(pageable);
        } else {
            page = brandRepository.search(query, pageable);
        }

        return PageResponse.from(page.map(brandMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.BRAND, key = "#id", sync = true)
    public BrandResponse getBrand(UUID id) {
        Brand brand = findById(id);
        return brandMapper.toResponse(brand);
    }

    @Override
    public BrandResponse createBrand(BrandCreateRequest request) {
        String name = request.getName();
        validateNameUniqueness(name);

        Brand brand = brandMapper.toEntity(request);
        brand.setName(name);
        brand.setIsActive(
                request.getIsActive() == null || Boolean.TRUE.equals(request.getIsActive()));

        Brand saved = brandRepository.save(brand);

        cacheEvictionHelper.evictAllAfterCommit(CacheNames.BRANDS);

        // Publish Kafka event for ES indexing
        publishIndexEvent(saved, BrandIndexEvent.Action.INDEX);

        return brandMapper.toResponse(saved);
    }

    @Override
    public BrandResponse updateBrand(UUID id, BrandUpdateRequest request) {
        Brand brand = findById(id);

        String name = null;
        if (request.getName() != null) {
            name = request.getName();
            if (!name.equalsIgnoreCase(brand.getName())) {
                validateNameUniqueness(name, id);
            }
        }

        brandMapper.updateEntity(request, brand);

        if (name != null) {
            brand.setName(name);
        }
        if (request.getIsActive() != null) {
            brand.setIsActive(request.getIsActive());
        }

        Brand saved = brandRepository.save(brand);

        cacheEvictionHelper.evictAllAfterCommit(CacheNames.BRANDS);
        cacheEvictionHelper.evictAfterCommit(CacheNames.BRAND, id);

        // Publish Kafka event for ES indexing
        publishIndexEvent(saved, BrandIndexEvent.Action.INDEX);

        return brandMapper.toResponse(saved);
    }

    @Override
    public void deleteBrand(UUID id) {
        Brand brand = findById(id);
        brandRepository.delete(brand);

        cacheEvictionHelper.evictAllAfterCommit(CacheNames.BRANDS);
        cacheEvictionHelper.evictAfterCommit(CacheNames.BRAND, id);

        // Publish Kafka event for ES deletion
        kafkaTemplate.send(
                KafkaTopicNames.BRAND_INDEX_TOPIC,
                BrandIndexEvent.builder()
                        .action(BrandIndexEvent.Action.DELETE)
                        .brandId(id.toString())
                        .build());
    }

    private void publishIndexEvent(Brand brand, BrandIndexEvent.Action action) {
        kafkaTemplate.send(
                KafkaTopicNames.BRAND_INDEX_TOPIC,
                BrandIndexEvent.builder()
                        .action(action)
                        .brandId(brand.getId().toString())
                        .document(documentMapper.toDocument(brand))
                        .build());
    }

    private Brand findById(UUID id) {
        return brandRepository
                .findById(id)
                .orElseThrow(() -> new ApiException(ApiErrorCode.BRAND_NOT_FOUND));
    }

    private void validateNameUniqueness(String name) {
        if (brandRepository.existsByNameIgnoreCase(name)) {
            throw new ApiException(ApiErrorCode.BRAND_NAME_CONFLICT);
        }
    }

    private void validateNameUniqueness(String name, UUID id) {
        if (brandRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ApiException(ApiErrorCode.BRAND_NAME_CONFLICT);
        }
    }
}
