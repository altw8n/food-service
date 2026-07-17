package com.example.orderservice.api;

import com.example.orderservice.domain.OrderEntity;
import com.example.orderservice.domain.OrderEntityMapper;
import com.example.orderservice.domain.OrderItemEntity;
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

    private OrderProcessor orderProcessor;
    private final OrderEntityMapper orderEntityMapper;

    @PostMapping
    public OrderDto create(@RequestBody OrderEntity orderEntity) {
        log.info("Создан заказ с Id {}", orderEntity.getId());
        var saved = orderProcessor.create(orderEntity);
        return orderEntityMapper.toOrderDto(saved);
    }

    @GetMapping("/{id}")
    public OrderDto getOne(@PathVariable Long id) {
        log.info("Запрос на выдачу заказа с Id {}", id);
        var found = orderProcessor.getOrderOrThrow(id);
        return orderEntityMapper.toOrderDto(found);
    }
}