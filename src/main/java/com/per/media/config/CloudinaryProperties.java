package com.per.media.config;

import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "cloudinary")
public class CloudinaryProperties {

    @NotBlank private String cloudName;

    @NotBlank private String apiKey;

    @NotBlank private String apiSecret;

    private String folder;
}
