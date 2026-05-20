package com.example.TeacherPlatform.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_materials")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CourseMaterial extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileType;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private String fileUrl;

    private String description;

    @Column(nullable = false)
    private Integer downloadCount = 0;
}