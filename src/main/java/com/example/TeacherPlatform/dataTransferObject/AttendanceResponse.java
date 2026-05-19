package com.example.TeacherPlatform.dataTransferObject;

import com.example.TeacherPlatform.model.enums.AttendanceStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AttendanceResponse {
    private Long id;
    private Long sessionId;
    private Integer sessionNumber;   // Important pentru ordonare
    private String sessionTopic;
    private Long enrollmentId;
    private Long teacherId;          // Important pentru frontend
    private String teacherFirstName; // Am spart numele în două ca să fie mai ușor de randat
    private String teacherLastName;
    private AttendanceStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}