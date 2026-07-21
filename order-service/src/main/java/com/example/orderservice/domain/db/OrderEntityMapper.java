package com.example.orderservice.domain.db;

import com.example.http.order.CreateOrderRequestDto;
import com.example.http.order.OrderDto;
import com.example.http.order.OrderItemDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.LinkedHashSet;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface OrderEntityMapper {

    OrderEntity toEntity(CreateOrderRequestDto requestDto);

    @AfterMapping
    default void linkOrderItemEntities(@MappingTarget OrderEntity orderEntity) {
        if (orderEntity.getItems() == null) {
            orderEntity.setItems(new LinkedHashSet<>());
        }
        orderEntity.getItems()
                .forEach(item -> item.setOrder(orderEntity));
    }

    OrderDto toOrderDto(OrderEntity entity);

    OrderItemDto toOrderItemDto(OrderItemEntity entity);
}
