package com.imran.flightbooking.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public void sendBookingConfirmation(String email) {

        // In real projects this sends email/SMS
        System.out.println("Booking confirmation sent to " + email);
    }
}