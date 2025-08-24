package com.ceylonbank.webbasedbankingsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Feedback")
public class Feedback {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FeedbackID")
    private Integer feedbackId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private User user;
    
    @Column(name = "Message", columnDefinition = "TEXT", nullable = false)
    private String message;
    
    @Column(name = "SubmittedAt", nullable = false)
    private LocalDateTime submittedAt;
    
    // Constructors
    public Feedback() {
        this.submittedAt = LocalDateTime.now();
    }
    
    public Feedback(User user, String message) {
        this();
        this.user = user;
        this.message = message;
    }
    
    // Getters and Setters
    public Integer getFeedbackId() {
        return feedbackId;
    }
    
    public void setFeedbackId(Integer feedbackId) {
        this.feedbackId = feedbackId;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
    
    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
    
    // Helper methods
    public String getFormattedSubmittedAt() {
        if (submittedAt == null) {
            return "Unknown";
        }
        return submittedAt.toString();
    }
    
    public boolean isRecent() {
        if (submittedAt == null) {
            return false;
        }
        return submittedAt.isAfter(LocalDateTime.now().minusDays(7));
    }
    
    public String getTruncatedMessage(int maxLength) {
        if (message == null) {
            return "";
        }
        if (message.length() <= maxLength) {
            return message;
        }
        return message.substring(0, maxLength) + "...";
    }
    
    @Override
    public String toString() {
        return "Feedback{" +
                "feedbackId=" + feedbackId +
                ", user=" + (user != null ? user.getUserId() : null) +
                ", message='" + getTruncatedMessage(50) + '\'' +
                ", submittedAt=" + submittedAt +
                '}';
    }
}

