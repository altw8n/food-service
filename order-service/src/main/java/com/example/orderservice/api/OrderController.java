package com.example.orderservice.api;

import com.example.http.order.CreateOrderRequestDto;
import com.example.http.order.OrderDto;
import com.example.orderservice.domain.db.OrderEntityMapper;
import com.example.orderservice.domain.OrderProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderProcessor orderProcessor;

    private final OrderEntityMapper orderEntityMapper;

    @PostMapping
    public OrderDto createOrder(@RequestBody CreateOrderRequestDto request) {
        log.info("Создание заказа: request = {}", request);
        var entity = orderProcessor.create(request);
        return orderEntityMapper.toOrderDto(entity);
    }

    @PostMapping("/{id}/pay")
    public OrderDto payOrder(
            @PathVariable Long id,
            @RequestBody OrderPaymentRequest request
    ) {
        log.info("Оплачивается заказ с id = {}, request = {}", id, request);
        var entity = orderProcessor.processPayment(id, request);
        return orderEntityMapper.toOrderDto(entity);
    }

    @GetMapping("/{id}")
    public OrderDto getOne(@PathVariable Long id) {
        log.info("Запрос на выдачу заказа с Id {}", id);
        var found = orderProcessor.getOrderOrThrow(id);
        return orderEntityMapper.toOrderDto(found);
    }
}