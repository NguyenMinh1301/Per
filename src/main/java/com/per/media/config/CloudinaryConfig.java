package com.per.media.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableConfigurationProperties(CloudinaryProperties.class)
@RequiredArgsConstructor
public class CloudinaryConfig {

    private final CloudinaryProperties properties;

    @Bean
    public Cloudinary cloudinary() {
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", properties.getCloudName());
        config.put("api_key", properties.getApiKey());
        config.put("api_secret", properties.getApiSecret());
        config.put("secure", true);
        return new Cloudinary(config);
    }
}
