package com.per.payment.service;

import com.per.payment.dto.request.CheckoutRequest;
import com.per.payment.dto.response.CheckoutResponse;

public interface CheckoutService {

    CheckoutResponse checkout(CheckoutRequest request);
}
