package com.example.courierdistributionsystem.controller.restController;

import com.example.courierdistributionsystem.dto.SignupRequest;
import com.example.courierdistributionsystem.model.DeliveryPackage;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.model.Customer;
import com.example.courierdistributionsystem.model.Courier;
import com.example.courierdistributionsystem.model.Admin;
import com.example.courierdistributionsystem.service.ViewService;
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
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.HashMap;
import org.hibernate.Hibernate;

@Controller
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ViewController {
    private static final Logger logger = LoggerFactory.getLogger(ViewController.class);

    @Autowired
    private ViewService viewService;

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
            model.addAttribute("signupForm", new SignupRequest());
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
    @Transactional(readOnly = true)
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

          
            Admin admin = (Admin) user;
            Hibernate.initialize(admin.getReports());

  
            List<User> allUsers = userService.getAllUsers();
            for (User u : allUsers) {
                if (u instanceof Customer) {
                    Hibernate.initialize(((Customer) u).getPackages());
                } else if (u instanceof Courier) {
                    Hibernate.initialize(((Courier) u).getDeliveries());
                } else if (u instanceof Admin) {
                    Hibernate.initialize(((Admin) u).getReports());
                }
            }
            
           
            List<DeliveryPackage> allPackages = deliveryPackageService.getAllDeliveryPackages();
            for (DeliveryPackage pkg : allPackages) {
                Hibernate.initialize(pkg.getStatusHistory());
                if (pkg.getCourier() != null) {
                    Hibernate.initialize(pkg.getCourier().getDeliveries());
                }
                if (pkg.getCustomer() != null) {
                    Hibernate.initialize(pkg.getCustomer().getPackages());
                }
            }
            
    
            long activeCouriers = allUsers.stream()
                    .filter(u -> u.getRole() == User.UserRole.COURIER)
                    .filter(u -> ((Courier) u).isAvailable())
                    .count();
            
     
            long pendingPackages = allPackages.stream()
                    .filter(p -> p.getStatus() == DeliveryPackage.DeliveryStatus.PENDING)
                    .count();
            

            long totalDeliveries = allPackages.stream()
                    .filter(p -> p.getStatus() == DeliveryPackage.DeliveryStatus.DELIVERED)
                    .count();

         
            model.addAttribute("user", admin);
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

            Map<String, Object> dashboardStats = viewService.getCustomerDashboardStats(user);
            model.addAttribute("user", user);
            model.addAttribute("activePackages", dashboardStats.get("activePackages"));
            model.addAttribute("completedPackages", dashboardStats.get("completedPackages"));
            model.addAttribute("stats", dashboardStats);
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

    @GetMapping("/settings")
    @Transactional(readOnly = true)
    public String showSettings(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            String username = (String) session.getAttribute("username");
            if (username == null) {
                redirectAttributes.addFlashAttribute("error", "Please login to continue.");
                return "redirect:/auth/login";
            }

            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            model.addAttribute("user", user);
            logger.info("Settings page successfully loaded for user: {}", username);
            return "settings";
        } catch (Exception e) {
            logger.error("Error loading settings page: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to load settings page: " + e.getMessage());
            return "redirect:/";
        }
    }

    @GetMapping("/profile")
    @Transactional(readOnly = true)
    public String showProfile(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            String username = (String) session.getAttribute("username");
            if (username == null) {
                return "redirect:/auth/login";
            }

            User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Map<String, Object> stats = new HashMap<>();
            if (user instanceof Customer) {
                stats = viewService.getCustomerProfileStats((Customer) user);
            } else if (user instanceof Courier) {
                stats = viewService.getCourierProfileStats((Courier) user);
            } else if (user instanceof Admin) {
                stats = viewService.getAdminProfileStats();
            }
            
            model.addAttribute("user", user);
            model.addAttribute("stats", stats);
            
            logger.info("Profile page successfully loaded for user: {}", username);
            return "profile";
        } catch (Exception e) {
            logger.error("Error loading profile page: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to load profile page: " + e.getMessage());
            return "redirect:/";
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

    @GetMapping("/customer/delivery-history")
    public String customerDeliveryHistory(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
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

            Map<String, Object> dashboardStats = viewService.getCustomerDashboardStats(user);
            model.addAttribute("user", user);
            model.addAttribute("completedPackages", dashboardStats.get("completedPackages"));
            model.addAttribute("role", "CUSTOMER");
            return "delivery_history";
        } catch (RuntimeException e) {
            logger.error("Error accessing delivery history: {}", e.getMessage());
            session.invalidate();
            redirectAttributes.addFlashAttribute("error", "Session expired. Please login again.");
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/courier/delivery-history")
    public String courierDeliveryHistory(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to continue.");
            return "redirect:/auth/login";
        }

        try {
            User user = viewService.getUserByUsername(username);
            if (!viewService.isValidRole(user, "COURIER")) {
                return viewService.getDashboardRedirect(username);
            }

            List<DeliveryPackage> completedDeliveries = deliveryPackageService.getCompletedDeliveriesByCourier((Courier) user);
            model.addAttribute("user", user);
            model.addAttribute("completedPackages", completedDeliveries);
            model.addAttribute("role", "COURIER");
            return "delivery_history";
        } catch (RuntimeException e) {
            logger.error("Error accessing courier delivery history: {}", e.getMessage());
            session.invalidate();
            redirectAttributes.addFlashAttribute("error", "Session expired. Please login again.");
            return "redirect:/auth/login";
        }
    }
}