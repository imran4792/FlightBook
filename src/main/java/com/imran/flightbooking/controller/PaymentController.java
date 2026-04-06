package com.imran.flightbooking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.imran.flightbooking.entity.Payment;
import com.imran.flightbooking.entity.Booking;
import com.imran.flightbooking.service.PaymentService;
import com.imran.flightbooking.service.BookingService;
import com.imran.flightbooking.service.UserService;
import com.imran.flightbooking.entity.User;

import jakarta.servlet.http.HttpSession;

@Controller
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @GetMapping("/payment-page")
    public String paymentPage() {
        return "payment/payment";
    }

    @GetMapping("/payment")
    public String payment(HttpSession session, org.springframework.ui.Model model) {
        // Keep user info for header (and optionally page display)
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            User user = userService.getUserById(userId);
            model.addAttribute("user", user);
        }

        // Retrieve booking data from session (carried from previous pages)
        Object flightId = session.getAttribute("bookingFlightId");
        Object flightNumber = session.getAttribute("bookingFlightNumber");
        Object route = session.getAttribute("bookingRoute");
        Object travelDate = session.getAttribute("bookingDate");
        Object passengerName = session.getAttribute("bookingPassengerName");
        Object age = session.getAttribute("bookingAge");
        Object totalPrice = session.getAttribute("bookingTotalPrice");

        model.addAttribute("flightId", flightId);
        model.addAttribute("flightNumber", flightNumber);
        model.addAttribute("route", route);
        model.addAttribute("travelDate", travelDate);
        model.addAttribute("passengerName", passengerName);
        model.addAttribute("age", age);
        model.addAttribute("totalPrice", totalPrice);

        return "booking/payment";
    }

    @PostMapping("/process-payment")
    public String processPayment(
            @RequestParam String cardHolderName,
            @RequestParam String cardNumber,
            @RequestParam String expiryDate,
            @RequestParam String cvv,
            @RequestParam(required = false) Long flightId,
            @RequestParam(required = false) String passengerName,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) Double totalPrice,
            @RequestParam(required = false) String flightNumber,
            @RequestParam(required = false) String route,
            @RequestParam(required = false) String travelDate,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            Payment payment = new Payment();
            payment.setPaymentMethod("CREDIT_CARD");
            
            // Process payment
            boolean isSuccessful = paymentService.processPaymentTransaction(payment);
            
            if (isSuccessful) {
                Long userId = (Long) session.getAttribute("userId");
                if (userId == null) {
                    redirectAttributes.addFlashAttribute("errorMessage", "You must be logged in to complete a booking");
                    return "redirect:/login";
                }

                // Prefer incoming form values; fallback to session values in case of missing values
                Long bookingFlightId = flightId != null ? flightId : (Long) session.getAttribute("bookingFlightId");
                String bookingFlightNumber = flightNumber != null ? flightNumber : (String) session.getAttribute("bookingFlightNumber");
                String bookingRoute = route != null ? route : (String) session.getAttribute("bookingRoute");
                String bookingDate = travelDate != null ? travelDate : (String) session.getAttribute("bookingDate");
                String bookingPassengerName = passengerName != null ? passengerName : (String) session.getAttribute("bookingPassengerName");

                Integer bookingAge = age;
                if (bookingAge == null) {
                    Object sessionAge = session.getAttribute("bookingAge");
                    if (sessionAge instanceof Integer) {
                        bookingAge = (Integer) sessionAge;
                    } else if (sessionAge instanceof String) {
                        try {
                            bookingAge = Integer.parseInt((String) sessionAge);
                        } catch (NumberFormatException ex) {
                            bookingAge = null;
                        }
                    }
                }

                Double bookingTotalPrice = totalPrice;
                if (bookingTotalPrice == null) {
                    Object sessionTotalPrice = session.getAttribute("bookingTotalPrice");
                    if (sessionTotalPrice instanceof Double) {
                        bookingTotalPrice = (Double) sessionTotalPrice;
                    } else if (sessionTotalPrice instanceof String) {
                        try {
                            bookingTotalPrice = Double.parseDouble((String) sessionTotalPrice);
                        } catch (NumberFormatException ex) {
                            bookingTotalPrice = null;
                        }
                    }
                }

                if (bookingFlightId == null || bookingPassengerName == null || bookingAge == null || bookingTotalPrice == null) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Booking information is incomplete, please retry the flow.");
                    return "redirect:/booking-failed";
                }

                try {
                    com.imran.flightbooking.dto.BookingRequestDto requestDto = new com.imran.flightbooking.dto.BookingRequestDto();
                    requestDto.setUserId(userId);
                    requestDto.setFlightId(bookingFlightId);
                    requestDto.setPassengerName(bookingPassengerName);
                    requestDto.setAge(bookingAge);
                    requestDto.setTotalPrice(bookingTotalPrice);
                    requestDto.setFlightNumber(bookingFlightNumber);
                    requestDto.setRoute(bookingRoute);
                    requestDto.setTravelDate(bookingDate);

                    com.imran.flightbooking.entity.Booking savedBooking = bookingService.createBookingFromRequest(requestDto);

                    // Save the booking reference and useful info for success screen
                    session.setAttribute("lastBookingReference", savedBooking.getBookingReference());
                    session.setAttribute("lastBookingId", savedBooking.getBookingId());
                    session.setAttribute("lastBookingDate", savedBooking.getBookingDate());
                    session.setAttribute("lastBookingTotalPrice", savedBooking.getTotalPrice());
                    session.setAttribute("lastBookingRoute", savedBooking.getRoute());
                    session.setAttribute("lastBookingFlight", savedBooking.getFlightNumber());
                    session.setAttribute("lastBookingPassengerName", savedBooking.getPassengerName());
                    session.setAttribute("lastBookingAge", savedBooking.getAge());

                    redirectAttributes.addFlashAttribute("successMessage", "Payment successful and booking confirmed");
                    return "redirect:/booking-success";
                } catch (IllegalArgumentException e) {
                    redirectAttributes.addFlashAttribute("warningMessage", "Payment successful but booking could not be saved: " + e.getMessage());
                    return "redirect:/booking-failed";
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while saving booking after payment.");
                    return "redirect:/booking-failed";
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Payment processing failed");
                return "redirect:/booking-failed";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred during payment processing");
            return "redirect:/booking-failed";
        }
    }

    @GetMapping("/payment-success")
    public String paymentSuccessPage() {
        return "payment/success";
    }

    @GetMapping("/payment-failed")
    public String paymentFailedPage() {
        return "payment/failed";
    }
}