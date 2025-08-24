package com.ceylonbank.webbasedbankingsystem.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "AuditLogs")
@Data
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LogID")
    private Integer logId;

    @Column(name = "UserID")
    private Integer userId;
    
    @Column(name = "Action")
    private String action;
    
    @Column(name = "Details")
    private String details;
    
    @Column(name = "Timestamp")
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
