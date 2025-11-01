package com.per.made_in.service.impl;

import com.per.category.entity.Category;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MadeInServiceImpl implements MadeInService {

    private final MadeInRepository madeInRepository;
    private final MadeInMapper madeInMapper;

    @Override
    @Transactional(readOnly = true)
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
    public MadeInResponse getMadeIn(UUID id) {
        MadeIn madeIn = findById(id);
        return madeInMapper.toResponse(madeIn);
    }

    @Override
    @Transactional(readOnly = true)
    public MadeInResponse createMadeIn(MadeInCreateRequest request) {
        String name = request.getName();
        validateNameUniqueness(name);

        MadeIn madeIn = madeInMapper.toEntity(request);
        madeIn.setName(name);
        madeIn.setIsActive(request.getIsActive() == null || Boolean.TRUE.equals(request.getIsActive()));

        MadeIn saved = madeInMapper.toEntity(request);
        return madeInMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
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
        if (request.getIsActive() != null) {
            madeIn.setIsActive(request.getIsActive());
        }

        MadeIn saved = madeInRepository.save(madeIn);
        return madeInMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public void deleteMadeIn(UUID id) {
        MadeIn madeIn = findById(id);
        madeInRepository.delete(madeIn);
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
