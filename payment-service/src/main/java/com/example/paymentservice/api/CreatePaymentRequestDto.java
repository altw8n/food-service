package com.example.paymentservice.api;

import com.example.paymentservice.domain.PaymentMethod;

import java.math.BigDecimal;

public record CreatePaymentRequestDto(
        Long orderId,
        PaymentMethod paymentMethod,
        BigDecimal amount
) {

}
