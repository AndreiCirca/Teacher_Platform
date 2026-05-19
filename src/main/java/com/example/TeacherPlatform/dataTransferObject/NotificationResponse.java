package com.example.TeacherPlatform.dataTransferObject;

import com.example.TeacherPlatform.model.enums.NotificationType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Long id;
    private Long userId;
    private String title;
    private String message;
    private NotificationType type;
    private Boolean read;
    private String actionUrl;
    private LocalDateTime createdAt;
}