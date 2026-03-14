package com.imran.flightbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.imran.flightbooking.entity.Passenger;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {

}