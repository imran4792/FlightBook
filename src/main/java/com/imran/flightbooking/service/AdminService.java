package com.imran.flightbooking.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.imran.flightbooking.entity.Admin;
import com.imran.flightbooking.entity.Flight;
import com.imran.flightbooking.entity.Airline;
import com.imran.flightbooking.repository.AdminRepository;
import com.imran.flightbooking.repository.FlightRepository;
import com.imran.flightbooking.repository.BookingRepository;
import com.imran.flightbooking.repository.UserRepository;

@Service
public class AdminService {

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Flight addFlight(Flight flight) {
        return flightRepository.save(flight);
    }

    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    public Page<Flight> getFilteredFlights(String keyword,
            Long airlineId,
            String source,
            String destination,
            String status,
            String sortBy,
            int page,
            int size) {
        String normalizedKeyword = trimToNull(keyword);
        String normalizedSource = trimToNull(source);
        String normalizedDestination = trimToNull(destination);
        String normalizedStatus = trimToNull(status);
        Pageable pageable = createPageable(sortBy, page, size);
        return flightRepository.findAll(
                buildFilter(
                        normalizedKeyword,
                        airlineId,
                        normalizedSource,
                        normalizedDestination,
                        normalizedStatus),
                pageable);
    }

    private Specification<Flight> buildFilter(
            String keyword,
            Long airlineId,
            String source,
            String destination,
            String status) {
        return (root, query, criteriaBuilder) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            if (trimToNull(keyword) != null) {
                String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("flightNumber")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.join("airline", jakarta.persistence.criteria.JoinType.LEFT).get("airlineName")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("source")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("destination")), likeKeyword)));
            }

            if (airlineId != null) {
                predicates.add(criteriaBuilder.equal(root.join("airline", jakarta.persistence.criteria.JoinType.LEFT).get("airlineId"), airlineId));
            }

            if (trimToNull(source) != null) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("source")), source.trim().toLowerCase()));
            }

            if (trimToNull(destination) != null) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("destination")), destination.trim().toLowerCase()));
            }

            if (trimToNull(status) != null) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("status")), status.trim().toLowerCase()));
            }

            return predicates.isEmpty()
                    ? criteriaBuilder.conjunction()
                    : criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private Pageable createPageable(String sortBy, int page, int size) {
        Sort sort = Sort.by("flightNumber").ascending();
        if (sortBy != null) {
            switch (sortBy) {
                case "price-low":
                    sort = Sort.by("price").ascending();
                    break;
                case "price-high":
                    sort = Sort.by("price").descending();
                    break;
                case "date":
                    sort = Sort.by("departureTime").ascending();
                    break;
                default:
                    sort = Sort.by("flightNumber").ascending();
            }
        }
        return PageRequest.of(Math.max(page, 0), size, sort);
    }

    private String trimToNull(String value) {
        return value != null && !value.trim().isEmpty() ? value.trim() : null;
    }

    public Flight getFlightById(Long id) {
        return flightRepository.findById(id).orElse(null);
    }

    public void updateFlight(Long flightId, String flightNumber, Long airlineId, String source,
                           String destination, String departureTime, String arrivalTime,
                           double price, String status) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new RuntimeException("Flight not found"));

        flight.setFlightNumber(flightNumber.trim());
        flight.setSource(source.trim());
        flight.setDestination(destination.trim());
        flight.setDepartureTime(departureTime.trim());
        flight.setArrivalTime(arrivalTime.trim());
        flight.setPrice(price);

        // Set airline by ID
        Airline airline = new Airline();
        airline.setAirlineId(airlineId);
        flight.setAirline(airline);

        flight.setStatus(status != null ? status.trim() : "active");

        flightRepository.save(flight);
    }

    public void deleteFlight(Long id) {
        flightRepository.deleteById(id);
    }

    // Register Admin
    public Admin registerAdmin(Admin admin) {
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        return adminRepository.save(admin);
    }

    // Authenticate Admin
    public Admin authenticateAdmin(String email, String password) {
        Admin admin = adminRepository.findByEmail(email);
        if (admin != null && passwordEncoder.matches(password, admin.getPassword())) {
            return admin;
        }
        return null;
    }

    // Get Admin by Email
    public Admin getAdminByEmail(String email) {
        return adminRepository.findByEmail(email);
    }
    
    // Get dashboard statistics
    public java.util.Map<String, Object> getDashboardStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalFlights", flightRepository.count());
        stats.put("activeBookings", bookingRepository.countActiveBookings());
        stats.put("totalUsers", userRepository.count());
        stats.put("monthlyRevenue", bookingRepository.getMonthlyRevenue());
        
        // Add booking status counts
        stats.put("confirmedBookings", bookingRepository.countConfirmedBookings());
        stats.put("pendingBookings", bookingRepository.countPendingBookings());
        stats.put("cancelledBookings", bookingRepository.countCancelledBookings());
        stats.put("totalRevenue", bookingRepository.getTotalRevenue());
        
        return stats;
    }
    
    // Get filtered bookings for admin
    public java.util.List<com.imran.flightbooking.entity.Booking> getFilteredBookings(String search, String status, String dateFrom, String dateTo) {
        String bookingReference = null;
        String passengerName = null;
        
        // Parse search term - if it looks like a booking reference (FB-XXXX), search by reference, otherwise by passenger name
        if (search != null && !search.trim().isEmpty()) {
            String trimmedSearch = search.trim();
            if (trimmedSearch.toUpperCase().startsWith("FB")) {
                bookingReference = trimmedSearch;
            } else {
                passengerName = trimmedSearch;
            }
        }
        
        return bookingRepository.findBookingsWithFilters(
            bookingReference,
            passengerName,
            status != null && !status.trim().isEmpty() ? status.toUpperCase() : null,
            dateFrom,
            dateTo
        );
    }
}