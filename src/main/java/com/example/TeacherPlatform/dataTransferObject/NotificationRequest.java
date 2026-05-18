package com.example.TeacherPlatform.dataTransferObject;

import com.example.TeacherPlatform.model.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    private NotificationType type = NotificationType.INFO;

    private Boolean read = false;

    private String actionUrl;
}