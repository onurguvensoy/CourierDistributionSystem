package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.model.Package;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.service.ViewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/")
public class ViewController {

    @Autowired
    private ViewService viewService;

    @Value("${google.maps.api.key}")
    private String googleMapsApiKey;

    @GetMapping
    public String home(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username != null) {
            return "redirect:/dashboard";
        }
        return "redirect:/auth/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/auth/login";
        }
        return viewService.getDashboardRedirect(username);
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/auth/login";
        }

        User user = viewService.getUserByUsername(username);
        if (!viewService.isValidRole(user, "ADMIN")) {
            return "redirect:/dashboard";
        }

        model.addAttribute("user", user);
        return "admin_dashboard";
    }

    @GetMapping("/courier/dashboard")
    public String courierDashboard(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/auth/login";
        }

        User courier = viewService.getUserByUsername(username);
        if (!viewService.isValidRole(courier, "COURIER")) {
            return "redirect:/dashboard";
        }

        List<Package> availablePackages = viewService.getAvailablePackages();
        List<Package> activeDeliveries = viewService.getActiveDeliveries(courier);

        model.addAttribute("user", courier);
        model.addAttribute("availablePackages", availablePackages);
        model.addAttribute("activeDeliveries", activeDeliveries);
        model.addAttribute("googleMapsApiKey", googleMapsApiKey);
        return "courier_dashboard";
    }

    @GetMapping("/customer/dashboard")
    public String customerDashboard(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/auth/login";
        }

        User user = viewService.getUserByUsername(username);
        if (!viewService.isValidRole(user, "CUSTOMER")) {
            return "redirect:/dashboard";
        }

        List<Package> myPackages = viewService.getCustomerPackages(user);

        model.addAttribute("user", user);
        model.addAttribute("myPackages", myPackages);
        model.addAttribute("googleMapsApiKey", googleMapsApiKey);
        return "customer_dashboard";
    }
} 