package com.imran.flightbooking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.imran.flightbooking.service.FlightService;
import com.imran.flightbooking.entity.Flight;
import com.imran.flightbooking.repository.AirlineRepository;
import com.imran.flightbooking.repository.AirportRepository;
import com.imran.flightbooking.service.UserService;
import com.imran.flightbooking.entity.User;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FlightController {

    @Autowired
    private FlightService flightService;
    
    @Autowired
    private AirlineRepository airlineRepository;
    
    @Autowired
    private AirportRepository airportRepository;

    @Autowired
    private UserService userService;

    private static final Map<String, String> CITY_MAP = new HashMap<>();
    static {
        CITY_MAP.put("DEL", "New Delhi");
        CITY_MAP.put("BOM", "Mumbai");
        CITY_MAP.put("BLR", "Bengaluru");
        CITY_MAP.put("HYD", "Hyderabad");
        CITY_MAP.put("AMD", "Ahmedabad");
        CITY_MAP.put("CCU", "Kolkata");
        CITY_MAP.put("MAA", "Chennai");
        CITY_MAP.put("COK", "Kochi");
        CITY_MAP.put("PNQ", "Pune");
        CITY_MAP.put("GOI", "Goa");
    }

    @GetMapping("/search-flights")
    public String searchFlightsPage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            User user = userService.getUserById(userId);
            model.addAttribute("user", user);
        }
        model.addAttribute("airports", airportRepository.findAll());
        return "flights/search";
    }

    @GetMapping("/flight-results")
    public String flightResultsPage(@RequestParam String source, @RequestParam String destination, 
                                    @RequestParam String date, @RequestParam String passengers, Model model) {
        if (flightService.isSamePlace(source, destination)) {
            model.addAttribute("error", "same place choosed");
            return "flights/search";
        }
        
        String sourceCity = CITY_MAP.getOrDefault(source, source);
        String destinationCity = CITY_MAP.getOrDefault(destination, destination);
        
        model.addAttribute("sourceCity", sourceCity);
        model.addAttribute("destinationCity", destinationCity);
        model.addAttribute("date", date);
        model.addAttribute("passengers", passengers);
        model.addAttribute("flights", flightService.searchFlights(source, destination));
        
        // Proceed to search flights
        // For now, just return results page
        return "flights/results";
    }

    @GetMapping("/flight-details")
    public String flightDetailsPage(@RequestParam Long flightId, 
                                   @RequestParam(required = false) String passengers, 
                                   HttpSession session,
                                   Model model) {
        // Check if user is logged in
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            User user = userService.getUserById(userId);
            model.addAttribute("user", user);
        }
        
        Flight flight = flightService.getFlightById(flightId);
        if (flight != null) {
            model.addAttribute("flight", flight);
        }
        model.addAttribute("passengers", passengers);
        return "flights/flight-details";
    }

    /**
     * API endpoint for filtering flights
     * Accepts: search text, airline filter, status filter, sort by option
     * Returns: JSON list of filtered flights
     */
    @GetMapping("/api/flights/search")
    @ResponseBody
    public Map<String, Object> searchFlightsApi(
            @RequestParam(value = "search", required = false, defaultValue = "") String search,
            @RequestParam(value = "airline", required = false) Long airlineId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "sortBy", required = false, defaultValue = "flight") String sortBy) {
        
        try {
            List<Flight> flights = flightService.searchFlights(search, airlineId, status, sortBy);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", flights.size());
            response.put("flights", flights);
            
            return response;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error searching flights: " + e.getMessage());
            return errorResponse;
        }
    }
    
    /**
     * Advanced API endpoint for filtering flights with source and destination
     * Accepts: search text, airline filter, status filter, source, destination, sort by
     * Returns: JSON list of filtered flights
     */
    @GetMapping("/api/flights/advanced-search")
    @ResponseBody
    public Map<String, Object> advancedSearchFlightsApi(
            @RequestParam(value = "search", required = false, defaultValue = "") String search,
            @RequestParam(value = "airline", required = false) Long airlineId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "destination", required = false) String destination,
            @RequestParam(value = "sortBy", required = false, defaultValue = "flight") String sortBy) {
        
        try {
            List<Flight> flights = flightService.advancedSearchFlights(search, airlineId, status, source, destination, sortBy);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", flights.size());
            response.put("flights", flights);
            
            return response;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error searching flights: " + e.getMessage());
            return errorResponse;
        }
    }
}