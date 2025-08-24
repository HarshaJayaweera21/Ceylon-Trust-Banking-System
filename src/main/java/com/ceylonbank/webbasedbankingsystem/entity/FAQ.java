package com.ceylonbank.webbasedbankingsystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "FAQs")
@Data
public class FAQ {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FaqID")
    private Integer faqId;

    @Column(name = "Question", nullable = false, length = 255)
    private String question;

    @Column(name = "Answer", nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(name = "Category", length = 50)
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CreatedBy", nullable = false)
    private User createdBy;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    // Constructors
    public FAQ() {
        this.createdAt = LocalDateTime.now();
    }

    public FAQ(String question, String answer, String category, User createdBy) {
        this();
        this.question = question;
        this.answer = answer;
        this.category = category;
        this.createdBy = createdBy;
    }

    // Helper methods
    public boolean isRecentlyUpdated() {
        return updatedAt != null && updatedAt.isAfter(createdAt);
    }

    public String getFormattedCategory() {
        if (category == null || category.trim().isEmpty()) {
            return "General";
        }
        return category;
    }
}
