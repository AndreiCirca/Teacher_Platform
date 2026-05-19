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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    // -------------------------------------------------------------------------
    // Mapări NATIVE & SUPRASCRIERI CRUD (Corelate cu GenericService)
    // -------------------------------------------------------------------------

    // REZOLVARE EROARE: Redenumit corect în findById (nu getById) pentru a corespunde clasei părinte.
    // CORECȚIE ANTI-IDOR: Utilizatorii pot accesa doar propriile notificări, excepție făcând rolul de ADMIN.
    @Override
    @Transactional(readOnly = true)
    public NotificationResponse findById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            User currentUser = getUserByEmail(auth.getName());
            boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"));

            if (!isAdmin && !notification.getUser().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Access denied. This notification does not belong to you.");
            }
        }
        return toResponse(notification);
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
        if (entity.getUser() != null) {
            response.setUserId(entity.getUser().getId());
        }
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

    // -------------------------------------------------------------------------
    // LOGICĂ DE BUSINESS: UTILIZATOR AUTENTIFICAT
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<NotificationResponse> findMyNotifications(Authentication authentication) {
        User user = getUserByEmail(authentication.getName());
        return notificationRepository.findByUserIdOrdered(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findUnreadMyNotifications(Authentication authentication) {
        User user = getUserByEmail(authentication.getName());
        return notificationRepository.findUnreadNotificationsByUser(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findRecentMyNotifications(Authentication authentication) {
        User user = getUserByEmail(authentication.getName());
        // Se limitează automat la nivel de query din repository la maximum 10 înregistrări conform specificațiilor
        return notificationRepository.findRecentNotificationsByUser(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countUnread(Authentication authentication) {
        User user = getUserByEmail(authentication.getName());
        return notificationRepository.countUnreadNotifications(user.getId());
    }

    @Transactional
    public NotificationResponse markAsRead(Long id, Authentication authentication) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        User user = getUserByEmail(authentication.getName());

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied. You cannot modify another user's notifications.");
        }

        notification.setRead(true);
        return toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllAsRead(Authentication authentication) {
        User user = getUserByEmail(authentication.getName());
        List<Notification> unread = notificationRepository.findUnreadNotificationsByUser(user.getId());
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    // -------------------------------------------------------------------------
    // LOGICĂ INTERNĂ SISTEM / ADMIN
    // -------------------------------------------------------------------------

    /**
     * Metodă utilitară sigură invocată automat de alte module din aplicație la evenimente cheie
     * (Aprobare/respingere curs, confirmare înscriere, încărcare materiale etc.)
     */
    @Transactional
    public void sendNotification(Long userId, String title, String message, NotificationType type, String actionUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot send notification. Target user not found with id: " + userId));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);
        notification.setActionUrl(actionUrl);

        notificationRepository.save(notification);
    }

    // -------------------------------------------------------------------------
    // Private Helper
    // -------------------------------------------------------------------------

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
}