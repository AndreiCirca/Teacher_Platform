package com.example.TeacherPlatform.dataTransferObject;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseSessionRequest {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotBlank(message = "Topic is required")
    private String topic;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    private String meetingLink;

    @NotNull(message = "Session number is required")
    @Positive(message = "Session number must be positive")
    private Integer sessionNumber;
}