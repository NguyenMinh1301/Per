package com.per.made_in.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.per.common.ApiConstants;
import com.per.common.ApiResponse;
import com.per.common.response.ApiSuccessCode;
import com.per.common.response.PageResponse;
import com.per.made_in.dto.request.MadeInCreateRequest;
import com.per.made_in.dto.request.MadeInUpdateRequest;
import com.per.made_in.dto.response.MadeInResponse;
import com.per.made_in.service.MadeInService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiConstants.MadeIn.ROOT)
@RequiredArgsConstructor
@Tag(name = "Made In", description = "Made In Origin APIs")
public class MadeInController {

    private final MadeInService madeInService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MadeInResponse>>> searchMadeIn(
            @RequestParam(value = "query", required = false) String query,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
                    Pageable pageable) {
        PageResponse<MadeInResponse> response = madeInService.getMadeIns(query, pageable);
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.MADEIN_LIST_SUCCESS, response));
    }

    @GetMapping(ApiConstants.MadeIn.DETAILS)
    public ResponseEntity<ApiResponse<MadeInResponse>> getMadeIn(@PathVariable("id") UUID id) {
        MadeInResponse response = madeInService.getMadeIn(id);
        return ResponseEntity.ok(
                ApiResponse.success(ApiSuccessCode.MADEIN_FETCH_SUCCESS, response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MadeInResponse>> create(
            @Valid @RequestBody MadeInCreateRequest request) {
        MadeInResponse response = madeInService.createMadeIn(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ApiSuccessCode.MADEIN_CREATE_SUCCESS, response));
    }

    @PutMapping(ApiConstants.MadeIn.DETAILS)
    public ResponseEntity<ApiResponse<MadeInResponse>> update(
            @PathVariable("id") UUID id, @Valid @RequestBody MadeInUpdateRequest request) {
        MadeInResponse response = madeInService.updateMadeIn(id, request);
        return ResponseEntity.ok(
                ApiResponse.success(ApiSuccessCode.MADEIN_UPDATE_SUCCESS, response));
    }

    @DeleteMapping(ApiConstants.MadeIn.DETAILS)
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") UUID id) {
        madeInService.deleteMadeIn(id);
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.MADEIN_DELETE_SUCCESS));
    }
}
