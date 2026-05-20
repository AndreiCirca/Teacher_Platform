package com.example.TeacherPlatform.dataTransferObject;

import com.example.TeacherPlatform.model.enums.CourseStatus;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CourseResponse {
    private Long id;
    private String title;
    private String description;
    private Long trainerId;
    private String trainerFirstName;
    private String trainerLastName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer creditHours;
    private Integer maxParticipants;
    private Integer currentEnrolled;
    private Integer sessionCount;
    private Boolean isOnline;
    private String location;
    private String meetingLink;
    private CourseStatus status;
    private String thumbnailUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}