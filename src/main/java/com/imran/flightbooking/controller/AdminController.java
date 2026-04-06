package com.imran.flightbooking.controller;

import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.imran.flightbooking.entity.Admin;
import com.imran.flightbooking.entity.Airline;
import com.imran.flightbooking.entity.Flight;
import com.imran.flightbooking.service.AdminService;
import com.imran.flightbooking.repository.AirportRepository;
import com.imran.flightbooking.repository.AirlineRepository;
import com.imran.flightbooking.repository.UserRepository;
import com.imran.flightbooking.repository.BookingRepository;

@Controller
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AirportRepository airportRepository;
    
    @Autowired
    private AirlineRepository airlineRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping("/admin/login")
    public String adminLoginPage() {
        return "admin/admin-login";
    }

    @PostMapping("/admin/login")
    public String adminLogin(@RequestParam String email,
                            @RequestParam String password,
                            HttpSession session,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        try {
            String normalizedEmail = email.trim().toLowerCase();
            String normalizedPassword = password.trim();

            Admin admin = adminService.authenticateAdmin(normalizedEmail, normalizedPassword);
            if (admin != null) {
                session.setAttribute("admin", admin);
                session.setAttribute("adminEmail", admin.getEmail());
                session.setAttribute("adminName", admin.getName());
                redirectAttributes.addFlashAttribute("successMessage", "Login Successful");
                return "redirect:/admin/dashboard";
            } else {
                model.addAttribute("errorMessage", "Invalid admin credentials");
                return "admin/admin-login";
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Login failed: " + e.getMessage());
            return "admin/admin-login";
        }
    }
    
    @GetMapping("/admin/register")
    public String adminRegisterPage() {
        return "admin/admin-register";
    }

    @PostMapping("/admin/register")
    public String adminRegister(@RequestParam String name,
                               @RequestParam String email,
                               @RequestParam String password,
                               @RequestParam String confirmPassword,
                               Model model) {
        try {
            if (!password.equals(confirmPassword)) {
                model.addAttribute("errorMessage", "Passwords do not match");
                return "admin/admin-register";
            }

            Admin existingAdmin = adminService.getAdminByEmail(email.trim().toLowerCase());
            if (existingAdmin != null) {
                model.addAttribute("errorMessage", "Email already registered");
                return "admin/admin-register";
            }

            Admin admin = new Admin();
            admin.setName(name.trim());
            admin.setEmail(email.trim().toLowerCase());
            admin.setPassword(password.trim());

            adminService.registerAdmin(admin);

            model.addAttribute("successMessage", "Admin registered successfully. Please login.");
            return "admin/admin-login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Registration failed: " + e.getMessage());
            return "admin/admin-register";
        }
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboardPage(HttpSession session, Model model) {
        String adminName = (String) session.getAttribute("adminName");
        if (adminName != null) {
            model.addAttribute("adminName", adminName);
        }
        
        // Add dashboard statistics
        java.util.Map<String, Object> stats = adminService.getDashboardStats();
        model.addAllAttributes(stats);
        
        return "admin/admin-dashboard";
    }

    @GetMapping("/admin/add-flight")
    public String addFlightPage(Model model) {
        model.addAttribute("airports", airportRepository.findAll());
        return "admin/add-flight";
    }

    @PostMapping("/admin/add-flight")
    public String addFlight(@RequestParam String flightNumber,
                           @RequestParam String source,
                           @RequestParam String destination,
                           @RequestParam String departureTime,
                           @RequestParam String arrivalTime,
                           @RequestParam double price,
                           @RequestParam Long airlineId,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        try {
            Flight flight = new Flight();
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

            adminService.addFlight(flight);

            redirectAttributes.addFlashAttribute("successMessage", "Flight added successfully!");
            return "redirect:/admin/manage-flights";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to add flight: " + e.getMessage());
            return "admin/add-flight";
        }
    }

    @GetMapping("/admin/manage-flights")
    public String manageFlightsPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long airlineId,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "flight") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        page = Math.max(0, page);
        Page<Flight> flightPage = adminService.getFilteredFlights(keyword, airlineId, source, destination, status, sortBy, page, size);
        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = 1; i <= flightPage.getTotalPages(); i++) {
            pageNumbers.add(i);
        }

        model.addAttribute("flightPage", flightPage);
        model.addAttribute("flights", flightPage.getContent());
        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);
        model.addAttribute("airlineId", airlineId);
        model.addAttribute("source", source);
        model.addAttribute("destination", destination);
        model.addAttribute("status", status);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("pageSize", size);
        model.addAttribute("airlines", airlineRepository.findAllAirlinesWithFlights());
        model.addAttribute("airports", airportRepository.findAllAirportsWithFlights());
        return "admin/manage-flights";
    }

    @GetMapping("/admin/manage-bookings")
    public String manageBookingsPage(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            Model model) {
        
        // Get booking statistics
        java.util.Map<String, Object> stats = adminService.getDashboardStats();
        model.addAllAttributes(stats);
        
        // Get filtered bookings
        java.util.List<com.imran.flightbooking.entity.Booking> bookings = adminService.getFilteredBookings(search, status, dateFrom, dateTo);
        
        // Display all bookings from the booking table
        model.addAttribute("bookings", bookings);
        model.addAttribute("totalBookings", bookings.size());
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        
        return "admin/manage-bookings";
    }

    @GetMapping("/admin/manage-users")
    public String manageUsersPage(Model model) {
        // Fetch all users from database
        java.util.List<com.imran.flightbooking.entity.User> users = userRepository.findAllUsers();
        
        // Add booking count to each user using a map for Thymeleaf
        java.util.Map<Long, Long> userBookingCounts = new java.util.HashMap<>();
        // Generate user initials (moved from template to controller to avoid Thymeleaf limitations with method calls)
        java.util.Map<Long, String> userInitials = new java.util.HashMap<>();
        
        for (com.imran.flightbooking.entity.User user : users) {
            Long bookingCount = bookingRepository.countByUserId(user.getUserId());
            userBookingCounts.put(user.getUserId(), bookingCount != null ? bookingCount : 0L);
            
            // Generate initials from firstName and lastName
            String initials = "";
            if (user.getFirstName() != null && !user.getFirstName().isEmpty()) {
                initials += user.getFirstName().charAt(0);
            }
            if (user.getLastName() != null && !user.getLastName().isEmpty()) {
                initials += user.getLastName().charAt(0);
            }
            userInitials.put(user.getUserId(), initials.toUpperCase());
        }
        
        // Calculate status counts (moved from template to controller to avoid SpEL limitations)
        long activeUsersCount = users.stream()
            .filter(u -> u.getStatus() != null && u.getStatus().equals("active"))
            .count();
        long inactiveUsersCount = users.stream()
            .filter(u -> u.getStatus() != null && u.getStatus().equals("inactive"))
            .count();
        long totalUsersCount = users.size();
        
        model.addAttribute("users", users);
        model.addAttribute("userBookingCounts", userBookingCounts);
        model.addAttribute("userInitials", userInitials);
        model.addAttribute("activeUsersCount", activeUsersCount);
        model.addAttribute("inactiveUsersCount", inactiveUsersCount);
        model.addAttribute("totalUsersCount", totalUsersCount);
        return "admin/manage-users";
    }

    @DeleteMapping("/admin/delete-user/{id}")
    @ResponseBody
    public String deleteUser(@PathVariable Long id) {
        try {
            userRepository.deleteById(id);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    @GetMapping("/admin/user-details/{id}")
    public String userDetailsPage(@PathVariable Long id, Model model) {
        com.imran.flightbooking.entity.User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            Long bookingCount = bookingRepository.countByUserId(id);
            model.addAttribute("user", user);
            model.addAttribute("bookingCount", bookingCount != null ? bookingCount : 0L);
        }
        return "admin/user-details";
    }

    @GetMapping("/admin/edit-user/{id}")
    public String editUserPage(@PathVariable Long id, Model model) {
        com.imran.flightbooking.entity.User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            model.addAttribute("user", user);
        }
        return "admin/edit-user";
    }

    @PostMapping("/admin/edit-user/{id}")
    public String updateUser(@PathVariable Long id,
                            @RequestParam String firstName,
                            @RequestParam String lastName,
                            @RequestParam String email,
                            @RequestParam String phone,
                            @RequestParam String status,
                            RedirectAttributes redirectAttributes) {
        try {
            com.imran.flightbooking.entity.User user = userRepository.findById(id).orElse(null);
            if (user != null) {
                user.setFirstName(firstName.trim());
                user.setLastName(lastName.trim());
                user.setEmail(email.trim().toLowerCase());
                user.setPhone(phone.trim());
                user.setStatus(status);
                userRepository.save(user);
                redirectAttributes.addFlashAttribute("successMessage", "User updated successfully!");
                return "redirect:/admin/manage-users";
            }
            redirectAttributes.addFlashAttribute("errorMessage", "User not found");
            return "redirect:/admin/manage-users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update user: " + e.getMessage());
            return "redirect:/admin/manage-users";
        }
    }

    @GetMapping("/admin/edit-flight/{id}")
    public String editFlightPage(@PathVariable String id, Model model) {
        try {
            Long flightId = Long.parseLong(id);
            Flight flight = adminService.getFlightById(flightId);
            if (flight == null) {
                model.addAttribute("errorMessage", "Flight not found");
                return "redirect:/admin/manage-flights";
            }
            model.addAttribute("flight", flight);
            model.addAttribute("airlines", airlineRepository.findAllAirlinesWithFlights());
            model.addAttribute("airports", airportRepository.findAllAirportsWithFlights());
            return "admin/edit-flight";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Invalid flight ID");
            return "redirect:/admin/manage-flights";
        }
    }

    @PostMapping("/admin/edit-flight/{id}")
    public String updateFlight(@PathVariable String id,
                              @RequestParam String flightNumber,
                              @RequestParam Long airlineId,
                              @RequestParam String source,
                              @RequestParam String destination,
                              @RequestParam String departureTime,
                              @RequestParam String arrivalTime,
                              @RequestParam double price,
                              @RequestParam(required = false, defaultValue = "active") String status,
                              RedirectAttributes redirectAttributes) {
        try {
            Long flightId = Long.parseLong(id);
            adminService.updateFlight(flightId, flightNumber, airlineId, source, destination,
                                    departureTime, arrivalTime, price, status);
            redirectAttributes.addFlashAttribute("successMessage", "Flight updated successfully!");
            return "redirect:/admin/manage-flights";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update flight: " + e.getMessage());
            return "redirect:/admin/edit-flight/" + id;
        }
    }

    @GetMapping("/admin/booking-details/{id}")
    public String bookingDetailsPage(@PathVariable String id, Model model) {
        // Try to find booking by bookingReference first, then by ID if not found
        com.imran.flightbooking.entity.Booking booking = bookingRepository.findByBookingReference(id);
        if (booking == null) {
            try {
                Long bookingId = Long.parseLong(id);
                booking = bookingRepository.findById(bookingId).orElse(null);
            } catch (NumberFormatException e) {
                // ID is not a number, continue with null
            }
        }
        
        if (booking != null) {
            model.addAttribute("booking", booking);
            model.addAttribute("bookingId", booking.getBookingReference());
        } else {
            model.addAttribute("bookingId", id);
            model.addAttribute("errorMessage", "Booking not found");
        }
        return "admin/booking-details";
    }

    @PostMapping("/admin/cancel-booking/{id}")
    @ResponseBody
    public String cancelBooking(@PathVariable String id) {
        try {
            // Find booking by reference or ID
            com.imran.flightbooking.entity.Booking booking = bookingRepository.findByBookingReference(id);
            if (booking == null) {
                try {
                    Long bookingId = Long.parseLong(id);
                    booking = bookingRepository.findById(bookingId).orElse(null);
                } catch (NumberFormatException e) {
                    return "error: Invalid booking ID";
                }
            }
            
            if (booking == null) {
                return "error: Booking not found";
            }
            
            if ("CANCELLED".equals(booking.getStatus()) || "COMPLETED".equals(booking.getStatus())) {
                return "error: Cannot cancel a " + booking.getStatus().toLowerCase() + " booking";
            }
            
            booking.setStatus("CANCELLED");
            bookingRepository.save(booking);
            
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
}