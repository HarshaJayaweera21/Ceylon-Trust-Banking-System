package com.ceylonbank.webbasedbankingsystem.controller;

import com.ceylonbank.webbasedbankingsystem.entity.FAQ;
import com.ceylonbank.webbasedbankingsystem.entity.User;
import com.ceylonbank.webbasedbankingsystem.security.CustomUserDetails;
import com.ceylonbank.webbasedbankingsystem.service.FAQService;
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
@RequestMapping("/admin/faq")
public class FAQAdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(FAQAdminController.class);
    
    @Autowired
    private FAQService faqService;
    
    // Display FAQ management page
    @GetMapping
    public String showFAQManagementPage(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Unauthenticated access attempt to FAQ management page");
            return "redirect:/login";
        }
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User admin = userDetails.getUser();
        model.addAttribute("admin", admin);
        
        try {
            // Get all FAQs
            List<FAQ> allFAQs = faqService.getAllFAQs();
            model.addAttribute("faqs", allFAQs);
            
            // Get categories
            List<String> categories = faqService.getDistinctCategories();
            model.addAttribute("categories", categories);
            
            // Get statistics
            Map<String, Object> stats = faqService.getFAQStats();
            model.addAttribute("stats", stats);
            
        } catch (Exception e) {
            logger.error("Error loading FAQ management page: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading FAQ management page. Please try again later.");
            model.addAttribute("faqs", List.of());
            model.addAttribute("categories", List.of());
            model.addAttribute("stats", Map.of(
                "totalFAQs", 0L,
                "totalCategories", 0L,
                "recentCount", 0L
            ));
        }
        
        return "faq-admin";
    }
    
    // Create new FAQ
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<?> createFAQ(@RequestBody Map<String, String> request, Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User admin = userDetails.getUser();
            
            String question = request.get("question");
            String answer = request.get("answer");
            String category = request.get("category");
            
            // Validate input
            if (question == null || question.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Question is required"));
            }
            if (answer == null || answer.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Answer is required"));
            }
            
            FAQ newFAQ = faqService.createFAQ(question.trim(), answer.trim(), category, admin);
            
            // Return simplified FAQ data
            Map<String, Object> faqData = new java.util.HashMap<>();
            faqData.put("faqId", newFAQ.getFaqId());
            faqData.put("question", newFAQ.getQuestion());
            faqData.put("answer", newFAQ.getAnswer());
            faqData.put("category", newFAQ.getFormattedCategory());
            faqData.put("createdAt", newFAQ.getCreatedAt());
            faqData.put("updatedAt", newFAQ.getUpdatedAt());
            faqData.put("isRecentlyUpdated", newFAQ.isRecentlyUpdated());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "FAQ created successfully",
                "faq", faqData
            ));
        } catch (Exception e) {
            logger.error("Error creating FAQ: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to create FAQ: " + e.getMessage()
            ));
        }
    }
    
    // Update existing FAQ
    @PutMapping("/update/{faqId}")
    @ResponseBody
    public ResponseEntity<?> updateFAQ(@PathVariable Integer faqId, @RequestBody Map<String, String> request) {
        try {
            String question = request.get("question");
            String answer = request.get("answer");
            String category = request.get("category");
            
            // Validate input
            if (question == null || question.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Question is required"));
            }
            if (answer == null || answer.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Answer is required"));
            }
            
            FAQ updatedFAQ = faqService.updateFAQ(faqId, question.trim(), answer.trim(), category);
            
            // Return simplified FAQ data
            Map<String, Object> faqData = new java.util.HashMap<>();
            faqData.put("faqId", updatedFAQ.getFaqId());
            faqData.put("question", updatedFAQ.getQuestion());
            faqData.put("answer", updatedFAQ.getAnswer());
            faqData.put("category", updatedFAQ.getFormattedCategory());
            faqData.put("createdAt", updatedFAQ.getCreatedAt());
            faqData.put("updatedAt", updatedFAQ.getUpdatedAt());
            faqData.put("isRecentlyUpdated", updatedFAQ.isRecentlyUpdated());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "FAQ updated successfully",
                "faq", faqData
            ));
        } catch (Exception e) {
            logger.error("Error updating FAQ with ID {}: {}", faqId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to update FAQ: " + e.getMessage()
            ));
        }
    }
    
    // Delete FAQ
    @DeleteMapping("/delete/{faqId}")
    @ResponseBody
    public ResponseEntity<?> deleteFAQ(@PathVariable Integer faqId) {
        try {
            faqService.deleteFAQ(faqId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "FAQ deleted successfully"
            ));
        } catch (Exception e) {
            logger.error("Error deleting FAQ with ID {}: {}", faqId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to delete FAQ: " + e.getMessage()
            ));
        }
    }
    
    // Get FAQ details for editing
    @GetMapping("/{faqId}")
    @ResponseBody
    public ResponseEntity<?> getFAQForEdit(@PathVariable Integer faqId) {
        try {
            Optional<FAQ> faqOpt = faqService.getFAQById(faqId);
            if (faqOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            FAQ faq = faqOpt.get();
            Map<String, Object> faqData = new java.util.HashMap<>();
            faqData.put("faqId", faq.getFaqId());
            faqData.put("question", faq.getQuestion());
            faqData.put("answer", faq.getAnswer());
            faqData.put("category", faq.getCategory());
            faqData.put("createdAt", faq.getCreatedAt());
            faqData.put("updatedAt", faq.getUpdatedAt());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "faq", faqData
            ));
        } catch (Exception e) {
            logger.error("Error fetching FAQ for edit with ID {}: {}", faqId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to fetch FAQ: " + e.getMessage()
            ));
        }
    }
    
    // Get all FAQs (AJAX)
    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<?> getAllFAQs() {
        try {
            List<FAQ> faqs = faqService.getAllFAQs();
            
            // Convert to simplified format for JSON response
            List<Map<String, Object>> faqData = faqs.stream()
                .map(faq -> {
                    Map<String, Object> faqMap = new java.util.HashMap<>();
                    faqMap.put("faqId", faq.getFaqId());
                    faqMap.put("question", faq.getQuestion());
                    faqMap.put("answer", faq.getAnswer());
                    faqMap.put("category", faq.getFormattedCategory());
                    faqMap.put("createdAt", faq.getCreatedAt());
                    faqMap.put("updatedAt", faq.getUpdatedAt());
                    faqMap.put("isRecentlyUpdated", faq.isRecentlyUpdated());
                    return faqMap;
                })
                .toList();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "faqs", faqData
            ));
        } catch (Exception e) {
            logger.error("Error fetching all FAQs: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to fetch FAQs: " + e.getMessage()
            ));
        }
    }
    
    // Get FAQs by category (AJAX)
    @GetMapping("/category/{category}")
    @ResponseBody
    public ResponseEntity<?> getFAQsByCategory(@PathVariable String category) {
        try {
            List<FAQ> faqs = faqService.getFAQsByCategory(category);
            
            // Convert to simplified format for JSON response
            List<Map<String, Object>> faqData = faqs.stream()
                .map(faq -> {
                    Map<String, Object> faqMap = new java.util.HashMap<>();
                    faqMap.put("faqId", faq.getFaqId());
                    faqMap.put("question", faq.getQuestion());
                    faqMap.put("answer", faq.getAnswer());
                    faqMap.put("category", faq.getFormattedCategory());
                    faqMap.put("createdAt", faq.getCreatedAt());
                    faqMap.put("updatedAt", faq.getUpdatedAt());
                    faqMap.put("isRecentlyUpdated", faq.isRecentlyUpdated());
                    return faqMap;
                })
                .toList();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "faqs", faqData,
                "category", category,
                "count", faqs.size()
            ));
        } catch (Exception e) {
            logger.error("Error fetching FAQs for category '{}': {}", category, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to fetch FAQs for category: " + e.getMessage()
            ));
        }
    }
    
    // Get categories (AJAX)
    @GetMapping("/categories")
    @ResponseBody
    public ResponseEntity<?> getCategories() {
        try {
            List<String> categories = faqService.getDistinctCategories();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "categories", categories
            ));
        } catch (Exception e) {
            logger.error("Error fetching FAQ categories: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to fetch categories: " + e.getMessage()
            ));
        }
    }
}
