package com.example.orderservice.api;

import com.example.http.payment.PaymentMethod;

public record OrderPaymentRequest(
        PaymentMethod paymentMethod
) {}
