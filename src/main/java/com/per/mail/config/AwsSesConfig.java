package com.per.mail.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

/**
 * AWS SES client configuration. Uses StaticCredentialsProvider with explicit credentials from
 * application properties.
 */
@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class AwsSesConfig {

    @Bean
    public SesClient sesClient(MailProperties props) {
        AwsBasicCredentials credentials =
                AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey());

        return SesClient.builder()
                .region(Region.of(props.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}
