package com.example.TeacherPlatform.controller;

import com.example.TeacherPlatform.controller.generic.GenericController;
import com.example.TeacherPlatform.dataTransferObject.NotificationRequest;
import com.example.TeacherPlatform.dataTransferObject.NotificationResponse;
import com.example.TeacherPlatform.model.Notification;
import com.example.TeacherPlatform.service.NotificationService;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController extends GenericController<Notification, NotificationRequest, NotificationResponse> {

    private final NotificationService notificationService;

    @Override
    protected GenericService<Notification, NotificationRequest, NotificationResponse> getService() {
        return notificationService;
    }
}