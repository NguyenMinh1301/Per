package com.per.product.entity;

import java.time.Instant;
import java.util.UUID;

import com.per.brand.entity.Brand;
import com.per.category.entity.Category;
import com.per.made_in.entity.MadeIn;
import com.per.product.enums.FragranceFamily;
import com.per.product.enums.Gender;
import com.per.product.enums.Sillage;
import com.per.product.enums.Longevity;
import com.per.product.enums.Seasonality;
import com.per.product.enums.Occasion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @UuidGenerator
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Foreign key
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "made_in_id", nullable = false)
    private MadeIn madeIn;

    // Info
    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "short_description", length = 600)
    private String shortDescription;

    @Column(name = "description")
    private String description;

    @Column(name = "launch_year")
    private Integer launchYear;

    @Column(name = "image_public_id", length = 255)
    private String imagePublicId;

    @Column(name = "image_url")
    private String imageUrl;

    // Details info
    @Enumerated(EnumType.STRING)
    @Column(name = "fragrance_family", length = 80)
    private FragranceFamily fragranceFamily;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "sillage", length = 30)
    private Sillage sillage;

    @Enumerated(EnumType.STRING)
    @Column(name = "longevity", length = 30)
    private Longevity longevity;

    @Enumerated(EnumType.STRING)
    @Column(name = "seasonality", length = 80)
    private Seasonality seasonality;

    @Enumerated(EnumType.STRING)
    @Column(name = "occasion", length = 120)
    private Occasion occasion;

    @Column(name = "is_limited_edition", nullable = false)
    private boolean isLimitedEdition = false;

    @Column(name = "is_discontinued", nullable = false)
    private boolean isDiscontinued = false;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
