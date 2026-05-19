package com.example.TeacherPlatform.controller;

import com.example.TeacherPlatform.controller.generic.GenericController;
import com.example.TeacherPlatform.dataTransferObject.NotificationRequest;
import com.example.TeacherPlatform.dataTransferObject.NotificationResponse;
import com.example.TeacherPlatform.model.Notification;
import com.example.TeacherPlatform.service.NotificationService;
import com.example.TeacherPlatform.service.generic.GenericService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController extends GenericController<Notification, NotificationRequest, NotificationResponse> {

    private final NotificationService notificationService;

    @Override
    protected GenericService<Notification, NotificationRequest, NotificationResponse> getService() {
        return notificationService;
    }

    // GET /api/notifications/my — Notificările utilizatorului curent (sortate nou -> vechi)
    @GetMapping("/my")
    @PreAuthorize("hasAnyAuthority('PROFESOR', 'FORMATOR', 'ADMIN')")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(Authentication authentication) {
        return ResponseEntity.ok(notificationService.findMyNotifications(authentication));
    }

    // GET /api/notifications/my/unread
    @GetMapping("/my/unread")
    @PreAuthorize("hasAnyAuthority('PROFESOR', 'FORMATOR', 'ADMIN')")
    public ResponseEntity<List<NotificationResponse>> getMyUnread(Authentication authentication) {
        return ResponseEntity.ok(notificationService.findUnreadMyNotifications(authentication));
    }

    // GET /api/notifications/my/recent — Ultimele 10 notificări primite
    @GetMapping("/my/recent")
    @PreAuthorize("hasAnyAuthority('PROFESOR', 'FORMATOR', 'ADMIN')")
    public ResponseEntity<List<NotificationResponse>> getMyRecent(Authentication authentication) {
        return ResponseEntity.ok(notificationService.findRecentMyNotifications(authentication));
    }

    // GET /api/notifications/my/unread-count
    @GetMapping("/my/unread-count")
    @PreAuthorize("hasAnyAuthority('PROFESOR', 'FORMATOR', 'ADMIN')")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        return ResponseEntity.ok(Map.of("count", notificationService.countUnread(authentication)));
    }

    // PUT /api/notifications/{id}/read — Marcare securizată per notificare
    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyAuthority('PROFESOR', 'FORMATOR', 'ADMIN')")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(notificationService.markAsRead(id, authentication));
    }

    // PUT /api/notifications/my/read-all
    @PutMapping("/my/read-all")
    @PreAuthorize("hasAnyAuthority('PROFESOR', 'FORMATOR', 'ADMIN')")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        notificationService.markAllAsRead(authentication);
        return ResponseEntity.noContent().build();
    }

    // Metodele CRUD administrative sunt blocate explicit doar pentru rolul suprem de ADMIN
    @Override
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<NotificationResponse> create(@Valid @RequestBody NotificationRequest request) {
        return super.create(request);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<NotificationResponse> update(@PathVariable Long id, @Valid @RequestBody NotificationRequest request) {
        return super.update(id, request);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return super.delete(id);
    }
}