package com.ceylonbank.webbasedbankingsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SupportTickets")
public class SupportTicket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TicketID")
    private Integer ticketId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private User user;
    
    @Column(name = "Subject", nullable = false, length = 100)
    private String subject;
    
    @Column(name = "Message", nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "Status", nullable = false, length = 20)
    private String status = "Open";
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AssignedTo")
    private User assignedTo;
    
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "ResolvedAt")
    private LocalDateTime resolvedAt;
    
    // Constructors
    public SupportTicket() {}
    
    public SupportTicket(User user, String subject, String message) {
        this.user = user;
        this.subject = subject;
        this.message = message;
        this.status = "Open";
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Integer getTicketId() {
        return ticketId;
    }
    
    public void setTicketId(Integer ticketId) {
        this.ticketId = ticketId;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public User getAssignedTo() {
        return assignedTo;
    }
    
    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
    
    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
    
    // Helper methods
    public boolean isOpen() {
        return "Open".equals(status);
    }
    
    public boolean isResolved() {
        return "Resolved".equals(status);
    }
    
    public void markAsResolved() {
        this.status = "Resolved";
        this.resolvedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "SupportTicket{" +
                "ticketId=" + ticketId +
                ", subject='" + subject + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
