package com.imran.flightbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.imran.flightbooking.entity.Flight;
import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long>, JpaSpecificationExecutor<Flight> {
    List<Flight> findBySourceAndDestination(String source, String destination);
    
    // Search by flight number (case-insensitive)
    @Query("SELECT f FROM Flight f WHERE LOWER(f.flightNumber) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<Flight> searchByFlightNumber(@Param("searchText") String searchText);
    
    // Search by airline name (case-insensitive)
    @Query("SELECT f FROM Flight f WHERE LOWER(f.airline.airlineName) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<Flight> searchByAirline(@Param("searchText") String searchText);
    
    // Search by route (case-insensitive) - source or destination
    @Query("SELECT f FROM Flight f WHERE LOWER(f.source) LIKE LOWER(CONCAT('%', :searchText, '%')) OR LOWER(f.destination) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<Flight> searchByRoute(@Param("searchText") String searchText);
    
    // Filter by airline ID
    List<Flight> findByAirlineAirlineId(Long airlineId);
    
    // Filter by status
    List<Flight> findByStatus(String status);
    
    // Filter by source
    List<Flight> findBySource(String source);
    
    // Filter by destination
    List<Flight> findByDestination(String destination);
    
    // Combined search: text + airline + status (legacy)
    @Query("SELECT f FROM Flight f WHERE " +
           "(LOWER(f.flightNumber) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(f.airline.airlineName) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(f.source) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(f.destination) LIKE LOWER(CONCAT('%', :searchText, '%'))) AND " +
           "(:airlineId IS NULL OR f.airline.airlineId = :airlineId) AND " +
           "(:status IS NULL OR f.status = :status)")
    List<Flight> searchFlights(
        @Param("searchText") String searchText,
        @Param("airlineId") Long airlineId,
        @Param("status") String status);
    
    /**
     * Advanced search with all filters: text, airline, status, source, destination
     */
    @Query("SELECT f FROM Flight f WHERE " +
           "(LOWER(f.flightNumber) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(f.airline.airlineName) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(f.source) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(f.destination) LIKE LOWER(CONCAT('%', :searchText, '%'))) AND " +
           "(:airlineId IS NULL OR f.airline.airlineId = :airlineId) AND " +
           "(:status IS NULL OR f.status = :status) AND " +
           "(:source IS NULL OR f.source = :source) AND " +
           "(:destination IS NULL OR f.destination = :destination)")
    List<Flight> advancedSearch(
        @Param("searchText") String searchText,
        @Param("airlineId") Long airlineId,
        @Param("status") String status,
        @Param("source") String source,
        @Param("destination") String destination);
}