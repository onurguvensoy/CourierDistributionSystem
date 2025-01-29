package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.model.SignupForm;
import com.example.courierdistributionsystem.model.DeliveryPackage;
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
import java.util.HashMap;

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

        session.setAttribute("username", username);
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
        
        if (!model.containsAttribute("signupForm")) {
            model.addAttribute("signupForm", new SignupForm());
        }
        
        return "signup";
    }

    @PostMapping("/auth/signup")
    public String handleSignup(@ModelAttribute SignupForm signupForm, 
                             Model model, 
                             HttpSession session,
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
            model.addAttribute("error", response.get("error"));
            model.addAttribute("signupForm", signupForm);
            return "signup";
        }
        
        redirectAttributes.addFlashAttribute("message", "Registration successful! Please login.");
        return "redirect:/auth/login";
    }

    @RequestMapping(value = "/auth/logout", method = {RequestMethod.GET, RequestMethod.POST})
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        authService.logoutUser();
        redirectAttributes.addFlashAttribute("message", "You have been logged out successfully");
        return "redirect:/auth/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/auth/login";
        }
        
        try {
            String view = viewService.getDashboardRedirect(username);
            return view;
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

        List<DeliveryPackage> availablePackages = viewService.getAvailablePackages();
        List<DeliveryPackage> activeDeliveries = viewService.getActiveDeliveries(courier);

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

        List<DeliveryPackage> myPackages = viewService.getCustomerPackages(user);

        model.addAttribute("user", user);
        model.addAttribute("myPackages", myPackages);
        model.addAttribute("googleMapsApiKey", googleMapsApiKey);
        return "customer_dashboard";
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