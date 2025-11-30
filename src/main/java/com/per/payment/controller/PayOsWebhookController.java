package com.per.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.per.common.ApiResponse;
import com.per.common.response.ApiSuccessCode;
import com.per.payment.service.PayOsWebhookService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import vn.payos.type.Webhook;

@RestController
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Payment Management APIs")
public class PayOsWebhookController {

    private final PayOsWebhookService webhookService;

    @PostMapping("${payos.webhook-path}")
    public ResponseEntity<ApiResponse<Void>> handleWebhook(@RequestBody Webhook payload) {
        webhookService.handleWebhook(payload);
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.PAYMENT_WEBHOOK_SUCCESS));
    }
}
