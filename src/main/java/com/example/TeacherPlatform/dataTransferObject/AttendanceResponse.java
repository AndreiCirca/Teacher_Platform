package com.example.TeacherPlatform.dataTransferObject;

import com.example.TeacherPlatform.model.enums.AttendanceStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AttendanceResponse {
    private Long id;
    private Long sessionId;
    private String sessionTopic;
    private Long enrollmentId;
    private String teacherFullName;
    private AttendanceStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}