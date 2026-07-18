package com.example.orderservice.api;

import com.example.orderservice.domain.OrderSatus;

import java.math.BigDecimal;
import java.util.Set;

public record OrderDto(
        Long id,
        Long customerId,
        String address,
        BigDecimal totalAmount,
        String courierName,
        Integer etaMinutes,
        OrderSatus orderStatus,
        Set<OrderItemDto> items
) {}
