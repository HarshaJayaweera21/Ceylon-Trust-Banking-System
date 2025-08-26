package com.ceylonbank.webbasedbankingsystem.controller;

import com.ceylonbank.webbasedbankingsystem.entity.FAQ;
import com.ceylonbank.webbasedbankingsystem.service.FAQService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/faq")
public class FAQController {
    
    private static final Logger logger = LoggerFactory.getLogger(FAQController.class);
    
    @Autowired
    private FAQService faqService;
    
    // Display FAQ page
    @GetMapping
    public String showFAQPage(Model model) {
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
            
            // Get FAQs grouped by category
            Map<String, List<FAQ>> faqsByCategory = faqService.getFAQsGroupedByCategory();
            model.addAttribute("faqsByCategory", faqsByCategory);
            
        } catch (Exception e) {
            logger.error("Error loading FAQ page: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading FAQs. Please try again later.");
            model.addAttribute("faqs", List.of());
            model.addAttribute("categories", List.of("All"));
            model.addAttribute("stats", Map.of(
                "totalFAQs", 0L,
                "totalCategories", 0L,
                "recentCount", 0L
            ));
            model.addAttribute("faqsByCategory", Map.of());
        }
        
        return "faq";
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
    
    // Search FAQs (AJAX)
    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<?> searchFAQs(@RequestParam String searchTerm) {
        try {
            List<FAQ> faqs = faqService.searchFAQs(searchTerm);
            
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
                "searchTerm", searchTerm,
                "count", faqs.size()
            ));
        } catch (Exception e) {
            logger.error("Error searching FAQs with term '{}': {}", searchTerm, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to search FAQs: " + e.getMessage()
            ));
        }
    }
    
    // Get FAQ details (AJAX)
    @GetMapping("/{faqId}")
    @ResponseBody
    public ResponseEntity<?> getFAQDetails(@PathVariable Integer faqId) {
        try {
            Optional<FAQ> faqOpt = faqService.getFAQById(faqId);
            if (faqOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            FAQ faq = faqOpt.get();
            Map<String, Object> faqData = Map.of(
                "faqId", faq.getFaqId(),
                "question", faq.getQuestion(),
                "answer", faq.getAnswer(),
                "category", faq.getFormattedCategory(),
                "createdAt", faq.getCreatedAt(),
                "updatedAt", faq.getUpdatedAt(),
                "isRecentlyUpdated", faq.isRecentlyUpdated()
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "faq", faqData
            ));
        } catch (Exception e) {
            logger.error("Error fetching FAQ details for ID {}: {}", faqId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to fetch FAQ details: " + e.getMessage()
            ));
        }
    }
    
    // Get FAQ statistics (AJAX)
    @GetMapping("/stats")
    @ResponseBody
    public ResponseEntity<?> getFAQStats() {
        try {
            Map<String, Object> stats = faqService.getFAQStats();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "stats", stats
            ));
        } catch (Exception e) {
            logger.error("Error fetching FAQ statistics: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to fetch FAQ statistics: " + e.getMessage()
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
                "error", "Failed to fetch FAQ categories: " + e.getMessage()
            ));
        }
    }
}
