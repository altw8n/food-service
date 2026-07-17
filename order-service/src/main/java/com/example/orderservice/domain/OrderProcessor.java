package com.example.orderservice.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class OrderProcessor {

    private final OrderJpaRepository orderItemJpaRepository;

    public OrderEntity create(OrderEntity orderEntity){
        return orderItemJpaRepository.save(orderEntity);
    }

    public OrderEntity getOrderOrThrow(Long id){
        var orderItemOptional = orderItemJpaRepository.findById(id);
        return  orderItemOptional
                .orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id '%s' not found".formatted(id)));
    }
}
