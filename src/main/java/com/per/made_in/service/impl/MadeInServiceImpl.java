package com.per.made_in.service.impl;

import java.util.UUID;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.per.common.config.cache.CacheEvictionHelper;
import com.per.common.config.cache.CacheNames;
import com.per.common.exception.ApiErrorCode;
import com.per.common.exception.ApiException;
import com.per.common.response.PageResponse;
import com.per.made_in.dto.request.MadeInCreateRequest;
import com.per.made_in.dto.request.MadeInUpdateRequest;
import com.per.made_in.dto.response.MadeInResponse;
import com.per.made_in.entity.MadeIn;
import com.per.made_in.mapper.MadeInMapper;
import com.per.made_in.repository.MadeInRepository;
import com.per.made_in.service.MadeInService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MadeInServiceImpl implements MadeInService {

    private final MadeInRepository madeInRepository;
    private final MadeInMapper madeInMapper;
    private final CacheEvictionHelper cacheEvictionHelper;

    // Note: Dual-write removed - CDC via Debezium handles ES sync

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheNames.MADE_INS,
            key =
                    "'list:' + (#query ?: 'all') + ':p' + #pageable.pageNumber + ':s' + #pageable.pageSize",
            sync = true)
    public PageResponse<MadeInResponse> getMadeIns(String query, Pageable pageable) {
        Page<MadeIn> page;

        if (query == null || query.isBlank()) {
            page = madeInRepository.findAll(pageable);
        } else {
            page = madeInRepository.search(query, pageable);
        }

        return PageResponse.from(page.map(madeInMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.MADE_IN, key = "#id", sync = true)
    public MadeInResponse getMadeIn(UUID id) {
        MadeIn madeIn = findById(id);
        return madeInMapper.toResponse(madeIn);
    }

    @Override
    public MadeInResponse createMadeIn(MadeInCreateRequest request) {
        String name = request.getName();
        validateNameUniqueness(name);

        MadeIn madeIn = madeInMapper.toEntity(request);
        madeIn.setName(name);

        MadeIn saved = madeInRepository.save(madeIn);

        cacheEvictionHelper.evictAllAfterCommit(CacheNames.MADE_INS);
        // Note: ES sync handled by CDC

        return madeInMapper.toResponse(saved);
    }

    @Override
    public MadeInResponse updateMadeIn(UUID id, MadeInUpdateRequest request) {
        MadeIn madeIn = findById(id);

        String name = null;
        if (request.getName() != null) {
            name = request.getName();
            if (!name.equalsIgnoreCase(madeIn.getName())) {
                validateNameUniqueness(name, id);
            }
        }

        madeInMapper.updateEntity(request, madeIn);

        if (name != null) {
            madeIn.setName(name);
        }

        MadeIn saved = madeInRepository.save(madeIn);

        cacheEvictionHelper.evictAllAfterCommit(CacheNames.MADE_INS);
        cacheEvictionHelper.evictAfterCommit(CacheNames.MADE_IN, id);
        // Note: ES sync handled by CDC

        return madeInMapper.toResponse(saved);
    }

    @Override
    public void deleteMadeIn(UUID id) {
        MadeIn madeIn = findById(id);
        madeInRepository.delete(madeIn);

        cacheEvictionHelper.evictAllAfterCommit(CacheNames.MADE_INS);
        cacheEvictionHelper.evictAfterCommit(CacheNames.MADE_IN, id);
        // Note: ES sync handled by CDC
    }

    private MadeIn findById(UUID id) {
        return madeInRepository
                .findById(id)
                .orElseThrow(() -> new ApiException(ApiErrorCode.MADEIN_NOT_FOUND));
    }

    private void validateNameUniqueness(String name) {
        if (madeInRepository.existsByNameIgnoreCase(name)) {
            throw new ApiException(ApiErrorCode.MADEIN_NAME_CONFLICT);
        }
    }

    private void validateNameUniqueness(String name, UUID id) {
        if (madeInRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ApiException(ApiErrorCode.MADEIN_NAME_CONFLICT);
        }
    }
}
