package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.model.Package;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.repository.PackageRepository;
import com.example.courierdistributionsystem.repository.UserRepository;
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
    private UserRepository userRepository;

    @Autowired
    private PackageRepository packageRepository;

    @Value("${google.maps.api.key}")
    private String googleMapsApiKey;

    @GetMapping("/")
    public String home() {
        return "redirect:/auth/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/auth/login";
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        switch (user.getRole().getRoleName()) {
            case "ADMIN":
                return "redirect:/admin/dashboard";
            case "COURIER":
                return "redirect:/courier/dashboard";
            case "CUSTOMER":
                return "redirect:/customer/dashboard";
            default:
                return "redirect:/auth/login";
        }
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/auth/login";
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getRole().getRoleName().equals("ADMIN")) {
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

        User courier = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!courier.getRole().getRoleName().equals("COURIER")) {
            return "redirect:/dashboard";
        }

        List<Package> availablePackages = packageRepository.findByStatus(Package.PackageStatus.PENDING);

        List<Package> activeDeliveries = packageRepository.findByCourierAndStatusIn(
            courier, 
            List.of(Package.PackageStatus.ASSIGNED, Package.PackageStatus.PICKED_UP)
        );

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

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getRole().getRoleName().equals("CUSTOMER")) {
            return "redirect:/dashboard";
        }

        List<Package> myPackages = packageRepository.findByCustomer(user);

        model.addAttribute("user", user);
        model.addAttribute("myPackages", myPackages);
        model.addAttribute("googleMapsApiKey", googleMapsApiKey);
        return "customer_dashboard";
    }
} 