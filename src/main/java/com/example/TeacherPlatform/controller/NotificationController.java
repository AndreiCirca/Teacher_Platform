package com.example.TeacherPlatform.controller;

import com.example.TeacherPlatform.controller.generic.GenericController;
import com.example.TeacherPlatform.dataTransferObject.NotificationRequest;
import com.example.TeacherPlatform.dataTransferObject.NotificationResponse;
import com.example.TeacherPlatform.model.Notification;
import com.example.TeacherPlatform.model.User;
import com.example.TeacherPlatform.repository.UserRepository;
import com.example.TeacherPlatform.service.NotificationService;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController extends GenericController<Notification, NotificationRequest, NotificationResponse> {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Override
    protected GenericService<Notification, NotificationRequest, NotificationResponse> getService() {
        return notificationService;
    }

    // GET /api/notifications/my — notificările utilizatorului curent
    @GetMapping("/my")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(notificationService.findByUserId(userId));
    }

    // GET /api/notifications/my/unread
    @GetMapping("/my/unread")
    public ResponseEntity<List<NotificationResponse>> getMyUnread(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(notificationService.findUnreadByUserId(userId));
    }

    // GET /api/notifications/my/recent — ultimele 10
    @GetMapping("/my/recent")
    public ResponseEntity<List<NotificationResponse>> getMyRecent(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(notificationService.findRecentByUserId(userId));
    }

    // GET /api/notifications/my/unread-count
    @GetMapping("/my/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(Map.of("count", notificationService.countUnread(userId)));
    }

    // PUT /api/notifications/{id}/read
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    // PUT /api/notifications/my/read-all
    @PutMapping("/my/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }

    // Metodele de creare/editare/ștergere sunt rezervate ADMIN
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationResponse> create(
            @RequestBody NotificationRequest request) {
        return super.create(request);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationResponse> update(
            @PathVariable Long id, @RequestBody NotificationRequest request) {
        return super.update(id, request);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return super.delete(id);
    }


    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }
}