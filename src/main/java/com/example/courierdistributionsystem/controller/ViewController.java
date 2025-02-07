package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.model.SignupForm;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.service.ViewService;
import com.example.courierdistributionsystem.service.DashboardService;
import com.example.courierdistributionsystem.dto.CustomerDashboardDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ViewController {
    private static final Logger logger = LoggerFactory.getLogger(ViewController.class);

    @Autowired
    private ViewService viewService;

    @Autowired
    private DashboardService dashboardService;

    @Value("${google.maps.api.key}")
    private String googleMapsApiKey;

    @GetMapping("/")
    public String home(HttpSession session, RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("username");
        if (username != null) {
            try {
                return viewService.getDashboardRedirect(username);
            } catch (Exception e) {
                logger.error("Error redirecting from home: {}", e.getMessage());
                session.invalidate();
                redirectAttributes.addFlashAttribute("error", "Session expired. Please login again.");
                return "redirect:/auth/login";
            }
        }
        return "redirect:/auth/login";
    }

    @GetMapping("/auth/login")
    public String showLoginForm(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("username");
        if (username != null) {
            try {
                return viewService.getDashboardRedirect(username);
            } catch (Exception e) {
                logger.error("Error redirecting from login: {}", e.getMessage());
                session.invalidate();
            }
        }
        
        return "login";
    }

    @GetMapping("/auth/signup")
    public String showSignupForm(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("username");
        if (username != null) {
            try {
                return viewService.getDashboardRedirect(username);
            } catch (Exception e) {
                logger.error("Error redirecting from signup: {}", e.getMessage());
                session.invalidate();
            }
        }
        
        if (!model.containsAttribute("signupForm")) {
            model.addAttribute("signupForm", new SignupForm());
        }
   
        return "signup";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to continue.");
            return "redirect:/auth/login";
        }
        
        try {
            return viewService.getDashboardRedirect(username);
        } catch (RuntimeException e) {
            logger.error("Error redirecting to dashboard: {}", e.getMessage());
            session.invalidate();
            redirectAttributes.addFlashAttribute("error", "Session expired. Please login again.");
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to continue.");
            return "redirect:/auth/login";
        }

        try {
            User user = viewService.getUserByUsername(username);
            if (!viewService.isValidRole(user, "ADMIN")) {
                return viewService.getDashboardRedirect(username);
            }

            model.addAttribute("user", user);
            model.addAttribute("googleMapsApiKey", googleMapsApiKey);
            return "admin_dashboard";
        } catch (RuntimeException e) {
            logger.error("Error accessing admin dashboard: {}", e.getMessage());
            session.invalidate();
            redirectAttributes.addFlashAttribute("error", "Session expired. Please login again.");
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/courier/dashboard")
    public String courierDashboard(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to continue.");
            return "redirect:/auth/login";
        }

        try {
            User courier = viewService.getUserByUsername(username);
            if (!viewService.isValidRole(courier, "COURIER")) {
                return viewService.getDashboardRedirect(username);
            }

            List<DeliveryPackage> availablePackages = viewService.getAvailablePackages();
            List<DeliveryPackage> activeDeliveries = viewService.getActiveDeliveries(courier);

            model.addAttribute("user", courier);
            model.addAttribute("availablePackages", availablePackages);
            model.addAttribute("activeDeliveries", activeDeliveries);
            model.addAttribute("googleMapsApiKey", googleMapsApiKey);
            return "courier_dashboard";
        } catch (RuntimeException e) {
            logger.error("Error accessing courier dashboard: {}", e.getMessage());
            session.invalidate();
            redirectAttributes.addFlashAttribute("error", "Session expired. Please login again.");
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/customer/dashboard")
    public String customerDashboard(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to continue.");
            return "redirect:/auth/login";
        }

        try {
            User user = viewService.getUserByUsername(username);
            if (!viewService.isValidRole(user, "CUSTOMER")) {
                return viewService.getDashboardRedirect(username);
            }

            CustomerDashboardDTO dashboardData = dashboardService.getCustomerDashboard(user);
            model.addAttribute("user", user);
            model.addAttribute("activePackages", dashboardData.getActivePackages());
            model.addAttribute("completedPackages", dashboardData.getCompletedPackages());
            model.addAttribute("stats", dashboardData.getStats());
            model.addAttribute("googleMapsApiKey", googleMapsApiKey);
            
            return "customer_dashboard";
        } catch (RuntimeException e) {
            logger.error("Error accessing customer dashboard: {}", e.getMessage());
            session.invalidate();
            redirectAttributes.addFlashAttribute("error", "Session expired. Please login again.");
            return "redirect:/auth/login";
        }
    }
}