package com.imran.flightbooking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.imran.flightbooking.entity.Booking;
import com.imran.flightbooking.service.FlightService;
import com.imran.flightbooking.service.BookingService;
import com.imran.flightbooking.service.UserService;
import com.imran.flightbooking.entity.Flight;
import com.imran.flightbooking.entity.User;

import jakarta.servlet.http.HttpSession;

import java.util.Map;

@Controller
public class BookingController {

    @Autowired
    private FlightService flightService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @GetMapping("/passenger-details")
    public String passengerDetailsPage(@RequestParam(required = false) Long flightId, @RequestParam(required = false) String passengers, @RequestParam(required = false) String date, Model model) {
        if (flightId == null) {
            return "redirect:/";
        }
        model.addAttribute("flight", flightService.getFlightById(flightId));
        model.addAttribute("flightId", flightId);
        model.addAttribute("passengers", passengers);
        model.addAttribute("date", date);

        // Extract numeric value from passengers string (e.g., "2 Passengers" -> 2)
        int passengerCount = 1;
        if (passengers != null && !passengers.isEmpty()) {
            try {
                passengerCount = Integer.parseInt(passengers.split(" ")[0]);
            } catch (Exception e) {
                passengerCount = 1;
            }
        }
        model.addAttribute("passengerCount", passengerCount);

        return "booking/passenger-details";
    }

    @PostMapping("/seat-selection")
    public String seatSelectionPage(@RequestParam Map<String, String> allParams, HttpSession session, Model model) {
        // Process passenger data here if needed
        model.addAttribute("passengerData", allParams);
        
        String flightIdStr = allParams.get("flightId");
        if (flightIdStr == null || flightIdStr.trim().isEmpty()) {
            return "redirect:/";
        }
        Long flightId;
        try {
            flightId = Long.parseLong(flightIdStr);
        } catch (NumberFormatException e) {
            return "redirect:/";
        }
        
        Flight flight = flightService.getFlightById(flightId);
        model.addAttribute("flight", flight);
        model.addAttribute("flightId", allParams.get("flightId"));
        model.addAttribute("passengers", allParams.get("passengers"));
        String date = allParams.get("date");
        model.addAttribute("date", date);

        // Store critical flow data in session to avoid data loss between booking pages
        session.setAttribute("bookingFlightId", flightId);
        session.setAttribute("bookingFlightNumber", flight != null ? flight.getFlightNumber() : null);
        session.setAttribute("bookingRoute", flight != null ? flight.getSource() + " to " + flight.getDestination() : null);
        session.setAttribute("bookingDate", date);
        session.setAttribute("bookingPassengers", allParams.get("passengers"));
        session.setAttribute("bookingTotalPrice", allParams.get("totalPrice"));

        // Save the first passenger's name and age so payment and booking success can display them
        String firstPassengerName = null;
        String firstPassengerAge = null;
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (entry.getKey().matches("passenger1?_name")) {
                firstPassengerName = entry.getValue();
            }
            if (entry.getKey().matches("passenger1?_age")) {
                firstPassengerAge = entry.getValue();
            }
        }
        session.setAttribute("bookingPassengerName", firstPassengerName != null ? firstPassengerName : allParams.get("passengerName"));
        session.setAttribute("bookingAge", firstPassengerAge != null ? firstPassengerAge : allParams.get("age"));
        // Extract passenger count
        int passengerCount = 1;
        String passengers = allParams.get("passengers");
        if (passengers != null && !passengers.isEmpty()) {
            try {
                passengerCount = Integer.parseInt(passengers.split(" ")[0]);
            } catch (Exception e) {
                passengerCount = 1;
            }
        }
        model.addAttribute("passengerCount", passengerCount);
        
        // Calculate duration
        if (flight != null && flight.getDepartureTime() != null && flight.getArrivalTime() != null) {
            try {
                String[] depParts = flight.getDepartureTime().split(":");
                String[] arrParts = flight.getArrivalTime().split(":");
                int depHour = Integer.parseInt(depParts[0]);
                int depMin = Integer.parseInt(depParts[1]);
                int arrHour = Integer.parseInt(arrParts[0]);
                int arrMin = Integer.parseInt(arrParts[1]);
                int depTotal = depHour * 60 + depMin;
                int arrTotal = arrHour * 60 + arrMin;
                if (arrTotal < depTotal) arrTotal += 24 * 60; // next day
                int duration = arrTotal - depTotal;
                int hours = duration / 60;
                int mins = duration % 60;
                model.addAttribute("duration", hours + "h " + mins + "m");
            } catch (Exception e) {
                model.addAttribute("duration", "2h 15m"); // fallback
            }
        } else {
            model.addAttribute("duration", "2h 15m");
        }
        
