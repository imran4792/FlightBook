package com.imran.flightbooking.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.imran.flightbooking.entity.Flight;
import com.imran.flightbooking.repository.FlightRepository;

@Service
public class FlightService {

    @Autowired
    private FlightRepository flightRepository;

    public Flight addFlight(Flight flight) {
        return flightRepository.save(flight);
    }

    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    public Flight getFlightById(Long id) {
        return flightRepository.findById(id).orElse(null);
    }

    public boolean isSamePlace(String source, String destination) {
        return source != null && source.equals(destination);
    }

    public List<Flight> searchFlights(String source, String destination) {
        return flightRepository.findBySourceAndDestination(source, destination);
    }

    /**
     * Advanced search with all filters
     * @param searchText Search by flight number, airline name, or route
     * @param airlineId Airline ID (optional, can be null)
     * @param status Flight status - "active", "inactive", or null for all
     * @param source Source airport code (optional, can be null)
     * @param destination Destination airport code (optional, can be null)
     * @param sortBy Sorting option: "flight", "price-low", "price-high", "date"
     * @return Filtered and sorted list of flights
     */
    public List<Flight> advancedSearchFlights(
            String searchText, 
            Long airlineId, 
            String status, 
            String source, 
            String destination,
            String sortBy) {
        
        // Trim and normalize search text
        String normalizedSearch = (searchText != null) ? searchText.trim() : "";
        String normalizedStatus = (status != null && !status.isEmpty()) ? status : null;
        String normalizedSource = (source != null && !source.isEmpty()) ? source.trim() : null;
        String normalizedDestination = (destination != null && !destination.isEmpty()) ? destination.trim() : null;
        
        // Fetch flights based on all filters
        List<Flight> flights = flightRepository.advancedSearch(
            normalizedSearch,
            airlineId,
            normalizedStatus,
            normalizedSource,
            normalizedDestination
        );

        // Apply sorting
        if (sortBy != null && !sortBy.isEmpty()) {
            flights = applySorting(flights, sortBy);
        }

        return flights;
    }

    /**
     * Search flights with filters (legacy method - for backward compatibility)
     * @param searchText Search by flight number, airline name, or route
     * @param airlineId Airline ID (optional, can be null)
     * @param status Flight status - "active", "inactive", or null for all
     * @param sortBy Sorting option: "flight", "price-low", "price-high", "date"
     * @return Filtered and sorted list of flights
     */
    public List<Flight> searchFlights(String searchText, Long airlineId, String status, String sortBy) {
        // Trim and normalize search text
        String normalizedSearch = (searchText != null) ? searchText.trim() : "";
        String normalizedStatus = (status != null && !status.isEmpty()) ? status : null;
        
        // Fetch flights based on filters
        List<Flight> flights;
        
        if (!normalizedSearch.isEmpty() || airlineId != null || normalizedStatus != null) {
            // Use combined search if any filter is applied
            flights = flightRepository.searchFlights(
                normalizedSearch,
                airlineId,
                normalizedStatus
            );
        } else {
            // Get all flights if no filters applied
            flights = flightRepository.findAll();
        }

        // Apply sorting
        if (sortBy != null && !sortBy.isEmpty()) {
            flights = applySorting(flights, sortBy);
        }

        return flights;
    }

    /**
     * Apply sorting to flights
     */
    private List<Flight> applySorting(List<Flight> flights, String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "flight":
                return flights.stream()
                        .sorted(Comparator.comparing(Flight::getFlightNumber))
                        .collect(Collectors.toList());
            case "price-low":
                return flights.stream()
                        .sorted(Comparator.comparingDouble(Flight::getPrice))
                        .collect(Collectors.toList());
            case "price-high":
                return flights.stream()
                        .sorted(Comparator.comparingDouble(Flight::getPrice).reversed())
                        .collect(Collectors.toList());
            case "date":
                return flights.stream()
                        .sorted(Comparator.comparing(Flight::getDepartureTime))
                        .collect(Collectors.toList());
            default:
                return flights;
        }
    }
}