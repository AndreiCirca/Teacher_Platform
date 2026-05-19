package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.NotificationRequest;
import com.example.TeacherPlatform.dataTransferObject.NotificationResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.Notification;
import com.example.TeacherPlatform.model.User;
import com.example.TeacherPlatform.model.enums.NotificationType;
import com.example.TeacherPlatform.repository.BaseRepository;
import com.example.TeacherPlatform.repository.NotificationRepository;
import com.example.TeacherPlatform.repository.UserRepository;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService extends GenericService<Notification, NotificationRequest, NotificationResponse> {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    protected BaseRepository<Notification> getRepository() {
        return notificationRepository;
    }

    @Override
    protected Notification toEntity(NotificationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setType(request.getType());
        notification.setRead(false);
        notification.setActionUrl(request.getActionUrl());
        return notification;
    }

    @Override
    protected NotificationResponse toResponse(Notification entity) {
        NotificationResponse response = new NotificationResponse();
        response.setId(entity.getId());
        response.setUserId(entity.getUser().getId());
        response.setTitle(entity.getTitle());
        response.setMessage(entity.getMessage());
        response.setType(entity.getType());
        response.setRead(entity.getRead());
        response.setActionUrl(entity.getActionUrl());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }

    @Override
    protected void updateEntity(Notification entity, NotificationRequest request) {
        entity.setTitle(request.getTitle());
        entity.setMessage(request.getMessage());
        entity.setType(request.getType());
        entity.setActionUrl(request.getActionUrl());
    }


    @Transactional(readOnly = true)
    public List<NotificationResponse> findByUserId(Long userId) {
        return notificationRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findUnreadByUserId(Long userId) {
        return notificationRepository.findUnreadNotificationsByUser(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findRecentByUserId(Long userId) {
        return notificationRepository.findRecentNotificationsByUser(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countUnreadNotifications(userId);
    }

    @Transactional
    public NotificationResponse markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        notification.setRead(true);
        return toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findUnreadNotificationsByUser(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    // Metodă utilitară folosită de alte service-uri (Course, Enrollment etc.)
    @Transactional
    public void sendNotification(Long userId, String title, String message, NotificationType type) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setTitle(title);
        request.setMessage(message);
        request.setType(type);
        create(request);
    }
}