package com.ceylonbank.webbasedbankingsystem.controller;

import com.ceylonbank.webbasedbankingsystem.entity.Notification;
import com.ceylonbank.webbasedbankingsystem.security.CustomUserDetails;
import com.ceylonbank.webbasedbankingsystem.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @GetMapping
    public ResponseEntity<?> getNotifications(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Authentication required"));
        }
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Integer userId = userDetails.getUserId();
        
        List<Notification> notifications = notificationService.getNotificationsByUserId(userId);
        long unreadCount = notificationService.getUnreadCount(userId);
        
        return ResponseEntity.ok(Map.of(
            "notifications", notifications,
            "unreadCount", unreadCount
        ));
    }
    
    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Authentication required"));
        }
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Integer userId = userDetails.getUserId();
        
        List<Notification> unreadNotifications = notificationService.getUnreadNotificationsByUserId(userId);
        
        return ResponseEntity.ok(Map.of(
            "notifications", unreadNotifications,
            "count", unreadNotifications.size()
        ));
    }
    
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentNotifications(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Authentication required"));
        }
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Integer userId = userDetails.getUserId();
        
        List<Notification> recentNotifications = notificationService.getRecentNotificationsByUserId(userId, 5);
        long unreadCount = notificationService.getUnreadCount(userId);
        
        return ResponseEntity.ok(Map.of(
            "notifications", recentNotifications,
            "unreadCount", unreadCount
        ));
    }
    
    @GetMapping("/all")
    public ResponseEntity<?> getAllNotifications(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Authentication required"));
        }
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Integer userId = userDetails.getUserId();
        
        List<Notification> allNotifications = notificationService.getAllNotificationsByUserId(userId);
        long unreadCount = notificationService.getUnreadCount(userId);
        
        return ResponseEntity.ok(Map.of(
            "notifications", allNotifications,
            "unreadCount", unreadCount
        ));
    }
    
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Integer notificationId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Authentication required"));
        }
        
        Notification notification = notificationService.markAsRead(notificationId);
        if (notification != null) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Notification marked as read"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Notification not found"));
        }
    }
    
    @PostMapping("/mark-all-read")
    public ResponseEntity<?> markAllAsRead(Authentication authentication) {
        System.out.println("markAllAsRead endpoint called");
        System.out.println("Authentication: " + authentication);
        
        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("Authentication failed");
            return ResponseEntity.badRequest().body(Map.of("error", "Authentication required"));
        }
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Integer userId = userDetails.getUserId();
        
        System.out.println("Marking all notifications as read for user: " + userId);
        notificationService.markAllAsRead(userId);
        
        return ResponseEntity.ok(Map.of("success", true, "message", "All notifications marked as read"));
    }
    
    @GetMapping("/count")
    public ResponseEntity<?> getUnreadCount(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Authentication required"));
        }
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Integer userId = userDetails.getUserId();
        
        long unreadCount = notificationService.getUnreadCount(userId);
        
        return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
    }
}
