package com.per.mail.config;

import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties for SMTP email service. Values are bound from mail.* properties. Spring
 * Boot auto-configures JavaMailSender from spring.mail.* properties.
 */
@Getter
@Setter
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "mail")
public class MailProperties {

    /** Sender email address for the "From" header. Required. */
    @NotBlank private String from;
}
