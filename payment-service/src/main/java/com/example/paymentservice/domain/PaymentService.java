package com.example.paymentservice.domain;

import com.example.http.payment.CreatePaymentRequestDto;
import com.example.http.payment.CreatePaymentResponseDto;
import com.example.http.payment.PaymentMethod;
import com.example.http.payment.PaymentStatus;
import com.example.paymentservice.domain.db.PaymentEntityMapper;
import com.example.paymentservice.domain.db.PaymentEntityRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class PaymentService {

    private final PaymentEntityRepository repository;
    private final PaymentEntityMapper mapper;

    public CreatePaymentResponseDto makePayment(CreatePaymentRequestDto request) {

        var found = repository.findByOrderId(request.orderId());
        if (found.isPresent()){
            log.info("Payment already exist for orderId={}", request.orderId());
            return mapper.toResponseDto(found.get());
        }

        var entity = mapper.toEntity(request);

        var status = request.paymentMethod().equals(PaymentMethod.QR)
                ? PaymentStatus.PAYMENT_FAILED
                : PaymentStatus.PAYMENT_SUCCESS;

        entity.setPaymentStatus(status);

        var savedEntity = repository.save(entity);
        return mapper.toResponseDto(savedEntity);
    }
}
