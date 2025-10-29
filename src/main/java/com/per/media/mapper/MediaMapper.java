package com.per.media.mapper;

import org.mapstruct.Mapper;

import com.per.media.dto.response.MediaUploadResponse;
import com.per.media.entity.MediaAsset;

@Mapper(componentModel = "spring")
public interface MediaMapper {

	MediaUploadResponse toUploadResponse(MediaAsset asset);
}
