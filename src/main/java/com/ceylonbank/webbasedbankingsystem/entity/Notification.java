package com.ceylonbank.webbasedbankingsystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Notifications")
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NotificationID")
    private Integer notificationId;

    @Column(name = "UserID", nullable = false)
    private Integer userId;

    @Column(name = "Message", nullable = false, length = 255)
    private String message;

    @Column(name = "Type", length = 20)
    private String type;

    @CreationTimestamp
    @Column(name = "SentAt", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "IsRead", nullable = false)
    private Boolean isRead = false;
}


