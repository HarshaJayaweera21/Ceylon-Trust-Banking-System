package com.ceylonbank.webbasedbankingsystem.controller;

import com.ceylonbank.webbasedbankingsystem.entity.Feedback;
import com.ceylonbank.webbasedbankingsystem.entity.User;
import com.ceylonbank.webbasedbankingsystem.security.CustomUserDetails;
import com.ceylonbank.webbasedbankingsystem.service.FeedbackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {
    
    private static final Logger logger = LoggerFactory.getLogger(FeedbackController.class);
    
    @Autowired
    private FeedbackService feedbackService;
    
    // Display feedback submission page
    @GetMapping
    public String showFeedbackPage(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Unauthenticated access attempt to feedback page");
            return "redirect:/login";
        }
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        model.addAttribute("user", user);
        
        try {
            // Get user's feedback statistics
            Map<String, Object> stats = feedbackService.getFeedbackStatsForUser(user.getUserId());
            model.addAttribute("stats", stats);
            
            // Get user's recent feedback
            List<Feedback> userFeedback = feedbackService.getFeedbackByUser(user.getUserId());
            model.addAttribute("userFeedback", userFeedback);
            
        } catch (Exception e) {
            logger.error("Error loading feedback page for user {}: {}", user.getUserId(), e.getMessage(), e);
            model.addAttribute("error", "Error loading feedback page. Please try again later.");
            model.addAttribute("stats", Map.of("userFeedbackCount", 0L));
            model.addAttribute("userFeedback", List.of());
        }
        
        return "feedback";
    }
    
    // Submit feedback
    @PostMapping("/submit")
    @ResponseBody
    public ResponseEntity<?> submitFeedback(@RequestBody Map<String, Object> request, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Authentication required"));
            }
            
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            
            String message = (String) request.get("message");
            
            // Validate input
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Message is required"));
            }
            
            if (message.trim().length() < 10) {
                return ResponseEntity.badRequest().body(Map.of("error", "Message must be at least 10 characters long"));
            }
            
            if (message.trim().length() > 2000) {
                return ResponseEntity.badRequest().body(Map.of("error", "Message must be less than 2000 characters"));
            }
            
            // Submit feedback
            Feedback feedback = feedbackService.submitFeedback(user, message.trim());
            
            // Return simplified feedback data
            Map<String, Object> feedbackData = new java.util.HashMap<>();
            feedbackData.put("feedbackId", feedback.getFeedbackId());
            feedbackData.put("message", feedback.getMessage());
            feedbackData.put("submittedAt", feedback.getSubmittedAt());
            feedbackData.put("userName", user.getFirstName() + " " + user.getLastName());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Feedback submitted successfully",
                "feedback", feedbackData
            ));
        } catch (Exception e) {
            logger.error("Error submitting feedback: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to submit feedback: " + e.getMessage()
            ));
        }
    }
    
    // Get user's feedback history
    @GetMapping("/history")
    @ResponseBody
    public ResponseEntity<?> getUserFeedbackHistory(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Authentication required"));
            }
            
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            
            List<Feedback> userFeedback = feedbackService.getFeedbackByUser(user.getUserId());
            
            // Convert to simplified format for JSON response
            List<Map<String, Object>> feedbackData = userFeedback.stream()
                .map(feedback -> {
                    Map<String, Object> feedbackMap = new java.util.HashMap<>();
                    feedbackMap.put("feedbackId", feedback.getFeedbackId());
                    feedbackMap.put("message", feedback.getMessage());
                    feedbackMap.put("submittedAt", feedback.getSubmittedAt());
                    feedbackMap.put("isRecent", feedback.isRecent());
                    return feedbackMap;
                })
                .toList();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "feedback", feedbackData
            ));
        } catch (Exception e) {
            logger.error("Error fetching user feedback history: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to fetch feedback history: " + e.getMessage()
            ));
        }
    }
    
    // Get feedback statistics for user
    @GetMapping("/stats")
    @ResponseBody
    public ResponseEntity<?> getUserFeedbackStats(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Authentication required"));
            }
            
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            
            Map<String, Object> stats = feedbackService.getFeedbackStatsForUser(user.getUserId());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "stats", stats
            ));
        } catch (Exception e) {
            logger.error("Error fetching user feedback stats: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to fetch feedback statistics: " + e.getMessage()
            ));
        }
    }
}

