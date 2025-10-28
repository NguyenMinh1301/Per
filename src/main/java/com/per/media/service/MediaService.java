package com.per.media.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.per.media.dto.response.MediaUploadResponse;

public interface MediaService {

    MediaUploadResponse uploadSingle(MultipartFile file);

    List<MediaUploadResponse> uploadBatch(List<MultipartFile> files);
}
