package com.example.TeacherPlatform.model;

import com.example.TeacherPlatform.model.enums.CourseStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Course extends BaseEntity {
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CourseCategory category;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private User trainer;
    
    @Column(nullable = false)
    private LocalDate startDate;
    
    @Column(nullable = false)
    private LocalDate endDate;
    
    @Column(nullable = false)
    private Integer creditHours;
    
    @Column(nullable = false)
    private Integer maxParticipants = 25;
    
    @Column(nullable = false)
    private Integer currentEnrolled = 0;
    
    @Column(nullable = false)
    private Integer sessionCount = 1;
    
    @Column(nullable = false)
    private Boolean isOnline = true;
    
    private String location;
    
    private String meetingLink;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatus status = CourseStatus.DRAFT;
    
    @Column(nullable = false)
    private String thumbnailUrl = "";
}

