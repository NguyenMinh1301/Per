package com.per.media.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.per.media.entity.MediaAsset;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, UUID> {

    boolean existsByPublicId(String publicId);
}
