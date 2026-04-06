package com.imran.flightbooking.service;

import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.imran.flightbooking.dto.BookingRequestDto;
import com.imran.flightbooking.entity.Booking;
import com.imran.flightbooking.entity.Flight;
import com.imran.flightbooking.repository.BookingRepository;
import com.imran.flightbooking.repository.FlightRepository;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Transactional
    public Booking createBooking(Booking booking) {
        // Validate required fields
        if (booking == null || booking.getUserId() == null || booking.getFlightId() == null) {
            throw new IllegalArgumentException("User ID and Flight ID are required for booking");
        }

        // Fetch flight details for validation and route information
        Flight flight = flightRepository.findById(booking.getFlightId()).orElse(null);
        if (flight == null) {
            throw new IllegalArgumentException("Flight not found with ID: " + booking.getFlightId());
        }

        // Set booking reference code if not already set
        if (booking.getBookingReference() == null || booking.getBookingReference().trim().isEmpty()) {
            booking.setBookingReference("FB-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        // Set booking date if not already set
        if (booking.getBookingDate() == null || booking.getBookingDate().isEmpty()) {
            LocalDate today = LocalDate.now();
            booking.setBookingDate(today.format(DateTimeFormatter.ISO_DATE));
        }

        // Set status if not already set
        if (booking.getStatus() == null || booking.getStatus().isEmpty()) {
            booking.setStatus("CONFIRMED");
        }

        // Set flight number and route from flight entity
        if (booking.getFlightNumber() == null && flight.getFlightNumber() != null) {
            booking.setFlightNumber(flight.getFlightNumber());
        }

        if (booking.getRoute() == null && flight.getSource() != null && flight.getDestination() != null) {
            booking.setRoute(flight.getSource() + " to " + flight.getDestination());
        }

        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking createBookingFromRequest(BookingRequestDto dto) {
        if (dto == null || dto.getUserId() == null || dto.getFlightId() == null || dto.getPassengerName() == null) {
            throw new IllegalArgumentException("Required booking request fields are missing");
        }

        Booking booking = new Booking();
        booking.setUserId(dto.getUserId());
        booking.setFlightId(dto.getFlightId());
        booking.setPassengerName(dto.getPassengerName());
        booking.setAge(dto.getAge());
        booking.setTotalPrice(dto.getTotalPrice());
        booking.setFlightNumber(dto.getFlightNumber());
        booking.setRoute(dto.getRoute());
        booking.setBookingDate(dto.getTravelDate());

        return createBooking(booking);
    }
    @Transactional
    public Booking createBookingFromPassengerDetails(Long userId, Long flightId, String passengerName, 
                                                     int age, double totalPrice) {
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setFlightId(flightId);
        booking.setPassengerName(passengerName);
        booking.setAge(age);
        booking.setTotalPrice(totalPrice);
        
        return createBooking(booking);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id).orElse(null);
    }

    @Transactional
    public void cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking != null) {
            booking.setStatus("CANCELLED");
            bookingRepository.save(booking);
        }
    }
    
    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId);
    }
    
    @Transactional
    public void updateBookingStatus(Long bookingId, String status) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking != null) {
            booking.setStatus(status);
            bookingRepository.save(booking);
        }
    }
    
    /**
     * Get total count of bookings for a user
     */
    public Long getTotalBookingsCount(Long userId) {
        try {
            Long count = bookingRepository.countByUserId(userId);
            return count != null ? count : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }
    
    /**
     * Get count of upcoming bookings (where booking_date >= today)
     */
    public Long getUpcomingBookingsCount(Long userId) {
        try {
            // Get all bookings and filter by date
            List<Booking> bookings = getUserBookings(userId);
            LocalDate today = LocalDate.now();
            
            long count = 0;
            for (Booking booking : bookings) {
                if (booking.getBookingDate() != null && !booking.getBookingDate().isEmpty()) {
                    try {
                        LocalDate bookingDate = LocalDate.parse(booking.getBookingDate(), DateTimeFormatter.ISO_DATE);
                        if (bookingDate.compareTo(today) >= 0) {
                            count++;
                        }
                    } catch (Exception e) {
                        // Skip if date format is invalid
                    }
                }
            }
            return count;
        } catch (Exception e) {
            return 0L;
        }
    }
    
    /**
     * Calculate total flight hours (assuming 2 hours per booking)
     */
    public Long calculateTotalFlightHours(Long userId) {
        try {
            Long totalBookings = getTotalBookingsCount(userId);
            return totalBookings * 2; // 2 hours per booking
        } catch (Exception e) {
            return 0L;
        }
    }
    
    /**
     * Get the latest booking for a user
     */
    public Booking getLatestBooking(Long userId) {
        try {
            List<Booking> bookings = getUserBookings(userId);
            if (!bookings.isEmpty()) {
                return bookings.get(0); // Already sorted descending by booking date
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Generate a random rating between 3.5 and 5.0
     */
    public Double generateRandomRating() {
        try {
            Random random = new Random();
            // Generate rating between 3.5 and 5.0
            double rating = 3.5 + (random.nextDouble() * 1.5);
            // Round to 1 decimal place
            return Math.round(rating * 10.0) / 10.0;
        } catch (Exception e) {
            return 4.5; // Default rating if error occurs
        }
    }
}