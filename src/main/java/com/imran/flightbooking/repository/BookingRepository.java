package com.imran.flightbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.imran.flightbooking.entity.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

}