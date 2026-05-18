package com.example.TeacherPlatform.dataTransferObject;

import com.example.TeacherPlatform.model.enums.CourseStatus;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CourseRequest {
    @NotBlank(message = "Course title is required")
    private String title;

    private String description;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Trainer ID is required")
    private Long trainerId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Credit hours are required")
    @Min(value = 1, message = "Credit hours must be at least 1")
    private Integer creditHours;

    @NotNull(message = "Max participants count is required")
    @Min(value = 1, message = "Max participants must be at least 1")
    private Integer maxParticipants;

    private Boolean isOnline = true;
    private String location;
    private String meetingLink;
    private CourseStatus status = CourseStatus.DRAFT;
    private String thumbnailUrl = "";
}