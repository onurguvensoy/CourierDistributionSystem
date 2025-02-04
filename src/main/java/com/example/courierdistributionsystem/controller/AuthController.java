package com.example.courierdistributionsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.courierdistributionsystem.service.AuthService;
import com.example.courierdistributionsystem.model.SignupForm;


@Controller
public class AuthController {

    @Autowired
    private AuthService authService;


    @PostMapping("/auth/login")
    public String login(@RequestParam String username,
                       @RequestParam String password,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        Map<String, Object> response = authService.login(username, password);

        if (response.containsKey("error")) {
            redirectAttributes.addFlashAttribute("error", response.get("error"));
            return "redirect:/auth/login";
        }

        // Store user information in session
        session.setAttribute("username", username);
        session.setAttribute("role", response.get("role"));
        session.setAttribute("userId", response.get("userId"));
        session.setAttribute("email", response.get("email"));
        
        if (response.containsKey("phoneNumber")) {
            session.setAttribute("phoneNumber", response.get("phoneNumber"));
        }
        if (response.containsKey("isAvailable")) {
            session.setAttribute("isAvailable", response.get("isAvailable"));
        }
        if (response.containsKey("deliveryAddress")) {
            session.setAttribute("deliveryAddress", response.get("deliveryAddress"));
        }

        return "redirect:/dashboard";
    }

    @PostMapping("/auth/signup")
    public String handleSignup(@ModelAttribute SignupForm signupForm, 
                             RedirectAttributes redirectAttributes) {
        Map<String, String> signupData = new HashMap<>();
        signupData.put("username", signupForm.getUsername());
        signupData.put("email", signupForm.getEmail());
        signupData.put("password", signupForm.getPassword());
        signupData.put("roleType", signupForm.getRoleType());
        signupData.put("phoneNumber", signupForm.getPhoneNumber());
        signupData.put("deliveryAddress", signupForm.getDeliveryAddress());
        signupData.put("vehicleType", signupForm.getVehicleType());
        
        Map<String, String> response = authService.signup(signupData);
        
        if (response.containsKey("error")) {
            redirectAttributes.addFlashAttribute("error", response.get("error"));
            redirectAttributes.addFlashAttribute("signupForm", signupForm);
            return "redirect:/auth/signup";
        }
        
        redirectAttributes.addFlashAttribute("message", "Registration successful! Please login.");
        return "redirect:/auth/login";
    }

    @PostMapping("/auth/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        authService.logoutUser();
        session.invalidate();
        redirectAttributes.addFlashAttribute("message", "You have been logged out successfully");
        return "redirect:/auth/login";
    }

    
}
