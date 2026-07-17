package com.example.orderservice.domain;

public enum OrderSatus {
    PENDING_PAYMENT,
    PAID,
    PAYMENT_FAILED,
    PENDING_DELIVERY,
    DELIVERED;
}
