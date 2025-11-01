package com.per.made_in.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.per.common.response.PageResponse;
import com.per.made_in.dto.request.MadeInCreateRequest;
import com.per.made_in.dto.request.MadeInUpdateRequest;
import com.per.made_in.dto.response.MadeInResponse;

public interface MadeInService {

    PageResponse<MadeInResponse> getMadeIns(String query, Pageable pageable);

    MadeInResponse getMadeIn(UUID id);

    MadeInResponse createMadeIn(MadeInCreateRequest request);

    MadeInResponse updateMadeIn(UUID id, MadeInUpdateRequest request);

    void deleteMadeIn(UUID id);
}
