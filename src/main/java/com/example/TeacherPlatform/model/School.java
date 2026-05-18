package com.example.TeacherPlatform.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "schools")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class School extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String county;

    @Column(name = "tax_id", unique = true)
    private String taxId;

    private String address;

    private String directorEmail;

    @Column(nullable = false)
    private Integer teacherCount = 0;
}