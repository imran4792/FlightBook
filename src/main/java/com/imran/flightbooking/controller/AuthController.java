package com.imran.flightbooking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.imran.flightbooking.entity.User;
import com.imran.flightbooking.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping({"/login", "/auth/login"})
    public String loginPage(@RequestParam(name = "error", required = false) String error,
                            @ModelAttribute(name = "successMessage", binding = false) String successMessage,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid login credentials");
        }
        if (successMessage != null && !successMessage.isEmpty()) {
            model.addAttribute("successMessage", successMessage);
        }
        return "auth/login";
    }

    @GetMapping("/error")
    public String errorPage() {
        return "error";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @GetMapping("/auth/register")
    public String authRegisterRedirect() {
        return "redirect:/register";
    }

    @PostMapping("/auth/register")
    public String registerUser(@RequestParam String firstName,
                               @RequestParam String lastName,
                               @RequestParam String email,
                               @RequestParam String phone,
                               @RequestParam String createdDate,
                               @RequestParam String password,
                               @RequestParam String confirmPassword,
                               Model model) {
        try {
            // Validate passwords match
            if (!password.equals(confirmPassword)) {
                model.addAttribute("errorMessage", "Passwords do not match");
                return "auth/register";
            }

            // Check if email already exists
            // For simplicity, assuming no duplicate check for now

            User user = new User();
            user.setFirstName(firstName.trim());
            user.setLastName(lastName.trim());
            user.setEmail(email.trim().toLowerCase());
            user.setPhone(phone.trim());
            user.setCreatedDate(createdDate.trim());
            user.setPassword(passwordEncoder.encode(password.trim()));

            userService.registerUser(user);

            model.addAttribute("successMessage", "User successfully registered");
            return "auth/register";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Registration failed");
            return "auth/register";
        }
    }

    @PostMapping("/auth/login")
    public String loginUser(@RequestParam String email,
                           @RequestParam String password,
                           HttpSession session,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        try {
            String normalizedEmail = email.trim().toLowerCase();
            String normalizedPassword = password.trim();

            User user = userService.authenticateUser(normalizedEmail, normalizedPassword);
            if (user != null) {
                // Store user in session
                session.setAttribute("loggedInUser", user);
                session.setAttribute("userId", user.getUserId());
                session.setAttribute("firstName", user.getFirstName());
                session.setAttribute("lastName", user.getLastName());
                session.setAttribute("email", user.getEmail());
                
                // Credentials are valid, redirect to dashboard with success message
                redirectAttributes.addFlashAttribute("successMessage", "Login Successful");
                return "redirect:/dashboard";
            } else {
                // Invalid credentials
                model.addAttribute("errorMessage", "Wrong credentials, please try again");
                return "auth/login";
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Wrong credentials, please try again");
            return "auth/login";
        }
    }

    @GetMapping("/logout")
    public String logoutUser(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}