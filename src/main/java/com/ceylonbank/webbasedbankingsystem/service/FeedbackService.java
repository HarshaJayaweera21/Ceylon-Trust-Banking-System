package com.ceylonbank.webbasedbankingsystem.service;

import com.ceylonbank.webbasedbankingsystem.entity.Feedback;
import com.ceylonbank.webbasedbankingsystem.entity.User;
import com.ceylonbank.webbasedbankingsystem.repository.FeedbackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FeedbackService {
    
    private static final Logger logger = LoggerFactory.getLogger(FeedbackService.class);
    
    @Autowired
    private FeedbackRepository feedbackRepository;
    
    // Submit new feedback
    public Feedback submitFeedback(User user, String message) {
        try {
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }
            if (message == null || message.trim().isEmpty()) {
                throw new IllegalArgumentException("Message cannot be empty");
            }
            
            Feedback feedback = new Feedback(user, message.trim());
            Feedback savedFeedback = feedbackRepository.save(feedback);
            
            logger.info("Feedback submitted successfully by user {}: {}", user.getUserId(), savedFeedback.getFeedbackId());
            return savedFeedback;
        } catch (Exception e) {
            logger.error("Error submitting feedback for user {}: {}", user != null ? user.getUserId() : "null", e.getMessage(), e);
            throw new RuntimeException("Failed to submit feedback", e);
        }
    }
    
    // Get all feedback (for admin/manager)
    public List<Feedback> getAllFeedback() {
        try {
            return feedbackRepository.findAllWithUser();
        } catch (Exception e) {
            logger.error("Error fetching all feedback: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch feedback", e);
        }
    }
    
    // Get feedback by user
    public List<Feedback> getFeedbackByUser(Integer userId) {
        try {
            return feedbackRepository.findByUserUserIdOrderBySubmittedAtDesc(userId);
        } catch (Exception e) {
            logger.error("Error fetching feedback for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch user feedback", e);
        }
    }
    
    // Get recent feedback (last 30 days)
    public List<Feedback> getRecentFeedback() {
        try {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            return feedbackRepository.findRecentFeedbackWithUser(thirtyDaysAgo);
        } catch (Exception e) {
            logger.error("Error fetching recent feedback: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch recent feedback", e);
        }
    }
    
    // Get feedback by ID
    public Optional<Feedback> getFeedbackById(Integer feedbackId) {
        try {
            return Optional.ofNullable(feedbackRepository.findByIdWithUser(feedbackId));
        } catch (Exception e) {
            logger.error("Error fetching feedback with ID {}: {}", feedbackId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch feedback", e);
        }
    }
    
    // Delete feedback
    public void deleteFeedback(Integer feedbackId) {
        try {
            if (!feedbackRepository.existsById(feedbackId)) {
                throw new RuntimeException("Feedback not found with ID: " + feedbackId);
            }
            feedbackRepository.deleteById(feedbackId);
            logger.info("Feedback deleted successfully: {}", feedbackId);
        } catch (Exception e) {
            logger.error("Error deleting feedback with ID {}: {}", feedbackId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete feedback", e);
        }
    }
    
    // Search feedback by message content
    public List<Feedback> searchFeedbackByMessage(String searchTerm) {
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return getAllFeedback();
            }
            return feedbackRepository.searchFeedbackByMessage(searchTerm.trim());
        } catch (Exception e) {
            logger.error("Error searching feedback by message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search feedback", e);
        }
    }
    
    // Search feedback by user name
    public List<Feedback> searchFeedbackByUserName(String searchTerm) {
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return getAllFeedback();
            }
            return feedbackRepository.searchFeedbackByUserName(searchTerm.trim());
        } catch (Exception e) {
            logger.error("Error searching feedback by user name: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search feedback", e);
        }
    }
    
    // Get feedback statistics
    public Map<String, Object> getFeedbackStats() {
        try {
            long totalFeedback = feedbackRepository.count();
            long recentFeedback = feedbackRepository.countRecentFeedback(LocalDateTime.now().minusDays(7));
            long todayFeedback = feedbackRepository.countRecentFeedback(LocalDateTime.now().minusDays(1));
            
            Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("totalFeedback", totalFeedback);
            stats.put("recentFeedback", recentFeedback);
            stats.put("todayFeedback", todayFeedback);
            
            return stats;
        } catch (Exception e) {
            logger.error("Error fetching feedback statistics: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch feedback statistics", e);
        }
    }
    
    // Get feedback statistics for a specific user
    public Map<String, Object> getFeedbackStatsForUser(Integer userId) {
        try {
            long userFeedbackCount = feedbackRepository.countByUserUserId(userId);
            
            Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("userFeedbackCount", userFeedbackCount);
            
            return stats;
        } catch (Exception e) {
            logger.error("Error fetching feedback statistics for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch user feedback statistics", e);
        }
    }
}

