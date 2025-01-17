package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.model.Package;
import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.repository.PackageRepository;
import com.example.courierdistributionsystem.repository.UserRepository;
import com.example.courierdistributionsystem.service.WebSocketService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/courier")
public class CourierPackageController {

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebSocketService webSocketService;

    @PostMapping("/take-package")
    public String takePackage(@RequestParam Long packageId,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/auth/login";
        }

        User courier = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (courier.getRole() != User.UserRole.COURIER) {
            return "redirect:/dashboard";
        }

        Package pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found"));

        if (pkg.getStatus() != Package.PackageStatus.PENDING) {
            redirectAttributes.addFlashAttribute("error", "Package is no longer available");
            return "redirect:/courier/dashboard";
        }

        pkg.setCourier(courier);
        pkg.setStatus(Package.PackageStatus.ASSIGNED);
        packageRepository.save(pkg);

        
        webSocketService.notifyPackageStatusUpdate(pkg);

        redirectAttributes.addFlashAttribute("message", "Package assigned successfully");
        return "redirect:/courier/dashboard";
    }

    @PostMapping("/update-delivery-status")
    public String updateDeliveryStatus(@RequestParam Long packageId,
                                     @RequestParam String status,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/auth/login";
        }

        User courier = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (courier.getRole() != User.UserRole.COURIER) {
            return "redirect:/dashboard";
        }

        Package pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found"));

        if (!pkg.getCourier().getId().equals(courier.getId())) {
            redirectAttributes.addFlashAttribute("error", "You are not assigned to this package");
            return "redirect:/courier/dashboard";
        }

        try {
            Package.PackageStatus newStatus = Package.PackageStatus.valueOf(status);
            pkg.setStatus(newStatus);
            packageRepository.save(pkg);

            webSocketService.notifyPackageStatusUpdate(pkg);

            redirectAttributes.addFlashAttribute("message", "Delivery status updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid status");
        }

        return "redirect:/courier/dashboard";
    }
} 