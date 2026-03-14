package com.imran.flightbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.imran.flightbooking.entity.Seat;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

}