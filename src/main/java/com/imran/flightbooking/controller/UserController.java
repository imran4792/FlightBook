package com.imran.flightbooking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "user/dashboard";
    }

    @GetMapping("/profile")
    public String profilePage() {
        return "user/profile";
    }

    @GetMapping("/edit-profile")
    public String editProfilePage() {
        return "user/edit-profile";
    }

    @GetMapping("/booking-history")
    public String bookingHistoryPage() {
        return "user/booking-history";
    }
}