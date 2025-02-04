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

import java.util.List;


@Controller
public class ViewController {

    @Autowired
    private ViewService viewService;


    @Autowired
    private DashboardService dashboardService;

    @Value("${google.maps.api.key}")
    private String googleMapsApiKey;

    @GetMapping("/")
    public String home(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username != null) {
            return "redirect:/dashboard";
        }
        return "redirect:/auth/login";
    }

    @GetMapping("/auth/login")
    public String showLoginForm(Model model, HttpSession session) {
        if (session.getAttribute("username") != null) {
            return "redirect:/dashboard";
        }
        
        if (model.containsAttribute("error")) {
            model.addAttribute("error", model.getAttribute("error"));
        }
        
        if (model.containsAttribute("message")) {
            model.addAttribute("message", model.getAttribute("message"));
        }
        
        return "login";
    }

    @GetMapping("/auth/signup")
    public String showSignupForm(Model model, HttpSession session) {
        if (session.getAttribute("username") != null) {
            return "redirect:/dashboard";
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
            return "redirect:/auth/login";
        }
        
        try {
            return viewService.getDashboardRedirect(username);
        } catch (RuntimeException e) {
            session.invalidate();
            redirectAttributes.addFlashAttribute("error", "Session expired. Please login again.");
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
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
            session.invalidate();
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/courier/dashboard")
    public String courierDashboard(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
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
            session.invalidate();
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/customer/dashboard")
    public String customerDashboard(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
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
            session.invalidate();
            return "redirect:/auth/login";
        }
    }

    @PostMapping("/courier/delivery/take")
    public String takeDelivery(@RequestParam Long packageId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/auth/login";
        }

        try {
            viewService.takeDeliveryPackage(packageId, username);
            redirectAttributes.addFlashAttribute("message", "Delivery taken successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/courier/dashboard";
    }

    @PostMapping("/courier/delivery/update-status")
    public String updateDeliveryStatus(@RequestParam Long packageId,
                                     @RequestParam String status,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/auth/login";
        }

        try {
            viewService.updateDeliveryStatus(packageId, username, status);
            redirectAttributes.addFlashAttribute("message", "Status updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/courier/dashboard";
    }

    @PostMapping("/courier/delivery/drop")
    public String dropDelivery(@RequestParam Long packageId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/auth/login";
        }

        try {
            viewService.dropDeliveryPackage(packageId, username);
            redirectAttributes.addFlashAttribute("message", "Delivery dropped successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/courier/dashboard";
    }
}