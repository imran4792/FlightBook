package com.imran.flightbooking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @GetMapping("/admin/login")
    public String adminLoginPage() {
        return "admin/login";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboardPage() {
        return "admin/dashboard";
    }

    @GetMapping("/admin/add-flight")
    public String addFlightPage() {
        return "admin/add-flight";
    }

    @GetMapping("/admin/manage-flights")
    public String manageFlightsPage() {
        return "admin/manage-flights";
    }

    @GetMapping("/admin/manage-bookings")
    public String manageBookingsPage() {
        return "admin/manage-bookings";
    }
}