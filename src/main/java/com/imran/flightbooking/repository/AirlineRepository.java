package com.imran.flightbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.imran.flightbooking.entity.Airline;

@Repository
public interface AirlineRepository extends JpaRepository<Airline, Long> {

}