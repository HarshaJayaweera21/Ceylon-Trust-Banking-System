package com.ceylonbank.webbasedbankingsystem.controller;

import com.ceylonbank.webbasedbankingsystem.entity.BankNews;
import com.ceylonbank.webbasedbankingsystem.entity.User;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/news")
public class NewsAdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(NewsAdminController.class);
    
    @Autowired
    private BankNewsService bankNewsService;
    
    // Display news management page
    @GetMapping
    public String showNewsManagementPage(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Unauthenticated access attempt to news management page");
            return "redirect:/login";
        }
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User admin = userDetails.getUser();
        model.addAttribute("admin", admin);
        
        try {
            // Get all news (including expired for admin)
            List<BankNews> allNews = bankNewsService.getAllNewsForAdmin();
            model.addAttribute("news", allNews);
            
            // Get categories
            List<String> categories = bankNewsService.getDistinctCategoriesForAdmin();
            model.addAttribute("categories", categories);
            
            // Get statistics
            Map<String, Object> stats = bankNewsService.getNewsStatsForAdmin();
            model.addAttribute("stats", stats);
            
        } catch (Exception e) {
            logger.error("Error loading news management page: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading news management page. Please try again later.");
            model.addAttribute("news", List.of());
            model.addAttribute("categories", List.of());
            model.addAttribute("stats", Map.of(
                "totalNews", 0L,
                "activeNews", 0L,
                "expiredNews", 0L,
                "publicNews", 0L,
                "staffNews", 0L
            ));
        }
        
        return "news-admin";
    }
    
    // Create new news
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<?> createNews(@RequestBody Map<String, Object> request, Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User admin = userDetails.getUser();
            
            String title = (String) request.get("title");
            String content = (String) request.get("content");
            String category = (String) request.get("category");
            String expiryDateStr = (String) request.get("expiryDate");
            Boolean isPublic = (Boolean) request.get("isPublic");
            
            // Validate input
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Title is required"));
            }
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Content is required"));
            }
            if (category == null || category.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Category is required"));
            }
            
            // Parse expiry date
            LocalDate expiryDate = null;
            if (expiryDateStr != null && !expiryDateStr.trim().isEmpty()) {
                try {
                    expiryDate = LocalDate.parse(expiryDateStr);
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid expiry date format"));
                }
            }
            
            BankNews newNews = bankNewsService.createNews(
                title.trim(), 
                content.trim(), 
                category.trim(), 
                admin, 
                expiryDate, 
                isPublic != null ? isPublic : true
            );
            
            // Return simplified news data
            Map<String, Object> newsData = new java.util.HashMap<>();
            newsData.put("newsId", newNews.getNewsId());
            newsData.put("title", newNews.getTitle());
            newsData.put("content", newNews.getContent());
            newsData.put("category", newNews.getCategory());
            newsData.put("postedAt", newNews.getPostedAt());
            newsData.put("expiryDate", newNews.getExpiryDate());
            newsData.put("isPublic", newNews.getIsPublic());
            newsData.put("isExpired", newNews.isExpired());
            newsData.put("postedBy", newNews.getPostedBy() != null ? 
                newNews.getPostedBy().getFirstName() + " " + newNews.getPostedBy().getLastName() : "Unknown");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "News created successfully",
                "news", newsData
            ));
        } catch (Exception e) {
            logger.error("Error creating news: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to create news: " + e.getMessage()
            ));
        }
    }
    
    // Update existing news
    @PutMapping("/update/{newsId}")
    @ResponseBody
    public ResponseEntity<?> updateNews(@PathVariable Integer newsId, @RequestBody Map<String, Object> request) {
        try {
            String title = (String) request.get("title");
            String content = (String) request.get("content");
            String category = (String) request.get("category");
            String expiryDateStr = (String) request.get("expiryDate");
            Boolean isPublic = (Boolean) request.get("isPublic");
            
            // Validate input
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Title is required"));
            }
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Content is required"));
            }
            if (category == null || category.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Category is required"));
            }
            
            // Parse expiry date
            LocalDate expiryDate = null;
            if (expiryDateStr != null && !expiryDateStr.trim().isEmpty()) {
                try {
                    expiryDate = LocalDate.parse(expiryDateStr);
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid expiry date format"));
                }
            }
            
            BankNews updatedNews = bankNewsService.updateNews(
                newsId, 
                title.trim(), 
                content.trim(), 
                category.trim(), 
                expiryDate, 
                isPublic != null ? isPublic : true
            );
            
            // Return simplified news data
            Map<String, Object> newsData = new java.util.HashMap<>();
            newsData.put("newsId", updatedNews.getNewsId());
            newsData.put("title", updatedNews.getTitle());
            newsData.put("content", updatedNews.getContent());
            newsData.put("category", updatedNews.getCategory());
            newsData.put("postedAt", updatedNews.getPostedAt());
            newsData.put("expiryDate", updatedNews.getExpiryDate());
            newsData.put("isPublic", updatedNews.getIsPublic());
            newsData.put("isExpired", updatedNews.isExpired());
            newsData.put("postedBy", updatedNews.getPostedBy() != null ? 
                updatedNews.getPostedBy().getFirstName() + " " + updatedNews.getPostedBy().getLastName() : "Unknown");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "News updated successfully",
                "news", newsData
            ));
        } catch (Exception e) {
            logger.error("Error updating news with ID {}: {}", newsId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to update news: " + e.getMessage()
            ));
        }
    }
    
    // Delete news
    @DeleteMapping("/delete/{newsId}")
    @ResponseBody
    public ResponseEntity<?> deleteNews(@PathVariable Integer newsId) {
        try {
            bankNewsService.deleteNews(newsId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "News deleted successfully"
            ));
        } catch (Exception e) {
            logger.error("Error deleting news with ID {}: {}", newsId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to delete news: " + e.getMessage()
            ));
        }
    }
    
    // Get news details for editing
    @GetMapping("/{newsId}")
    @ResponseBody
    public ResponseEntity<?> getNewsForEdit(@PathVariable Integer newsId) {
        try {
            Optional<BankNews> newsOpt = bankNewsService.getNewsById(newsId);
            if (newsOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            BankNews news = newsOpt.get();
            Map<String, Object> newsData = new java.util.HashMap<>();
            newsData.put("newsId", news.getNewsId());
            newsData.put("title", news.getTitle());
            newsData.put("content", news.getContent());
            newsData.put("category", news.getCategory());
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
            logger.error("Error fetching news for edit with ID {}: {}", newsId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to fetch news: " + e.getMessage()
            ));
        }
    }
    
    // Get all news (AJAX)
    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<?> getAllNews() {
        try {
            List<BankNews> news = bankNewsService.getAllNewsForAdmin();
            
            // Convert to simplified format for JSON response
            List<Map<String, Object>> newsData = news.stream()
                .map(newsItem -> {
                    Map<String, Object> newsMap = new java.util.HashMap<>();
                    newsMap.put("newsId", newsItem.getNewsId());
                    newsMap.put("title", newsItem.getTitle());
                    newsMap.put("content", newsItem.getContent());
                    newsMap.put("category", newsItem.getCategory());
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
                "news", newsData
            ));
        } catch (Exception e) {
            logger.error("Error fetching all news: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to fetch news: " + e.getMessage()
            ));
        }
    }
    
    // Get categories (AJAX)
    @GetMapping("/categories")
    @ResponseBody
    public ResponseEntity<?> getCategories() {
        try {
            List<String> categories = bankNewsService.getDistinctCategoriesForAdmin();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "categories", categories
            ));
        } catch (Exception e) {
            logger.error("Error fetching news categories: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to fetch categories: " + e.getMessage()
            ));
        }
    }
}

