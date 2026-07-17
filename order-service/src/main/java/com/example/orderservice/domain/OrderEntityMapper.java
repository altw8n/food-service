package com.example.orderservice.domain;

import com.example.orderservice.api.OrderDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface OrderEntityMapper {

    OrderEntity toEntity(OrderDto requestDto);

    @AfterMapping
    default void linkOrderItemEntities(@MappingTarget OrderEntity orderEntity) {
        orderEntity.getOrderItemEntities().forEach(orderItemEntity -> orderItemEntity.setOrder(orderEntity));
    }

    OrderDto toOrderDto(OrderEntity orderEntity);
}
