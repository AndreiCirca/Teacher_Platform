package com.example.TeacherPlatform.dataTransferObject;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CourseCategoryResponse {
    private Long id;
    private String name;
    private String description;
    private String color;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}