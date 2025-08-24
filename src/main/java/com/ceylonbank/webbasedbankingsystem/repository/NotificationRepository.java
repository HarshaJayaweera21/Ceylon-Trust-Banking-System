package com.ceylonbank.webbasedbankingsystem.repository;

import com.ceylonbank.webbasedbankingsystem.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    
    List<Notification> findByUserIdOrderBySentAtDesc(Integer userId);
    
    List<Notification> findByUserIdAndIsReadFalseOrderBySentAtDesc(Integer userId);
    
    long countByUserIdAndIsReadFalse(Integer userId);
    
    List<Notification> findTop5ByUserIdOrderBySentAtDesc(Integer userId);
    
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.type = :type ORDER BY n.sentAt DESC")
    List<Notification> findByUserIdAndTypeOrderBySentAtDesc(@Param("userId") Integer userId, @Param("type") String type);
    
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isRead = false AND n.type = :type")
    List<Notification> findUnreadByUserIdAndType(@Param("userId") Integer userId, @Param("type") String type);
}



