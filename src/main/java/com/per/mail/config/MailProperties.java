package com.per.mail.config;

import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties for AWS SES email service. Values are bound from aws.ses.* properties.
 * All fields are required - application will fail to start if not configured.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "aws.ses")
public class MailProperties {

    /** AWS region for SES service (e.g., ap-southeast-1). Required. */
    @NotBlank private String region;

    /** Verified sender email address in AWS SES. Required. */
    @NotBlank private String sourceEmail;

    /** AWS Access Key ID for SES authentication. Required. */
    @NotBlank private String accessKey;

    /** AWS Secret Access Key for SES authentication. Required. */
    @NotBlank private String secretKey;
}
