package com.example.TeacherPlatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_sessions")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CourseSession extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @Column(nullable = false)
    private String topic;
    
    @Column(nullable = false)
    private LocalDateTime startTime;
    
    @Column(nullable = false)
    private LocalDateTime endTime;
    
    private String meetingLink;
    
    @Column(nullable = false)
    private Integer sessionNumber;
    
    @Column(nullable = false)
    private Boolean attendanceMarked = false;
}

