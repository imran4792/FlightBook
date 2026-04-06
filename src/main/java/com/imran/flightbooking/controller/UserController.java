package com.imran.flightbooking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;

import com.imran.flightbooking.service.UserService;
import com.imran.flightbooking.service.BookingService;
import com.imran.flightbooking.entity.User;
import com.imran.flightbooking.entity.Booking;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;

@Controller
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private BookingService bookingService;

    @GetMapping("/dashboard")
    public String dashboardPage(HttpSession session, Model model) {
        try {
            // Get user ID from session
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return "redirect:/login";
            }
            
            // Fetch user details
            User user = userService.getUserById(userId);
            if (user == null) {
                session.invalidate();
                return "redirect:/login";
            }
            
            // Calculate dashboard metrics
            Long totalBookings = bookingService.getTotalBookingsCount(userId);
            Long upcomingBookings = bookingService.getUpcomingBookingsCount(userId);
            Long totalFlightHours = bookingService.calculateTotalFlightHours(userId);
            Double averageRating = bookingService.generateRandomRating();
            Booking latestBooking = bookingService.getLatestBooking(userId);
            
            // Add all data to model
            model.addAttribute("user", user);
            model.addAttribute("totalBookings", totalBookings);
            model.addAttribute("destinationsVisited", upcomingBookings);
            model.addAttribute("flightHours", totalFlightHours);
            model.addAttribute("averageRating", averageRating);
            model.addAttribute("latestBooking", latestBooking);
            model.addAttribute("currentDate", java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_DATE));
            
            return "user/dashboard";
        } catch (Exception e) {
            e.printStackTrace();
            return "error/error-page"; // Fallback error page
        }
    }

    @GetMapping("/profile")
    public String profilePage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        User user = userService.getUserById(userId);
        List<Booking> bookings = bookingService.getUserBookings(userId);
        Long totalBookings = bookingService.getTotalBookingsCount(userId);
        
        model.addAttribute("user", user);
        model.addAttribute("bookings", bookings);
        model.addAttribute("totalBookings", totalBookings);
        
        return "user/profile";
    }

    @GetMapping("/edit-profile")
    public String editProfilePage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        User user = userService.getUserById(userId);
        model.addAttribute("user", user);
        
        return "user/edit-profile";
    }

    @GetMapping("/booking-history")
    public String bookingHistoryPage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        List<Booking> bookings = bookingService.getUserBookings(userId);
        model.addAttribute("bookings", bookings);
        
        return "user/booking-history";
    }

    @GetMapping("/booking-details/{bookingId}")
    public String bookingDetailsPage(@PathVariable Long bookingId, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        Booking booking = bookingService.getBookingById(bookingId);
        if (booking == null || !booking.getUserId().equals(userId)) {
            return "redirect:/booking-history"; // or error page
        }
        
        model.addAttribute("booking", booking);
        return "user/ticket-details";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@ModelAttribute("user") @Valid User user, BindingResult result, HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the errors below.");
            return "redirect:/edit-profile";
        }
        
        // Get existing user to preserve password and other fields
        User existingUser = userService.getUserById(userId);
        if (existingUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
            return "redirect:/edit-profile";
        }
        
        // Update fields
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        
        try {
            userService.updateUser(existingUser);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update profile. Please try again.");
        }
        
        return "redirect:/edit-profile";
    }
}