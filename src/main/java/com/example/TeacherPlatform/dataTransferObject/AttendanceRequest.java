package com.example.TeacherPlatform.dataTransferObject;

import com.example.TeacherPlatform.model.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AttendanceRequest {

    @NotNull(message = "Session ID is required")
    private Long sessionId;

    @NotNull(message = "Enrollment ID is required")
    private Long enrollmentId;

    @NotNull(message = "Attendance status is required")
    private AttendanceStatus status;
}