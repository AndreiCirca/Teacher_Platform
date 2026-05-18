package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.NotificationRequest;
import com.example.TeacherPlatform.dataTransferObject.NotificationResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.Notification;
import com.example.TeacherPlatform.model.User;
import com.example.TeacherPlatform.repository.BaseRepository;
import com.example.TeacherPlatform.repository.NotificationRepository;
import com.example.TeacherPlatform.repository.UserRepository;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        Notification notification = new Notification();
        mapFields(notification, request);
        return notification;
    }

    @Override
    protected NotificationResponse toResponse(Notification entity) {
        NotificationResponse response = new NotificationResponse();
        response.setId(entity.getId());

        if (entity.getUser() != null) {
            response.setUserId(entity.getUser().getId());
            response.setUserEmail(entity.getUser().getEmail());
        }

        response.setTitle(entity.getTitle());
        response.setMessage(entity.getMessage());
        response.setType(entity.getType());
        response.setRead(entity.getRead());
        response.setActionUrl(entity.getActionUrl());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    @Override
    protected void updateEntity(Notification entity, NotificationRequest request) {
        mapFields(entity, request);
    }

    private void mapFields(Notification entity, NotificationRequest request) {
        entity.setTitle(request.getTitle());
        entity.setMessage(request.getMessage());
        entity.setType(request.getType());
        entity.setRead(request.getRead());
        entity.setActionUrl(request.getActionUrl());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));
        entity.setUser(user);
    }
}