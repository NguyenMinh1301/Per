package com.per.payment.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import vn.payos.PayOS;

@Configuration
@EnableConfigurationProperties(PayOsProperties.class)
public class PayOsConfig {

    @Bean
    public PayOS payOS(PayOsProperties properties) {
        return new PayOS(
                properties.getClientId(), properties.getApiKey(), properties.getChecksumKey());
    }
}
