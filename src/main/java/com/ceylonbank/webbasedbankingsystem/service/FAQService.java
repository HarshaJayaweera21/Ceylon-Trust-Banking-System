package com.ceylonbank.webbasedbankingsystem.service;

import com.ceylonbank.webbasedbankingsystem.entity.FAQ;
import com.ceylonbank.webbasedbankingsystem.entity.User;
import com.ceylonbank.webbasedbankingsystem.repository.FAQRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class FAQService {
    private static final Logger logger = LoggerFactory.getLogger(FAQService.class);

    @Autowired
    private FAQRepository faqRepository;

    // Get all FAQs
    public List<FAQ> getAllFAQs() {
        try {
            return faqRepository.findAllByOrderByCreatedAtDesc();
        } catch (Exception e) {
            logger.error("Error fetching all FAQs: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch FAQs", e);
        }
    }

    // Get FAQs by category
    public List<FAQ> getFAQsByCategory(String category) {
        try {
            if (category == null || category.trim().isEmpty() || "All".equalsIgnoreCase(category)) {
                return getAllFAQs();
            }
            return faqRepository.findByCategoryIgnoreCaseOrderByCreatedAtDesc(category);
        } catch (Exception e) {
            logger.error("Error fetching FAQs by category '{}': {}", category, e.getMessage());
            throw new RuntimeException("Failed to fetch FAQs by category", e);
        }
    }

    // Get FAQ by ID
    public Optional<FAQ> getFAQById(Integer faqId) {
        try {
            return faqRepository.findById(faqId);
        } catch (Exception e) {
            logger.error("Error fetching FAQ with ID {}: {}", faqId, e.getMessage());
            throw new RuntimeException("Failed to fetch FAQ", e);
        }
    }

    // Search FAQs
    public List<FAQ> searchFAQs(String searchTerm) {
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return getAllFAQs();
            }
            return faqRepository.searchFAQs(searchTerm.trim());
        } catch (Exception e) {
            logger.error("Error searching FAQs with term '{}': {}", searchTerm, e.getMessage());
            throw new RuntimeException("Failed to search FAQs", e);
        }
    }

    // Get distinct categories
    public List<String> getDistinctCategories() {
        try {
            List<String> categories = faqRepository.findDistinctCategories();
            // Add "All" category at the beginning
            categories.add(0, "All");
            return categories;
        } catch (Exception e) {
            logger.error("Error fetching FAQ categories: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch FAQ categories", e);
        }
    }

    // Get FAQ statistics
    public Map<String, Object> getFAQStats() {
        try {
            long totalFAQs = faqRepository.count();
            List<String> categories = faqRepository.findDistinctCategories();
            long totalCategories = categories.size();
            
            // Get recent FAQs count (last 30 days)
            List<FAQ> recentFAQs = faqRepository.findRecentFAQs();
            long recentCount = recentFAQs.size();

            return Map.of(
                "totalFAQs", totalFAQs,
                "totalCategories", totalCategories,
                "recentCount", recentCount,
                "categories", categories
            );
        } catch (Exception e) {
            logger.error("Error fetching FAQ statistics: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch FAQ statistics", e);
        }
    }

    // Create new FAQ (for admin use)
    public FAQ createFAQ(String question, String answer, String category, User createdBy) {
        try {
            FAQ faq = new FAQ(question, answer, category, createdBy);
            return faqRepository.save(faq);
        } catch (Exception e) {
            logger.error("Error creating FAQ: {}", e.getMessage());
            throw new RuntimeException("Failed to create FAQ", e);
        }
    }

    // Update FAQ (for admin use)
    public FAQ updateFAQ(Integer faqId, String question, String answer, String category) {
        try {
            FAQ faq = faqRepository.findById(faqId)
                    .orElseThrow(() -> new RuntimeException("FAQ not found"));
            
            faq.setQuestion(question);
            faq.setAnswer(answer);
            faq.setCategory(category);
            faq.setUpdatedAt(LocalDateTime.now());
            
            return faqRepository.save(faq);
        } catch (Exception e) {
            logger.error("Error updating FAQ with ID {}: {}", faqId, e.getMessage());
            throw new RuntimeException("Failed to update FAQ", e);
        }
    }

    // Delete FAQ (for admin use)
    public void deleteFAQ(Integer faqId) {
        try {
            if (!faqRepository.existsById(faqId)) {
                throw new RuntimeException("FAQ not found");
            }
            faqRepository.deleteById(faqId);
        } catch (Exception e) {
            logger.error("Error deleting FAQ with ID {}: {}", faqId, e.getMessage());
            throw new RuntimeException("Failed to delete FAQ", e);
        }
    }

    // Get FAQs grouped by category
    public Map<String, List<FAQ>> getFAQsGroupedByCategory() {
        try {
            List<FAQ> allFAQs = getAllFAQs();
            return allFAQs.stream()
                    .collect(Collectors.groupingBy(
                            faq -> faq.getFormattedCategory(),
                            Collectors.toList()
                    ));
        } catch (Exception e) {
            logger.error("Error grouping FAQs by category: {}", e.getMessage());
            throw new RuntimeException("Failed to group FAQs by category", e);
        }
    }

    // Get recent FAQs
    public List<FAQ> getRecentFAQs() {
        try {
            return faqRepository.findRecentFAQs();
        } catch (Exception e) {
            logger.error("Error fetching recent FAQs: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch recent FAQs", e);
        }
    }
}
