package com.example.TeacherPlatform.dataTransferObject;

import com.example.TeacherPlatform.model.enums.EnrollmentStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EnrollmentResponse {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private Long teacherId;
    private String teacherFullName;
    private EnrollmentStatus status;
    private Boolean certificateGenerated;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}