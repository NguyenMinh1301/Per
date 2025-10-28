package com.per.media.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "media_assets")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaAsset {

    @Id
    @UuidGenerator
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "asset_id", nullable = false, length = 150)
    private String assetId;

    @Column(name = "public_id", nullable = false, unique = true, length = 255)
    private String publicId;

    @Column(name = "folder", length = 255)
    private String folder;

    @Column(name = "resource_type", nullable = false, length = 32)
    private String resourceType;

    @Column(name = "format", length = 50)
    private String format;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "url", nullable = false, length = 2048)
    private String url;

    @Column(name = "secure_url", nullable = false, length = 2048)
    private String secureUrl;

    @Column(name = "bytes", nullable = false)
    private long bytes;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "duration")
    private Double duration;

    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    @Column(name = "etag", length = 100)
    private String etag;

    @Column(name = "signature", length = 255)
    private String signature;

    @Column(name = "version", length = 50)
    private String version;

    @Column(name = "cloud_created_at")
    private Instant cloudCreatedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
