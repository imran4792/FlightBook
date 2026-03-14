package com.imran.flightbooking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FlightController {

    @GetMapping("/search-flights")
    public String searchFlightsPage() {
        return "flights/search";
    }

    @GetMapping("/flight-results")
    public String flightResultsPage() {
        return "flights/results";
    }

    @GetMapping("/flight-details")
    public String flightDetailsPage() {
        return "flights/details";
    }
}