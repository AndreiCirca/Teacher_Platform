package com.example.TeacherPlatform.dataTransferObject;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseSessionResponse {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String topic;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String meetingLink;
    private Integer sessionNumber;
    private Boolean attendanceMarked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}