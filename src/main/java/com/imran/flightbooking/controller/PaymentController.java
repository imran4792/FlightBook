package com.imran.flightbooking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaymentController {

    @GetMapping("/payment-page")
    public String paymentPage() {
        return "payment/payment";
    }

    @GetMapping("/payment-success")
    public String paymentSuccessPage() {
        return "payment/success";
    }

    @GetMapping("/payment-failed")
    public String paymentFailedPage() {
        return "payment/failed";
    }
}