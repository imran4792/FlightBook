package com.imran.flightbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.imran.flightbooking.entity.Airport;
import java.util.List;

@Repository
public interface AirportRepository extends JpaRepository<Airport, Long> {
    
    /**
     * Find all unique airports that are used as source or destination in flights
     */
    @Query("SELECT DISTINCT a FROM Airport a WHERE " +
           "EXISTS (SELECT 1 FROM Flight f WHERE f.source = a.iataCode) OR " +
           "EXISTS (SELECT 1 FROM Flight f WHERE f.destination = a.iataCode)")
    List<Airport> findAllAirportsWithFlights();
    
    /**
     * Find airport by IATA code
     */
    Airport findByIataCode(String iataCode);
    
    /**
     * Find airport by city name
     */
    List<Airport> findByCity(String city);
}