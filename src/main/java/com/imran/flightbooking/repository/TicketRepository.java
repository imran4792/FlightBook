package com.imran.flightbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.imran.flightbooking.entity.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

}