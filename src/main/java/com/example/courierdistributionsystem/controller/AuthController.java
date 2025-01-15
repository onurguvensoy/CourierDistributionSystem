package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(required = false) String error,
                              @RequestParam(required = false) String registered,
                              @RequestParam(required = false) String logout,
                              Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        if (registered != null) {
            model.addAttribute("message", "Registration successful! Please login");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                             @RequestParam String password,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = userRepository.findByUsername(username)
                .orElse(null);
        
        if (user != null && user.getPassword().equals(password)) {
            // Store user information in session
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole().toString());
            return "redirect:/dashboard";
        } else {
            redirectAttributes.addAttribute("error", true);
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String processSignup(@ModelAttribute @Valid User user,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "signup";
        }

        // Check if username already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            result.rejectValue("username", "error.user", "Username is already taken");
            return "signup";
        }

        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            result.rejectValue("email", "error.user", "Email is already registered");
            return "signup";
        }

        // Set default values
        user.setAverageRating(0.0);
        
        // Save the user
        userRepository.save(user);
        
        redirectAttributes.addAttribute("registered", true);
        return "redirect:/auth/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        // Invalidate the session
        session.invalidate();
        redirectAttributes.addAttribute("logout", true);
        return "redirect:/auth/login";
    }
} 