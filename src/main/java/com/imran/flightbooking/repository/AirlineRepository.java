package com.imran.flightbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.imran.flightbooking.entity.Airline;
import java.util.List;

@Repository
public interface AirlineRepository extends JpaRepository<Airline, Long> {
    
    /**
     * Find all unique airlines that have flights
     */
    @Query("SELECT DISTINCT a FROM Airline a WHERE EXISTS (SELECT 1 FROM Flight f WHERE f.airline = a)")
    List<Airline> findAllAirlinesWithFlights();
    
    /**
     * Find airline by name
     */
    Airline findByAirlineName(String airlineName);
}