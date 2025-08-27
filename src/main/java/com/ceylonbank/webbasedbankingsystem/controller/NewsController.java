package com.ceylonbank.webbasedbankingsystem.controller;

import com.ceylonbank.webbasedbankingsystem.entity.BankNews;
import com.ceylonbank.webbasedbankingsystem.security.CustomUserDetails;
import com.ceylonbank.webbasedbankingsystem.service.BankNewsService;
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
@RequestMapping("/news")
public class NewsController {
    
    private static final Logger logger = LoggerFactory.getLogger(NewsController.class);
    
    @Autowired
    private BankNewsService bankNewsService;
    
    // Display news page
    @GetMapping
    public String showNewsPage(Model model, Authentication authentication) {
        try {
            String userRole = "Customer"; // Default role
            
            if (authentication != null && authentication.isAuthenticated()) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                userRole = userDetails.getUser().getRole().getRoleName();
            }
            
            // Get all visible news
            List<BankNews> allNews = bankNewsService.getVisibleNews(userRole);
            model.addAttribute("news", allNews);
            
            // Get categories
            List<String> categories = bankNewsService.getDistinctCategories(userRole);
            model.addAttribute("categories", categories);
            
            // Get statistics
            Map<String, Object> stats = bankNewsService.getNewsStats(userRole);
            model.addAttribute("stats", stats);
            
            // Get news grouped by category
            Map<String, List<BankNews>> newsByCategory = bankNewsService.getNewsGroupedByCategory(userRole);
            model.addAttribute("newsByCategory", newsByCategory);
            
            // Add user role to model
            model.addAttribute("userRole", userRole);
            
        } catch (Exception e) {
            logger.error("Error loading news page: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading news. Please try again later.");
            model.addAttribute("news", List.of());
            model.addAttribute("categories", List.of());
            model.addAttribute("stats", Map.of(
                "totalNews", 0L,
                "recentCount", 0L,
                "userRole", "Customer"
            ));
            model.addAttribute("newsByCategory", Map.of());
            model.addAttribute("userRole", "Customer");
        }
        
