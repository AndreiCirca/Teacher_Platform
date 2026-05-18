package com.example.TeacherPlatform.dataTransferObject;

import com.example.TeacherPlatform.model.enums.EnrollmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EnrollmentRequest {
    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Teacher ID is required")
    private Long teacherId;

    private EnrollmentStatus status = EnrollmentStatus.PENDING;

    private Boolean certificateGenerated = false;
}