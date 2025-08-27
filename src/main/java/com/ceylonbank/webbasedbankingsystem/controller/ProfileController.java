package com.ceylonbank.webbasedbankingsystem.controller;

import com.ceylonbank.webbasedbankingsystem.entity.User;
import com.ceylonbank.webbasedbankingsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/profile")
    public String profilePage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Optional<User> userOpt = userService.getUserByUsername(username);
        if (userOpt.isPresent()) {
            model.addAttribute("user", userOpt.get());
        } else {
            // Redirect to login if user not found
            return "redirect:/login";
        }
        
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String firstName,
                               @RequestParam String lastName,
                               @RequestParam String email,
                               @RequestParam String street,
                               @RequestParam String city,
                               @RequestParam String postalCode,
                               RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            Optional<User> userOpt = userService.getUserByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Update user fields
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setEmail(email);
                user.setStreet(street);
                user.setCity(city);
                user.setPostalCode(postalCode);
                
                // Save updated user using the profile-specific method
                userService.updateUserProfile(user.getUserId(), firstName, lastName, email, street, city, postalCode);
                
                redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "User not found.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }

    @PostMapping("/profile/delete")
    public String deleteProfile(@RequestParam String password,
                               RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            Optional<User> userOpt = userService.getUserByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Verify password before deactivation
                if (passwordEncoder.matches(password, user.getPasswordHash())) {
                    // Password is correct, proceed with deactivation
                    userService.deactivateUser(user.getUserId());
                    
                    // Clear the security context
                    SecurityContextHolder.clearContext();
                    
                    // Redirect to login page with success message
                    redirectAttributes.addFlashAttribute("success", "Account deactivated successfully. You have been logged out.");
                    return "redirect:/login?deactivated=true";
                } else {
                    // Password is incorrect
                    redirectAttributes.addFlashAttribute("error", "Incorrect password. Please try again.");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "User not found.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to deactivate profile: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }
}
