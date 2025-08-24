package com.ceylonbank.webbasedbankingsystem.repository;

import com.ceylonbank.webbasedbankingsystem.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    
    // Find all feedback ordered by submission date (newest first)
    List<Feedback> findAllByOrderBySubmittedAtDesc();
    
    // Find feedback by user ID ordered by submission date
    List<Feedback> findByUserUserIdOrderBySubmittedAtDesc(Integer userId);
    
    // Find recent feedback (last 30 days)
    @Query("SELECT f FROM Feedback f WHERE f.submittedAt >= :date ORDER BY f.submittedAt DESC")
    List<Feedback> findRecentFeedback(@Param("date") LocalDateTime date);
    
    // Count total feedback
    long count();
    
    // Count feedback by user
    long countByUserUserId(Integer userId);
    
    // Count recent feedback (last 7 days)
    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.submittedAt >= :date")
    long countRecentFeedback(@Param("date") LocalDateTime date);
    
    // Find feedback with user details (eager loading)
    @Query("SELECT f FROM Feedback f LEFT JOIN FETCH f.user WHERE f.feedbackId = :feedbackId")
    Feedback findByIdWithUser(@Param("feedbackId") Integer feedbackId);
    
    // Find all feedback with user details (eager loading)
    @Query("SELECT f FROM Feedback f LEFT JOIN FETCH f.user ORDER BY f.submittedAt DESC")
    List<Feedback> findAllWithUser();
    
    // Find recent feedback with user details (eager loading)
    @Query("SELECT f FROM Feedback f LEFT JOIN FETCH f.user WHERE f.submittedAt >= :date ORDER BY f.submittedAt DESC")
    List<Feedback> findRecentFeedbackWithUser(@Param("date") LocalDateTime date);
    
    // Search feedback by message content
    @Query("SELECT f FROM Feedback f LEFT JOIN FETCH f.user WHERE f.message LIKE %:searchTerm% ORDER BY f.submittedAt DESC")
    List<Feedback> searchFeedbackByMessage(@Param("searchTerm") String searchTerm);
    
    // Find feedback by user name
    @Query("SELECT f FROM Feedback f LEFT JOIN FETCH f.user WHERE " +
           "LOWER(f.user.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(f.user.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(CONCAT(f.user.firstName, ' ', f.user.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY f.submittedAt DESC")
    List<Feedback> searchFeedbackByUserName(@Param("searchTerm") String searchTerm);
    
    // Delete all feedback by user ID
    void deleteByUserUserId(Integer userId);
}

