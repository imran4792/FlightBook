package com.imran.flightbooking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.imran.flightbooking.entity.Payment;
import com.imran.flightbooking.repository.PaymentRepository;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    public Payment processPayment(Payment payment) {
        payment.setPaymentStatus("SUCCESS");
        return paymentRepository.save(payment);
    }
}