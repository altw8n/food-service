package com.example.orderservice.domain;

import com.example.http.order.CreateOrderRequestDto;
import com.example.http.order.OrderSatus;
import com.example.http.payment.CreatePaymentRequestDto;
import com.example.http.payment.CreatePaymentResponseDto;
import com.example.http.payment.PaymentStatus;
import com.example.kafka.DeliveryAssignedEvent;
import com.example.kafka.OrderPaidEvent;
import com.example.orderservice.api.OrderPaymentRequest;
import com.example.orderservice.domain.db.OrderEntity;
import com.example.orderservice.domain.db.OrderEntityMapper;
import com.example.orderservice.domain.db.OrderItemEntity;
import com.example.orderservice.domain.db.OrderJpaRepository;
import com.example.orderservice.external.PaymentHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderProcessor {

    private final OrderEntityMapper orderEntityMapper;
    private final OrderJpaRepository repository;
    private final PaymentHttpClient paymentHttpClient;
    private final KafkaTemplate<Long, OrderPaidEvent> kafkaTemplate;

    @Value("${order-paid-topic}")
    private String orderPaidTopic;

    public OrderEntity create(CreateOrderRequestDto request){
        var entity = orderEntityMapper.toEntity(request);
        calculatePricingsForOrder(entity);
        entity.setOrderStatus(OrderSatus.PENDING_PAYMENT);
        return repository.save(entity);
    }

    public OrderEntity getOrderOrThrow(Long id){
        var orderItemOptional = repository.findById(id);
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

    public OrderEntity processPayment(
            Long id,
            OrderPaymentRequest request
    ) {

        var entity = getOrderOrThrow(id);
        if (!entity.getOrderStatus().equals(OrderSatus.PENDING_PAYMENT)){
            throw  new RuntimeException("Order must be in status PENDING_PAYMENT");
        }
        var response = paymentHttpClient.createPayment(CreatePaymentRequestDto.builder()
                        .orderId(id)
                        .paymentMethod(request.paymentMethod())
                        .amount(entity.getTotalAmount())
                .build());

        var status = response.paymentStatus().equals(PaymentStatus.PAYMENT_SUCCESS)
                    ? OrderSatus.PAID
                    : OrderSatus.PAYMENT_FAILED;

        entity.setOrderStatus(status);
        sendOrderPaidEvent(entity, response);
        return repository.save(entity);

    }

    private void sendOrderPaidEvent(
            OrderEntity entity,
            CreatePaymentResponseDto paymentResponseDto
    ) {
        kafkaTemplate.send(
                orderPaidTopic,
                entity.getId(),
                OrderPaidEvent.builder()
                        .orderId(entity.getId())
                        .amount(entity.getTotalAmount())
                        .paymentMethod(paymentResponseDto.paymentMethod())
                        .paymentId(paymentResponseDto.paymentId())
                        .build()
        ).thenAccept(result -> {
            log.info("Order Paid event sent with: id = {}", entity.getId());
        });

    }

    public void processDeliveryAssigned(DeliveryAssignedEvent event) {
        var order = getOrderOrThrow(event.orderId());

        if(!order.getOrderStatus().equals(OrderSatus.PAID)){
            processIncorrectDeliveryState(order);
            return;
        }
        order.setOrderStatus(OrderSatus.DELIVERY_ASSIGNED);
        order.setCourierName(event.courierName());
        order.setEtaMinutes(event.etaMinutes());
        repository.save(order);
    }

    private void processIncorrectDeliveryState(OrderEntity order) {
        if(order.getOrderStatus().equals(OrderSatus.DELIVERY_ASSIGNED)){
            log.info("Order delivery already processed id={}", order.getId());
        } else if (!order.getOrderStatus().equals(OrderSatus.PAID)) {
            log.error("Trying to assign delivery but order have incorrect state: state={}", order.getId());
        }
    }
}
