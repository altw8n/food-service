package com.example.paymentservice.api;

import com.example.paymentservice.domain.PaymentMethod;
import com.example.paymentservice.domain.PaymentStatus;

import java.math.BigDecimal;

public record CreatePaymentResponseDto (
        Long paymentId,
        PaymentStatus paymentStatus,
        Long orderId,
        PaymentMethod paymentMethod,
        BigDecimal amount
){
}
