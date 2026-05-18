package com.example.TeacherPlatform.repository;

import com.example.TeacherPlatform.model.Notification;
import com.example.TeacherPlatform.model.enums.NotificationType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends BaseRepository<Notification> {
    
    List<Notification> findByUserId(Long userId);
    
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.read = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadNotificationsByUser(@Param("userId") Long userId);
    
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId ORDER BY n.createdAt DESC LIMIT 10")
    List<Notification> findRecentNotificationsByUser(@Param("userId") Long userId);
    
    List<Notification> findByType(NotificationType type);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.read = false")
    Long countUnreadNotifications(@Param("userId") Long userId);
    
    @Query("SELECT n FROM Notification n WHERE n.type = :type ORDER BY n.createdAt DESC")
    List<Notification> findByTypeOrdered(@Param("type") NotificationType type);
}



