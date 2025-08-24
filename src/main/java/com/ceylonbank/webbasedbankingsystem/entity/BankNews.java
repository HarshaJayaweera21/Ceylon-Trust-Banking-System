package com.ceylonbank.webbasedbankingsystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "BankNews")
@Data
public class BankNews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NewsID")
    private Integer newsId;

    @Column(name = "Title", nullable = false, length = 100)
    private String title;

    @Column(name = "Content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "Category", nullable = false, length = 50)
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PostedBy", nullable = false)
    private User postedBy;

    @Column(name = "PostedAt", nullable = false)
    private LocalDateTime postedAt;

    @Column(name = "ExpiryDate")
    private LocalDate expiryDate;

    @Column(name = "IsPublic", nullable = false)
    private Boolean isPublic;

    // Constructors
    public BankNews() {
        this.postedAt = LocalDateTime.now();
        this.isPublic = true;
    }

    public BankNews(String title, String content, String category, User postedBy) {
        this();
        this.title = title;
        this.content = content;
        this.category = category;
        this.postedBy = postedBy;
    }

    // Helper methods
    public boolean isExpired() {
        if (expiryDate == null) {
            return false;
        }
        return LocalDate.now().isAfter(expiryDate);
    }

    public boolean isVisibleToRole(String role) {
        // If expired, not visible to anyone
        if (isExpired()) {
            return false;
        }
        
        // If public, visible to everyone
        if (isPublic) {
            return true;
        }
        
        // If not public, only visible to staff (not customers)
        return !"Customer".equalsIgnoreCase(role);
    }

    public String getFormattedCategory() {
        if (category == null || category.trim().isEmpty()) {
            return "General";
        }
        return category;
    }

    public String getTruncatedContent(int maxLength) {
        if (content == null) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }

    public String getFormattedPostedDate() {
        if (postedAt == null) {
            return "Unknown";
        }
        return postedAt.toLocalDate().toString();
    }

    public String getFormattedExpiryDate() {
        if (expiryDate == null) {
            return "No expiry";
        }
        return expiryDate.toString();
    }
}