        return "booking/seat-selection";
    }

    @GetMapping("/booking-details")
    public String bookingDetailsPage(@RequestParam Long flightId, @RequestParam(required = false) String passengers, @RequestParam(required = false) String date, HttpSession session, Model model) {
        Flight flight = flightService.getFlightById(flightId);
        if (flight == null) {
            // Create a mock flight for testing purposes
            flight = new Flight();
            flight.setFlightId(flightId);
            flight.setFlightNumber("TEST-" + flightId);
            flight.setSource("Delhi");
            flight.setDestination("Mumbai");
            flight.setDepartureTime("10:00 AM");
            flight.setArrivalTime("12:30 PM");
            flight.setPrice(5000.0);
            model.addAttribute("errorMessage", "Flight not found in database. Using test data for ID: " + flightId);
        }
        model.addAttribute("flight", flight);
        model.addAttribute("passengers", passengers);
        model.addAttribute("date", date);

        // Store date in session for later use
        if (date != null && !date.trim().isEmpty()) {
            session.setAttribute("journeyDate", date);
        }

        // Extract numeric value from passengers string (e.g., "2 Passengers" -> 2)
        int passengerCount = 1;
        if (passengers != null && !passengers.isEmpty()) {
            try {
                passengerCount = Integer.parseInt(passengers.split(" ")[0]);
            } catch (Exception e) {
                passengerCount = 1;
            }
        }
        model.addAttribute("passengerCount", passengerCount);

        return "booking/booking-details";
    }

    @GetMapping("/booking-success")
    public String bookingSuccessPage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            User user = userService.getUserById(userId);
            model.addAttribute("user", user);
        }

        Object lastBookingReference = session.getAttribute("lastBookingReference");
        Object lastBookingDate = session.getAttribute("lastBookingDate");
        Object lastBookingTotal = session.getAttribute("lastBookingTotalPrice");
        Object lastBookingRoute = session.getAttribute("lastBookingRoute");
        Object lastBookingFlight = session.getAttribute("lastBookingFlight");
        Object lastPassengerName = session.getAttribute("lastBookingPassengerName");
        Object lastAge = session.getAttribute("lastBookingAge");

        if (lastBookingReference != null) {
            model.addAttribute("bookingReference", lastBookingReference);
        } else {
            model.addAttribute("bookingReference", "FB-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        model.addAttribute("bookingDate", lastBookingDate != null ? lastBookingDate : session.getAttribute("bookingDate"));
        model.addAttribute("totalPrice", lastBookingTotal != null ? lastBookingTotal : session.getAttribute("bookingTotalPrice"));
        model.addAttribute("route", lastBookingRoute != null ? lastBookingRoute : session.getAttribute("bookingRoute"));
        model.addAttribute("flightNumber", lastBookingFlight != null ? lastBookingFlight : session.getAttribute("bookingFlightNumber"));
        model.addAttribute("passengerName", lastPassengerName != null ? lastPassengerName : session.getAttribute("bookingPassengerName"));
        model.addAttribute("age", lastAge != null ? lastAge : session.getAttribute("bookingAge"));

        return "booking/booking-success";
    }

    @GetMapping("/booking-failed")
    public String bookingFailedPage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            User user = userService.getUserById(userId);
            model.addAttribute("user", user);
        }
        return "booking/booking-failed";
    }

    @PostMapping("/save-booking")
    public String saveBooking(
            @RequestParam Long flightId,
            @RequestParam String passengerName,
            @RequestParam int age,
            @RequestParam double totalPrice,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Get user ID from session
            Long userId = (Long) session.getAttribute("userId");
            
            if (userId == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "You must be logged in to complete a booking");
                return "redirect:/login";
            }
            
            // Validate passenger details
            if (passengerName == null || passengerName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Passenger name is required");
                return "redirect:/booking-failed";
            }
            
            if (age <= 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Invalid age");
                return "redirect:/booking-failed";
            }
            
            // Create and save booking
            Booking booking = bookingService.createBookingFromPassengerDetails(
                    userId, flightId, passengerName, age, totalPrice
            );
            
            // Store booking ID in session for confirmation page
            session.setAttribute("lastBookingId", booking.getBookingId());
            
            redirectAttributes.addFlashAttribute("successMessage", "Booking confirmed successfully");
            return "redirect:/booking-success";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/booking-failed";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while saving the booking");
            return "redirect:/booking-failed";
        }
    }
}