package com.example.TeacherPlatform.model;

import com.example.TeacherPlatform.model.enums.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "enrollments", uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "teacher_id"}))
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Enrollment extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status = EnrollmentStatus.PENDING;
    
    @Column(nullable = false)
    private Boolean certificateGenerated = false;
}

