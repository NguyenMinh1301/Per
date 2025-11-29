package com.per.payment.configuration;

import com.per.common.ApiConstants;
import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "payos")
public class PayOsProperties {

    @NotBlank private String clientId;

    @NotBlank private String apiKey;

    @NotBlank private String checksumKey;

    /** Internal callback endpoint that PayOS will hit for payment notifications. */
    @NotBlank private String webhookPath = ApiConstants.Payment.WEBHOOK;

    /** Relative path for PayOS redirect (success). */
    @NotBlank private String returnPath = ApiConstants.Payment.RETURN;

    /** Relative path for PayOS redirect (cancel). */
    @NotBlank private String cancelPath = ApiConstants.Payment.CANCEL;
}
