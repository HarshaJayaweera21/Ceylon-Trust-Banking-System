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
import java.util.Optional;

@Controller
@RequestMapping("/admin/feedback")
public class FeedbackAdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(FeedbackAdminController.class);
    
    @Autowired
    private FeedbackService feedbackService;
    
    // Display feedback management page
    @GetMapping
    public String showFeedbackManagementPage(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Unauthenticated access attempt to feedback management page");
            return "redirect:/login";
        }
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User admin = userDetails.getUser();
        model.addAttribute("admin", admin);
        
        try {
            // Get all feedback
            List<Feedback> allFeedback = feedbackService.getAllFeedback();
            model.addAttribute("feedback", allFeedback);
            
            // Get statistics
            Map<String, Object> stats = feedbackService.getFeedbackStats();
            model.addAttribute("stats", stats);
            
        } catch (Exception e) {
            logger.error("Error loading feedback management page: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading feedback management page. Please try again later.");
            model.addAttribute("feedback", List.of());
            model.addAttribute("stats", Map.of(
                "totalFeedback", 0L,
                "recentFeedback", 0L,
                "todayFeedback", 0L
            ));
        }
        
        return "feedback-admin";
    }
    
    // Get all feedback (AJAX)
    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<?> getAllFeedback() {
        try {
            List<Feedback> feedback = feedbackService.getAllFeedback();
            
            // Convert to simplified format for JSON response
            List<Map<String, Object>> feedbackData = feedback.stream()
                .map(feedbackItem -> {
                    Map<String, Object> feedbackMap = new java.util.HashMap<>();
                    feedbackMap.put("feedbackId", feedbackItem.getFeedbackId());
                    feedbackMap.put("message", feedbackItem.getMessage());
                    feedbackMap.put("submittedAt", feedbackItem.getSubmittedAt());
                    feedbackMap.put("isRecent", feedbackItem.isRecent());
                    feedbackMap.put("userName", feedbackItem.getUser() != null ? 
                        feedbackItem.getUser().getFirstName() + " " + feedbackItem.getUser().getLastName() : "Unknown");
                    feedbackMap.put("userEmail", feedbackItem.getUser() != null ? 
                        feedbackItem.getUser().getEmail() : "Unknown");
                    feedbackMap.put("userRole", feedbackItem.getUser() != null ? 
                        feedbackItem.getUser().getRole() : "Unknown");
                    return feedbackMap;
                })
                .toList();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "feedback", feedbackData
            ));
        } catch (Exception e) {
            logger.error("Error fetching all feedback: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to fetch feedback: " + e.getMessage()
            ));
        }
    }
    
    // Get feedback details for viewing
    @GetMapping("/{feedbackId}")
    @ResponseBody
    public ResponseEntity<?> getFeedbackDetails(@PathVariable Integer feedbackId) {
        try {
            Optional<Feedback> feedbackOpt = feedbackService.getFeedbackById(feedbackId);
            if (feedbackOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Feedback feedback = feedbackOpt.get();
            Map<String, Object> feedbackData = new java.util.HashMap<>();
            feedbackData.put("feedbackId", feedback.getFeedbackId());
            feedbackData.put("message", feedback.getMessage());
            feedbackData.put("submittedAt", feedback.getSubmittedAt());
            feedbackData.put("isRecent", feedback.isRecent());
            
            if (feedback.getUser() != null) {
                Map<String, Object> userData = new java.util.HashMap<>();
                userData.put("userId", feedback.getUser().getUserId());
                userData.put("firstName", feedback.getUser().getFirstName());
                userData.put("lastName", feedback.getUser().getLastName());
                userData.put("email", feedback.getUser().getEmail());
                userData.put("role", feedback.getUser().getRole());
                userData.put("nic", feedback.getUser().getNic());
                feedbackData.put("user", userData);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "feedback", feedbackData
            ));
        } catch (Exception e) {
            logger.error("Error fetching feedback details with ID {}: {}", feedbackId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to fetch feedback details: " + e.getMessage()
            ));
        }
    }
    
    // Delete feedback
    @DeleteMapping("/delete/{feedbackId}")
    @ResponseBody
    public ResponseEntity<?> deleteFeedback(@PathVariable Integer feedbackId) {
        try {
            feedbackService.deleteFeedback(feedbackId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Feedback deleted successfully"
            ));
        } catch (Exception e) {
            logger.error("Error deleting feedback with ID {}: {}", feedbackId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to delete feedback: " + e.getMessage()
            ));
        }
    }
    
    // Search feedback by message
    @GetMapping("/search/message")
    @ResponseBody
    public ResponseEntity<?> searchFeedbackByMessage(@RequestParam String searchTerm) {
        try {
            List<Feedback> feedback = feedbackService.searchFeedbackByMessage(searchTerm);
            
            // Convert to simplified format for JSON response
            List<Map<String, Object>> feedbackData = feedback.stream()
                .map(feedbackItem -> {
                    Map<String, Object> feedbackMap = new java.util.HashMap<>();
                    feedbackMap.put("feedbackId", feedbackItem.getFeedbackId());
                    feedbackMap.put("message", feedbackItem.getMessage());
                    feedbackMap.put("submittedAt", feedbackItem.getSubmittedAt());
                    feedbackMap.put("isRecent", feedbackItem.isRecent());
                    feedbackMap.put("userName", feedbackItem.getUser() != null ? 
                        feedbackItem.getUser().getFirstName() + " " + feedbackItem.getUser().getLastName() : "Unknown");
                    feedbackMap.put("userEmail", feedbackItem.getUser() != null ? 
                        feedbackItem.getUser().getEmail() : "Unknown");
                    feedbackMap.put("userRole", feedbackItem.getUser() != null ? 
                        feedbackItem.getUser().getRole() : "Unknown");
                    return feedbackMap;
                })
                .toList();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "feedback", feedbackData
            ));
        } catch (Exception e) {
            logger.error("Error searching feedback by message: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to search feedback: " + e.getMessage()
            ));
        }
    }
    
    // Search feedback by user name
    @GetMapping("/search/user")
    @ResponseBody
    public ResponseEntity<?> searchFeedbackByUserName(@RequestParam String searchTerm) {
        try {
            List<Feedback> feedback = feedbackService.searchFeedbackByUserName(searchTerm);
            
            // Convert to simplified format for JSON response
            List<Map<String, Object>> feedbackData = feedback.stream()
                .map(feedbackItem -> {
                    Map<String, Object> feedbackMap = new java.util.HashMap<>();
                    feedbackMap.put("feedbackId", feedbackItem.getFeedbackId());
                    feedbackMap.put("message", feedbackItem.getMessage());
                    feedbackMap.put("submittedAt", feedbackItem.getSubmittedAt());
                    feedbackMap.put("isRecent", feedbackItem.isRecent());
                    feedbackMap.put("userName", feedbackItem.getUser() != null ? 
                        feedbackItem.getUser().getFirstName() + " " + feedbackItem.getUser().getLastName() : "Unknown");
                    feedbackMap.put("userEmail", feedbackItem.getUser() != null ? 
                        feedbackItem.getUser().getEmail() : "Unknown");
                    feedbackMap.put("userRole", feedbackItem.getUser() != null ? 
                        feedbackItem.getUser().getRole() : "Unknown");
                    return feedbackMap;
                })
                .toList();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "feedback", feedbackData
            ));
        } catch (Exception e) {
            logger.error("Error searching feedback by user name: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to search feedback: " + e.getMessage()
            ));
        }
    }
    
    // Get feedback statistics
    @GetMapping("/stats")
    @ResponseBody
    public ResponseEntity<?> getFeedbackStats() {
        try {
            Map<String, Object> stats = feedbackService.getFeedbackStats();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "stats", stats
            ));
        } catch (Exception e) {
            logger.error("Error fetching feedback statistics: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to fetch feedback statistics: " + e.getMessage()
            ));
        }
    }
}
