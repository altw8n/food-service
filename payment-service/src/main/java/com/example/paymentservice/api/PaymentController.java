package com.example.paymentservice.api;

import com.example.http.payment.CreatePaymentRequestDto;
import com.example.http.payment.CreatePaymentResponseDto;
import com.example.paymentservice.domain.*;
import com.example.paymentservice.domain.db.PaymentEntityMapper;
import com.example.paymentservice.domain.db.PaymentEntityRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping( "/api/payments")
public class PaymentController {
    private final PaymentEntityRepository repository;
    private final PaymentEntityMapper mapper;
    private final PaymentService paymentService;

    @PostMapping
    public CreatePaymentResponseDto createPayment(
            @RequestBody CreatePaymentRequestDto request
    ){
        log.info("Recived reques: Request={}", request);

        return paymentService.makePayment(request);
    }

}
