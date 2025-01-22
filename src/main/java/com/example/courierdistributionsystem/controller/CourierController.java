/*
package com.example.courierdistributionsystem.controller;

import com.example.courierdistributionsystem.service.PackageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/courier")
public class CourierController {

    @Autowired
    private PackageService packageService;

    @PostMapping("/take-package")
    public String takePackage(@RequestParam Long packageId,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/auth/login";
        }

        try {
            packageService.takePackage(packageId, username);
            redirectAttributes.addFlashAttribute("message", "Package assigned successfully");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred while taking the package");
        }

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

        try {
            packageService.updateDeliveryStatus(packageId, status, username);
            redirectAttributes.addFlashAttribute("message", "Delivery status updated successfully");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred while updating the status");
        }

        return "redirect:/courier/dashboard";
    }
} */
