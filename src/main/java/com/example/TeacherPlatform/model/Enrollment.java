// com/example/TeacherPlatform/model/Enrollment.java
package com.example.TeacherPlatform.model;

import com.example.TeacherPlatform.model.enums.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "enrollments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"teacher_id", "course_id"}))
@Getter @Setter @NoArgsConstructor
public class Enrollment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status = EnrollmentStatus.PENDING;

    @Column(nullable = false)
    private Boolean certificateGenerated = false;
}