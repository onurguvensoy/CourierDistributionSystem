package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.model.role.Role;
import com.example.courierdistributionsystem.model.role.CustomerRole;
import com.example.courierdistributionsystem.model.role.CourierRole;
import com.example.courierdistributionsystem.repository.UserRepository;
import com.example.courierdistributionsystem.repository.RoleRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;
    
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

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
//layered architecture
    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                             @RequestParam String password,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = userRepository.findByUsername(username)
                .orElse(null);
        
        if (user != null && user.getPassword().equals(hashPassword(password))) {
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole().getRoleName());
            return "redirect:/dashboard";
        } else {
            redirectAttributes.addAttribute("error", true);
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", new String[]{"CUSTOMER", "COURIER"});
        return "signup";
    }

    @PostMapping("/signup")
    public String processSignup(@ModelAttribute @Valid User user,
                              @RequestParam String roleType,
                              @RequestParam(required = false) String deliveryAddress,
                              @RequestParam(required = false) String billingAddress,
                              @RequestParam(required = false) String phoneNumber,
                              @RequestParam(required = false) String vehicleType,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "signup";
        }

        if (userRepository.existsByUsername(user.getUsername())) {
            result.rejectValue("username", "error.user", "Username is already taken");
            return "signup";
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            result.rejectValue("email", "error.user", "Email is already registered");
            return "signup";
        }

        // Create role with specific fields
        Role role;
        if (roleType.equals("CUSTOMER")) {
            CustomerRole customerRole = new CustomerRole();
            customerRole.setDeliveryAddress(deliveryAddress);
            customerRole.setBillingAddress(billingAddress);
            customerRole.setPhoneNumber(phoneNumber);
            role = customerRole;
        } else if (roleType.equals("COURIER")) {
            CourierRole courierRole = new CourierRole();
            courierRole.setVehicleType(vehicleType);
            courierRole.setAverageRating(0.0);
            role = courierRole;
        } else {
            throw new IllegalArgumentException("Invalid role type: " + roleType);
        }
        
        // Save role first
        role = roleRepository.save(role);
        
        // Set the saved role and hashed password
        user.setRole(role);
        user.setPassword(hashPassword(user.getPassword()));
        
        userRepository.save(user);
        
        redirectAttributes.addAttribute("registered", true);
        return "redirect:/auth/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addAttribute("logout", true);
        return "redirect:/auth/login";
    }
}


public interface IDeneme {

}