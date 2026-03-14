package com.imran.flightbooking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BookingController {

    @GetMapping("/passenger-details")
    public String passengerDetailsPage() {
        return "booking/passenger-details";
    }

    @GetMapping("/seat-selection")
    public String seatSelectionPage() {
        return "booking/seat-selection";
    }

    @GetMapping("/payment")
    public String paymentPage() {
        return "booking/payment";
    }

    @GetMapping("/booking-success")
    public String bookingSuccessPage() {
        return "booking/success";
    }
}