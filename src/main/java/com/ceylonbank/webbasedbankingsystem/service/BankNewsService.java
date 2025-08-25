package com.ceylonbank.webbasedbankingsystem.service;

import com.ceylonbank.webbasedbankingsystem.entity.BankNews;
import com.ceylonbank.webbasedbankingsystem.entity.User;
import com.ceylonbank.webbasedbankingsystem.observer.NewsSubject;
import com.ceylonbank.webbasedbankingsystem.repository.BankNewsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class BankNewsService {
    private static final Logger logger = LoggerFactory.getLogger(BankNewsService.class);

    @Autowired
    private BankNewsRepository bankNewsRepository;
    
    @Autowired
    private NewsSubject newsSubject; // Inject the observer subject

    // Get all visible news for a specific role
    public List<BankNews> getVisibleNews(String userRole) {
        try {
            if ("Customer".equalsIgnoreCase(userRole)) {
                return bankNewsRepository.findAllNonExpiredPublicOrderByPostedAtDesc();
            } else {
                // Staff can see both public and staff news
                List<BankNews> publicNews = bankNewsRepository.findAllNonExpiredPublicOrderByPostedAtDesc();
                List<BankNews> staffNews = bankNewsRepository.findAllNonExpiredStaffOrderByPostedAtDesc();
                
                // Combine and sort by posted date
                publicNews.addAll(staffNews);
                return publicNews.stream()
                    .sorted((a, b) -> b.getPostedAt().compareTo(a.getPostedAt()))
                    .collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.error("Error fetching visible news for role '{}': {}", userRole, e.getMessage());
            throw new RuntimeException("Failed to fetch news", e);
        }
    }

    // Get news by category for a specific role
    public List<BankNews> getNewsByCategory(String category, String userRole) {
        try {
            if ("Customer".equalsIgnoreCase(userRole)) {
                return bankNewsRepository.findPublicByCategoryAndNonExpiredOrderByPostedAtDesc(category);
            } else {
                // Staff can see both public and staff news
                List<BankNews> publicNews = bankNewsRepository.findPublicByCategoryAndNonExpiredOrderByPostedAtDesc(category);
                List<BankNews> staffNews = bankNewsRepository.findStaffByCategoryAndNonExpiredOrderByPostedAtDesc(category);
                
                // Combine and sort by posted date
                publicNews.addAll(staffNews);
                return publicNews.stream()
                    .sorted((a, b) -> b.getPostedAt().compareTo(a.getPostedAt()))
                    .collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.error("Error fetching news by category '{}' for role '{}': {}", category, userRole, e.getMessage());
            throw new RuntimeException("Failed to fetch news by category", e);
        }
    }

    // Get news by ID
    public Optional<BankNews> getNewsById(Integer newsId) {
        try {
            return bankNewsRepository.findById(newsId);
        } catch (Exception e) {
            logger.error("Error fetching news with ID {}: {}", newsId, e.getMessage());
            throw new RuntimeException("Failed to fetch news", e);
        }
    }

    // Search news for a specific role
    public List<BankNews> searchNews(String searchTerm, String userRole) {
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return getVisibleNews(userRole);
            }
            
            if ("Customer".equalsIgnoreCase(userRole)) {
                return bankNewsRepository.searchNonExpiredPublicNews(searchTerm.trim());
            } else {
                // Staff can see both public and staff news
                List<BankNews> publicNews = bankNewsRepository.searchNonExpiredPublicNews(searchTerm.trim());
                List<BankNews> staffNews = bankNewsRepository.searchNonExpiredStaffNews(searchTerm.trim());
                
                // Combine and sort by posted date
                publicNews.addAll(staffNews);
                return publicNews.stream()
                    .sorted((a, b) -> b.getPostedAt().compareTo(a.getPostedAt()))
                    .collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.error("Error searching news with term '{}' for role '{}': {}", searchTerm, userRole, e.getMessage());
            throw new RuntimeException("Failed to search news", e);
        }
    }

    // Get distinct categories for a specific role
    public List<String> getDistinctCategories(String userRole) {
        try {
            if ("Customer".equalsIgnoreCase(userRole)) {
                return bankNewsRepository.findDistinctPublicCategoriesNonExpired();
            } else {
                // Staff can see both public and staff categories
                List<String> publicCategories = bankNewsRepository.findDistinctPublicCategoriesNonExpired();
                List<String> staffCategories = bankNewsRepository.findDistinctStaffCategoriesNonExpired();
                
                // Combine and remove duplicates
                publicCategories.addAll(staffCategories);
                return publicCategories.stream()
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.error("Error fetching news categories for role '{}': {}", userRole, e.getMessage());
            throw new RuntimeException("Failed to fetch news categories", e);
        }
    }

    // Get news statistics for a specific role
    public Map<String, Object> getNewsStats(String userRole) {
        try {
            long totalNews, recentCount;
            List<String> categories;
            
            if ("Customer".equalsIgnoreCase(userRole)) {
                totalNews = bankNewsRepository.countNonExpiredPublicNews();
                recentCount = bankNewsRepository.findRecentNonExpiredPublicNews().size();
                categories = bankNewsRepository.findDistinctPublicCategoriesNonExpired();
            } else {
                totalNews = bankNewsRepository.countNonExpiredNews();
                recentCount = bankNewsRepository.findRecentNonExpiredNews().size();
                List<String> publicCategories = bankNewsRepository.findDistinctPublicCategoriesNonExpired();
                List<String> staffCategories = bankNewsRepository.findDistinctStaffCategoriesNonExpired();
                publicCategories.addAll(staffCategories);
                categories = publicCategories.stream().distinct().sorted().collect(Collectors.toList());
            }

            return Map.of(
                "totalNews", totalNews,
                "recentCount", recentCount,
                "categories", categories,
                "userRole", userRole
            );
        } catch (Exception e) {
            logger.error("Error fetching news statistics for role '{}': {}", userRole, e.getMessage());
            throw new RuntimeException("Failed to fetch news statistics", e);
        }
    }

    // Create new news (for admin use)
    public BankNews createNews(String title, String content, String category, User postedBy, LocalDate expiryDate, Boolean isPublic) {
        try {
            BankNews news = new BankNews();
            news.setTitle(title);
            news.setContent(content);
            news.setCategory(category);
            news.setPostedBy(postedBy);
            news.setExpiryDate(expiryDate);
            news.setIsPublic(isPublic != null ? isPublic : true);
            news.setPostedAt(LocalDateTime.now());
            
            return bankNewsRepository.save(news);
        } catch (Exception e) {
            logger.error("Error creating news: {}", e.getMessage());
            throw new RuntimeException("Failed to create news", e);
        }
    }

    // Update news (for admin use)
    public BankNews updateNews(Integer newsId, String title, String content, String category, LocalDate expiryDate, Boolean isPublic) {
        try {
            BankNews news = bankNewsRepository.findById(newsId)
                    .orElseThrow(() -> new RuntimeException("News not found"));
            
            news.setTitle(title);
            news.setContent(content);
            news.setCategory(category);
            news.setExpiryDate(expiryDate);
            news.setIsPublic(isPublic != null ? isPublic : true);
            
            return bankNewsRepository.save(news);
        } catch (Exception e) {
            logger.error("Error updating news with ID {}: {}", newsId, e.getMessage());
            throw new RuntimeException("Failed to update news", e);
        }
    }


    // Get news grouped by category for a specific role
    public Map<String, List<BankNews>> getNewsGroupedByCategory(String userRole) {
        try {
            List<BankNews> allNews = getVisibleNews(userRole);
            return allNews.stream()
                    .collect(Collectors.groupingBy(
                            news -> news.getFormattedCategory(),
                            Collectors.toList()
                    ));
        } catch (Exception e) {
            logger.error("Error grouping news by category for role '{}': {}", userRole, e.getMessage());
            throw new RuntimeException("Failed to group news by category", e);
        }
    }

    // Get recent news for a specific role
    public List<BankNews> getRecentNews(String userRole) {
        try {
            if ("Customer".equalsIgnoreCase(userRole)) {
                return bankNewsRepository.findRecentNonExpiredPublicNews();
            } else {
                // Staff can see both public and staff news
                List<BankNews> publicNews = bankNewsRepository.findRecentNonExpiredPublicNews();
                List<BankNews> staffNews = bankNewsRepository.findRecentNonExpiredStaffNews();
                
                // Combine and sort by posted date
                publicNews.addAll(staffNews);
                return publicNews.stream()
                    .sorted((a, b) -> b.getPostedAt().compareTo(a.getPostedAt()))
                    .collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.error("Error fetching recent news for role '{}': {}", userRole, e.getMessage());
            throw new RuntimeException("Failed to fetch recent news", e);
        }
    }

    // Check if news is visible to a specific role
    public boolean isNewsVisibleToRole(BankNews news, String userRole) {
        if (news == null) {
            return false;
        }
        return news.isVisibleToRole(userRole);
    }
    
    // Get distinct categories for admin (all news)
    public List<String> getDistinctCategoriesForAdmin() {
        return bankNewsRepository.findDistinctCategoriesForAdmin();
    }
    
    // Get all news for admin (including expired)
    public List<BankNews> getAllNewsForAdmin() {
        return bankNewsRepository.findAllByOrderByPostedAtDesc();
    }
    
    // Get news statistics for admin
    public Map<String, Object> getNewsStatsForAdmin() {
        long totalNews = bankNewsRepository.count();
        long activeNews = bankNewsRepository.countActiveNews();
        long expiredNews = bankNewsRepository.countExpiredNews();
        long publicNews = bankNewsRepository.countPublicNews();
        long staffNews = bankNewsRepository.countStaffNews();
        
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalNews", totalNews);
        stats.put("activeNews", activeNews);
        stats.put("expiredNews", expiredNews);
        stats.put("publicNews", publicNews);
        stats.put("staffNews", staffNews);
        
        return stats;
    }
    
    // Create news
    public BankNews createNews(String title, String content, String category, User postedBy, LocalDate expiryDate, boolean isPublic) {
        BankNews news = new BankNews();
        news.setTitle(title);
        news.setContent(content);
        news.setCategory(category);
        news.setPostedBy(postedBy);
        news.setExpiryDate(expiryDate);
        news.setIsPublic(isPublic);
        news.setPostedAt(java.time.LocalDateTime.now());
        
        // Save the news first
        BankNews savedNews = bankNewsRepository.save(news);
        
        // Notify all observers about the new news
        newsSubject.notifyObservers(savedNews);
        
        return savedNews;
    }
    
    // Update news
    public BankNews updateNews(Integer newsId, String title, String content, String category, LocalDate expiryDate, boolean isPublic) {
        Optional<BankNews> newsOpt = bankNewsRepository.findById(newsId);
        if (newsOpt.isEmpty()) {
            throw new RuntimeException("News not found with ID: " + newsId);
        }
        
        BankNews news = newsOpt.get();
        news.setTitle(title);
        news.setContent(content);
        news.setCategory(category);
        news.setExpiryDate(expiryDate);
        news.setIsPublic(isPublic);
        
        return bankNewsRepository.save(news);
    }
    
    // Delete news
    public void deleteNews(Integer newsId) {
        if (!bankNewsRepository.existsById(newsId)) {
            throw new RuntimeException("News not found with ID: " + newsId);
        }
        bankNewsRepository.deleteById(newsId);
    }
}
