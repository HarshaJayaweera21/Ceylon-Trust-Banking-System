package com.ceylonbank.webbasedbankingsystem.controller;

import com.ceylonbank.webbasedbankingsystem.entity.SupportTicket;
import com.ceylonbank.webbasedbankingsystem.entity.User;
import com.ceylonbank.webbasedbankingsystem.security.CustomUserDetails;
import com.ceylonbank.webbasedbankingsystem.service.SupportTicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Controller
@RequestMapping("/contact")
public class ContactController {
    
    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);
    
    @Autowired
    private SupportTicketService supportTicketService;
    
    @GetMapping
    public String showContactPage(Model model, Authentication authentication) {
        try {
            // Check if user is authenticated
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Unauthenticated user attempted to access contact page");
                return "redirect:/login?redirect=/contact";
            }
            
            // Get authenticated user
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            
            // Verify user is a customer
            if (!"Customer".equals(user.getRole().getRoleName())) {
                logger.warn("Non-customer user {} attempted to access contact page", user.getUserId());
                return "redirect:/access-denied";
            }
            
            // Add user info to model
            model.addAttribute("user", user);
            logger.info("Contact page accessed by authenticated customer: {}", user.getUserId());
            
            return "contact";
        } catch (Exception e) {
            logger.error("Error loading contact page: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading contact page. Please try again later.");
            return "contact";
        }
    }
    
    @PostMapping("/submit-ticket")
    public String submitSupportTicket(
            @RequestParam @NotBlank(message = "Subject is required") 
            @Size(min = 5, max = 100, message = "Subject must be between 5 and 100 characters") String subject,
            @RequestParam @NotBlank(message = "Message is required") 
            @Size(min = 10, max = 1000, message = "Message must be between 10 and 1000 characters") String message,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            logger.info("Support ticket submission attempt");
            
            // Check if user is authenticated
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Unauthenticated user attempted to submit support ticket");
                redirectAttributes.addFlashAttribute("error", "You must be logged in to submit a support ticket.");
                return "redirect:/login?redirect=/contact";
            }
            
            // Get authenticated user
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            
            // Validate user is a customer
            if (!"Customer".equals(user.getRole().getRoleName())) {
                logger.warn("Non-customer user {} attempted to submit support ticket", user.getUserId());
                redirectAttributes.addFlashAttribute("error", "Only customers can submit support tickets.");
                return "redirect:/contact";
            }
            
            // Create support ticket
            SupportTicket ticket = supportTicketService.createTicket(user, subject, message);
            
            logger.info("Support ticket created successfully with ID: {} for user: {}", 
                       ticket.getTicketId(), user.getUserId());
            
            // Add success message
            redirectAttributes.addFlashAttribute("success", "true");
            redirectAttributes.addFlashAttribute("message", 
                "Your support ticket has been submitted successfully! Ticket ID: " + ticket.getTicketId() + 
                ". We will get back to you as soon as possible.");
            
            return "redirect:/contact?success=true";
            
        } catch (IllegalArgumentException e) {
            logger.warn("Validation error in support ticket submission: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/contact?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            logger.error("Error submitting support ticket: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", 
                "Failed to submit support ticket. Please try again or contact us directly.");
            return "redirect:/contact?error=" + java.net.URLEncoder.encode("Failed to submit support ticket. Please try again.", java.nio.charset.StandardCharsets.UTF_8);
        }
    }
}
