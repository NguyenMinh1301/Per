package com.per.made_in.service;

import java.util.UUID;

import com.per.made_in.dto.request.MadeInCreateRequest;
import com.per.made_in.dto.request.MadeInUpdateRequest;
import com.per.made_in.dto.response.MadeInResponse;
import org.springframework.data.domain.Pageable;

import com.per.common.response.PageResponse;

public interface MadeInService {

    PageResponse<MadeInResponse> getMadeIns(String query, Pageable pageable);

    MadeInResponse getMadeIn(UUID id);

    MadeInResponse createMadeIn(MadeInCreateRequest request);

    MadeInResponse updateMadeIn(UUID id, MadeInUpdateRequest request);

    void deleteMadeIn(UUID id);
}
