package com.imran.flightbooking.service;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.imran.flightbooking.entity.Payment;
import com.imran.flightbooking.repository.PaymentRepository;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    private Random random = new Random();

    public Payment processPayment(Payment payment) {
        payment.setPaymentStatus("SUCCESS");
        return paymentRepository.save(payment);
    }

    public boolean processPaymentTransaction(Payment payment) {
        // Simulate payment processing with 80% success rate
        boolean isSuccessful = random.nextDouble() < 0.8;
        
        if (isSuccessful) {
            payment.setPaymentStatus("SUCCESS");
            System.out.println("Payment processed successfully!");
        } else {
            payment.setPaymentStatus("FAILED");
            System.out.println("Payment processing failed!");
        }
        
        try {
            paymentRepository.save(payment);
        } catch (Exception e) {
            System.out.println("Error saving payment: " + e.getMessage());
            return false;
        }
        
        return isSuccessful;
    }
}