package com.example.TeacherPlatform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "schools")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class School extends BaseEntity {
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String county;
    
    @Column(unique = true)
    private String cui;
    
    private String address;
    
    private String directorEmail;
    
    @Column(nullable = false)
    private Integer teacherCount = 0;
}

