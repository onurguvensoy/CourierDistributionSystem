package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.model.SignupForm;
import com.example.courierdistributionsystem.model.Package;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.service.ViewService;
import com.example.courierdistributionsystem.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/")
public class ViewController {

    @Autowired
    private ViewService viewService;

    @Autowired
    private AuthService authService;

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

    @GetMapping("/auth/login")
    public String showLoginForm(@RequestParam(required = false) String error,
                              @RequestParam(required = false) String registered,
                              @RequestParam(required = false) String logout,
                              Model model,
                              HttpSession session) {
        if (session.getAttribute("username") != null) {
            return "redirect:/dashboard";
        }

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

    @PostMapping("/auth/login")
    public String login(@RequestParam String username,
                       @RequestParam String password,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        Map<String, String> response = authService.CheckLoginUser(Map.of(
            "username", username,
            "password", password
        ));

        if (response.containsKey("error")) {
            redirectAttributes.addAttribute("error", true);
            return "redirect:/auth/login";
        }

        session.setAttribute("username", response.get("username"));
        session.setAttribute("role", response.get("role"));
        if (response.containsKey("phoneNumber")) {
            session.setAttribute("phoneNumber", response.get("phoneNumber"));
        }
        if (response.containsKey("isAvailable")) {
            session.setAttribute("isAvailable", response.get("isAvailable"));
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/auth/signup")
    public String showSignupForm(Model model, HttpSession session) {
        if (session.getAttribute("username") != null) {
            return "redirect:/dashboard";
        }

        model.addAttribute("signupForm", new SignupForm());
        return "signup";
    }

    @PostMapping("/auth/signup")
    public String signup(@ModelAttribute SignupForm signupForm,
                        RedirectAttributes redirectAttributes) {
        try {
            authService.PostSignup(signupForm.getUsername(), 
                                 signupForm.getEmail(), 
                                 signupForm.getPassword(), 
                                 signupForm.getRoleType());
            redirectAttributes.addAttribute("registered", true);
            return "redirect:/auth/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/signup";
        }
    }

    @PostMapping("/auth/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login?logout=true";
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