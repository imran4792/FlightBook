package com.imran.flightbooking.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.imran.flightbooking.entity.Flight;
import com.imran.flightbooking.repository.FlightRepository;

@Service
public class AdminService {

    @Autowired
    private FlightRepository flightRepository;

    public Flight addFlight(Flight flight) {
        return flightRepository.save(flight);
    }

    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    public void deleteFlight(Long id) {
        flightRepository.deleteById(id);
    }
}