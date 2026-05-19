package com.example.TeacherPlatform.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course_categories")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CourseCategory extends BaseEntity {
    
    @Column(nullable = false, unique = true)
    private String name;
    
    private String description;
    
    @Column(nullable = false)
    private String color = "#0F6E56";
    
    @Column(nullable = false)
    private Boolean active = true;
}