        return "news";
    }
    
    // Get news by category (AJAX)
    @GetMapping("/category/{category}")
    @ResponseBody
    public ResponseEntity<?> getNewsByCategory(@PathVariable String category, Authentication authentication) {
        try {
            String userRole = "Customer"; // Default role
            
            if (authentication != null && authentication.isAuthenticated()) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                userRole = userDetails.getUser().getRole().getRoleName();
            }
            
            List<BankNews> news = bankNewsService.getNewsByCategory(category, userRole);
            
            // Convert to simplified format for JSON response
            List<Map<String, Object>> newsData = news.stream()
                .map(newsItem -> {
                    Map<String, Object> newsMap = new java.util.HashMap<>();
                    newsMap.put("newsId", newsItem.getNewsId());
                    newsMap.put("title", newsItem.getTitle());
                    newsMap.put("content", newsItem.getContent());
                    newsMap.put("category", newsItem.getFormattedCategory());
                    newsMap.put("postedAt", newsItem.getPostedAt());
                    newsMap.put("expiryDate", newsItem.getExpiryDate());
                    newsMap.put("isPublic", newsItem.getIsPublic());
                    newsMap.put("isExpired", newsItem.isExpired());
                    newsMap.put("postedBy", newsItem.getPostedBy() != null ? 
                        newsItem.getPostedBy().getFirstName() + " " + newsItem.getPostedBy().getLastName() : "Unknown");
                    return newsMap;
                })
                .toList();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "news", newsData,
                "category", category,
                "count", news.size()
            ));
        } catch (Exception e) {
            logger.error("Error fetching news for category '{}': {}", category, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to fetch news for category: " + e.getMessage()
            ));
        }
    }
    
    // Search news (AJAX)
    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<?> searchNews(@RequestParam String searchTerm, Authentication authentication) {
        try {
            String userRole = "Customer"; // Default role
            
            if (authentication != null && authentication.isAuthenticated()) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                userRole = userDetails.getUser().getRole().getRoleName();
            }
            
            List<BankNews> news = bankNewsService.searchNews(searchTerm, userRole);
            
            // Convert to simplified format for JSON response
            List<Map<String, Object>> newsData = news.stream()
                .map(newsItem -> {
                    Map<String, Object> newsMap = new java.util.HashMap<>();
                    newsMap.put("newsId", newsItem.getNewsId());
                    newsMap.put("title", newsItem.getTitle());
                    newsMap.put("content", newsItem.getContent());
                    newsMap.put("category", newsItem.getFormattedCategory());
                    newsMap.put("postedAt", newsItem.getPostedAt());
                    newsMap.put("expiryDate", newsItem.getExpiryDate());
                    newsMap.put("isPublic", newsItem.getIsPublic());
                    newsMap.put("isExpired", newsItem.isExpired());
                    newsMap.put("postedBy", newsItem.getPostedBy() != null ? 
                        newsItem.getPostedBy().getFirstName() + " " + newsItem.getPostedBy().getLastName() : "Unknown");
                    return newsMap;
                })
                .toList();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "news", newsData,
                "searchTerm", searchTerm,
                "count", news.size()
            ));
        } catch (Exception e) {
            logger.error("Error searching news with term '{}': {}", searchTerm, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to search news: " + e.getMessage()
            ));
        }
    }
    
    // Get news details (AJAX)
    @GetMapping("/{newsId}")
    @ResponseBody
    public ResponseEntity<?> getNewsDetails(@PathVariable Integer newsId, Authentication authentication) {
        try {
            String userRole = "Customer"; // Default role
            
            if (authentication != null && authentication.isAuthenticated()) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                userRole = userDetails.getUser().getRole().getRoleName();
            }
            
            Optional<BankNews> newsOpt = bankNewsService.getNewsById(newsId);
            if (newsOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            BankNews news = newsOpt.get();
            
            // Check if news is visible to user role
            if (!bankNewsService.isNewsVisibleToRole(news, userRole)) {
                return ResponseEntity.badRequest().body(Map.of("error", "News not accessible"));
            }
            
            Map<String, Object> newsData = new java.util.HashMap<>();
            newsData.put("newsId", news.getNewsId());
            newsData.put("title", news.getTitle());
            newsData.put("content", news.getContent());
            newsData.put("category", news.getFormattedCategory());
            newsData.put("postedAt", news.getPostedAt());
            newsData.put("expiryDate", news.getExpiryDate());
            newsData.put("isPublic", news.getIsPublic());
            newsData.put("isExpired", news.isExpired());
            newsData.put("postedBy", news.getPostedBy() != null ? 
                news.getPostedBy().getFirstName() + " " + news.getPostedBy().getLastName() : "Unknown");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "news", newsData
            ));
        } catch (Exception e) {
            logger.error("Error fetching news details for ID {}: {}", newsId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to fetch news details: " + e.getMessage()
            ));
        }
    }
    
    // Get news statistics (AJAX)
    @GetMapping("/stats")
    @ResponseBody
    public ResponseEntity<?> getNewsStats(Authentication authentication) {
        try {
            String userRole = "Customer"; // Default role
            
            if (authentication != null && authentication.isAuthenticated()) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                userRole = userDetails.getUser().getRole().getRoleName();
            }
            
            Map<String, Object> stats = bankNewsService.getNewsStats(userRole);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "stats", stats
            ));
        } catch (Exception e) {
            logger.error("Error fetching news statistics: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to fetch news statistics: " + e.getMessage()
            ));
        }
    }
    
    // Get categories (AJAX)
    @GetMapping("/categories")
    @ResponseBody
    public ResponseEntity<?> getCategories(Authentication authentication) {
        try {
            String userRole = "Customer"; // Default role
            
            if (authentication != null && authentication.isAuthenticated()) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                userRole = userDetails.getUser().getRole().getRoleName();
            }
            
            List<String> categories = bankNewsService.getDistinctCategories(userRole);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "categories", categories
            ));
        } catch (Exception e) {
            logger.error("Error fetching news categories: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to fetch news categories: " + e.getMessage()
            ));
        }
    }
}

