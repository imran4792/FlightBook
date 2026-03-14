package com.imran.flightbooking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

   

    @PostMapping("/login")
    public String loginUser() {
        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logoutUser() {
        return "redirect:/";
    }
}