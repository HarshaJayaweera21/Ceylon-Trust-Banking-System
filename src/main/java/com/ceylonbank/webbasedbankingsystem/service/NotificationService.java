package com.ceylonbank.webbasedbankingsystem.service;

import com.ceylonbank.webbasedbankingsystem.entity.Notification;
import com.ceylonbank.webbasedbankingsystem.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    public List<Notification> getNotificationsByUserId(Integer userId) {
        return notificationRepository.findByUserIdOrderBySentAtDesc(userId);
    }
    
    public List<Notification> getUnreadNotificationsByUserId(Integer userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderBySentAtDesc(userId);
    }
    
    public Notification markAsRead(Integer notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setIsRead(true);
            return notificationRepository.save(notification);
        }
        return null;
    }
    
    public void markAllAsRead(Integer userId) {
        List<Notification> unreadNotifications = getUnreadNotificationsByUserId(userId);
        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }
    
    public long getUnreadCount(Integer userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }
    
    public Notification createNotification(Integer userId, String message, String type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setMessage(message);
        notification.setType(type);
        notification.setIsRead(false);
        return notificationRepository.save(notification);
    }
    
    public List<Notification> getRecentNotificationsByUserId(Integer userId, int limit) {
        return notificationRepository.findTop5ByUserIdOrderBySentAtDesc(userId);
    }
    
    public List<Notification> getAllNotificationsByUserId(Integer userId) {
        return notificationRepository.findByUserIdOrderBySentAtDesc(userId);
    }
}
