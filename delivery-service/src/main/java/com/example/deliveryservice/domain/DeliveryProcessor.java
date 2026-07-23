package com.example.deliveryservice.domain;

import com.example.kafka.DeliveryAssignedEvent;
import com.example.kafka.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
@Slf4j
@Service
public class DeliveryProcessor {

    private final DeliveryEntityRepository repository;
    private final KafkaTemplate<Long, DeliveryAssignedEvent> kafkaTemplate;

    @Value("${delivery-assigned-topic}")
    private String deliveryAssignedTopic;

    public void processOrderPaid(OrderPaidEvent event) {
        var orderId = event.orderId();
        var found = repository.findByOrderId(orderId);

        if (found.isPresent()){
            log.info(" found order delivery was already assigned delivery={}", found.get());
            return;
        }

        var assignedDelivery = assignDelivery(orderId);

        sendDeliveryAssignedEvent(assignedDelivery);
    }

    private void sendDeliveryAssignedEvent(DeliveryEntity assignedDelivery) {
        kafkaTemplate.send(
                deliveryAssignedTopic,
                assignedDelivery.getOrderId(),
                DeliveryAssignedEvent.builder()
                        .courierName(assignedDelivery.getCourierName())
                        .orderId(assignedDelivery.getOrderId())
                        .etaMinutes(assignedDelivery.getEtaMinutes())
                        .build()
        ).thenAccept( result -> {
            log.info("delivery assigned-event sent: delivery={}", assignedDelivery.getId());
        });
    }

    private DeliveryEntity assignDelivery(Long orderId) {
        var entity = new DeliveryEntity();
        entity.setOrderId(orderId);
        entity.setCourierName("courier = " + ThreadLocalRandom.current().nextInt(100));
        entity.setEtaMinutes(ThreadLocalRandom.current().nextInt(10, 45));
        return repository.save(entity);
    }
}
