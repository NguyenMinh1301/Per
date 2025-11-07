package com.per.payment.service;

import vn.payos.type.Webhook;

public interface PayOsWebhookService {

    void handleWebhook(Webhook payload);
}
