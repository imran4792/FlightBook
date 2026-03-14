package com.imran.flightbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.imran.flightbooking.entity.Aircraft;

@Repository
public interface AircraftRepository extends JpaRepository<Aircraft, Long> 
{
	
}