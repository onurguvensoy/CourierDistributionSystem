package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.model.SignupForm;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.model.Customer;
import com.example.courierdistributionsystem.model.Courier;
import com.example.courierdistributionsystem.model.Rating;
import com.example.courierdistributionsystem.service.ViewService;
import com.example.courierdistributionsystem.service.DashboardService;
import com.example.courierdistributionsystem.dto.CustomerDashboardDTO;
import com.example.courierdistributionsystem.service.UserService;
import com.example.courierdistributionsystem.service.DeliveryPackageService;
import com.example.courierdistributionsystem.model.DeliveryReport;
import com.example.courierdistributionsystem.service.DeliveryReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Controller
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ViewController {
    private static final Logger logger = LoggerFactory.getLogger(ViewController.class);

    @Autowired
    private ViewService viewService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserService userService;

    @Autowired
    private DeliveryPackageService deliveryPackageService;

    @Autowired
    private DeliveryReportService deliveryReportService;

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
    public String showLoginForm(Model model, HttpSession session) {
        return "login";
    }

    @GetMapping("/auth/signup")
    public String showSignupForm(Model model, HttpSession session) {
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

            // Get all users
            List<User> allUsers = userService.getAllUsers();
            
            // Get all packages
            List<DeliveryPackage> allPackages = deliveryPackageService.getAllDeliveryPackages();
            
            // Get active couriers (available)
            long activeCouriers = allUsers.stream()
                    .filter(u -> u.getRole() == User.UserRole.COURIER)
                    .filter(u -> ((Courier) u).isAvailable())
                    .count();
            
            // Get pending packages
            long pendingPackages = allPackages.stream()
                    .filter(p -> p.getStatus() == DeliveryPackage.DeliveryStatus.PENDING)
                    .count();
            
            // Get total deliveries
            long totalDeliveries = allPackages.stream()
                    .filter(p -> p.getStatus() == DeliveryPackage.DeliveryStatus.DELIVERED)
                    .count();

            // Add data to model
            model.addAttribute("user", user);
            model.addAttribute("users", allUsers);
            model.addAttribute("packages", allPackages);
            model.addAttribute("totalUsers", allUsers.size());
            model.addAttribute("activeCouriers", activeCouriers);
            model.addAttribute("pendingPackages", pendingPackages);
            model.addAttribute("totalDeliveries", totalDeliveries);

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
            return "customer_dashboard";
        } catch (RuntimeException e) {
            logger.error("Error accessing customer dashboard: {}", e.getMessage());
            session.invalidate();
            redirectAttributes.addFlashAttribute("error", "Session expired. Please login again.");
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/customer/new-package")
    public String newPackage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
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

            model.addAttribute("user", user);
            return "new_package";
        } catch (RuntimeException e) {
            logger.error("Error accessing new package page: {}", e.getMessage());
            session.invalidate();
            redirectAttributes.addFlashAttribute("error", "Session expired. Please login again.");
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/profile")
    @Transactional(readOnly = true)
    public String showProfile(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("username");
        logger.debug("Profile access attempt - Username from session: {}", username);
        
        if (username == null) {
            logger.warn("Profile access denied - No username in session");
            redirectAttributes.addFlashAttribute("error", "Please login to continue.");
            return "redirect:/auth/login";
        }

        try {
            User user = viewService.getUserByUsername(username);
            logger.debug("User retrieved from service: {}, Role: {}", user.getUsername(), user.getRole());
            
          

            if (!viewService.isValidRole(user, user.getRole().toString())) {
                logger.warn("Profile access denied - Invalid role for user: {}", username);
                return viewService.getDashboardRedirect(username);
            }

            model.addAttribute("user", user);

            // Add role-specific data
            switch (user.getRole()) {
                case CUSTOMER -> {
                    Customer customer = (Customer) user;
                    List<DeliveryPackage> deliveredPackages = customer.getPackages().stream()
                           .filter(p -> p.getStatus() == DeliveryPackage.DeliveryStatus.DELIVERED)
                           .collect(Collectors.toList());
                    model.addAttribute("deliveredPackages", deliveredPackages);
                    model.addAttribute("totalPackages", customer.getPackages().size());
                    model.addAttribute("deliveredPackagesCount", deliveredPackages.size());
                    logger.debug("Added customer-specific data for user: {}", username);
                }
                case COURIER -> {
                    Courier courier = (Courier) user;
                    List<Rating> ratings = courier.getRatings();
                    model.addAttribute("ratings", ratings);
                    model.addAttribute("totalDeliveries", courier.getDeliveries().size());
                    model.addAttribute("ratingsCount", ratings.size());
                    logger.debug("Added courier-specific data for user: {}", username);
                }
                case ADMIN -> {
                    Long totalUsers = (Long) userService.getUserStats().get("totalUsers");
                    Long activeDeliveriesCount = deliveryPackageService.getAllDeliveryPackages().stream()
                            .filter(p -> p.getStatus() != DeliveryPackage.DeliveryStatus.DELIVERED 
                                    && p.getStatus() != DeliveryPackage.DeliveryStatus.CANCELLED)
                            .count();
                    model.addAttribute("totalUsers", totalUsers);
                    model.addAttribute("activeDeliveries", activeDeliveriesCount);
                    logger.debug("Added admin-specific data for user: {}", username);
                }
            }

            logger.info("Profile page successfully loaded for user: {}", username);
            return "profile";
        } catch (RuntimeException e) {
            logger.error("Error accessing profile page for user {}: {}", username, e.getMessage(), e);
            session.invalidate();
            redirectAttributes.addFlashAttribute("error", "Session expired. Please login again.");
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/admin/reports")
    public String showReportsPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
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

            List<DeliveryReport> reports = deliveryReportService.getAllReports();
            model.addAttribute("user", user);
            model.addAttribute("reports", reports);
            return "reports";
        } catch (RuntimeException e) {
            logger.error("Error accessing reports page: {}", e.getMessage());
            session.invalidate();
            redirectAttributes.addFlashAttribute("error", "Session expired. Please login again.");
            return "redirect:/auth/login";
        }
    }
}