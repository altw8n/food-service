package com.example.orderservice.domain;

import com.example.orderservice.api.CreateOrderRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
@Service
public class OrderProcessor {

    private final OrderEntityMapper orderEntityMapper;
    private final OrderJpaRepository orderItemJpaRepository;

    public OrderEntity create(CreateOrderRequestDto request){
        var entity = orderEntityMapper.toEntity(request);
        calculatePricingsForOrder(entity);
        entity.setOrderStatus(OrderSatus.PENDING_PAYMENT);
        return orderItemJpaRepository.save(entity);
    }

    public OrderEntity getOrderOrThrow(Long id){
        var orderItemOptional = orderItemJpaRepository.findById(id);
        return  orderItemOptional
                .orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id '%s' not found".formatted(id)));
    }

    private void calculatePricingsForOrder(OrderEntity entity) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OrderItemEntity item : entity.getItems()){
            var randomPrice = ThreadLocalRandom.current().nextDouble(100, 5000);
            item.setPriceAtPurchase(BigDecimal.valueOf(randomPrice));

            totalPrice = item.getPriceAtPurchase()
                    .multiply(BigDecimal.valueOf(item.getQuantity()))
                    .add(totalPrice);
        }
        entity.setTotalAmount(totalPrice);
    }

}
