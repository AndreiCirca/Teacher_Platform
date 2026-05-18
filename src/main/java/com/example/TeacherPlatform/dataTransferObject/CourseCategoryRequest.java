package com.example.TeacherPlatform.dataTransferObject;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CourseCategoryRequest {
    @NotBlank(message = "Category name is required")
    private String name;

    private String description;

    @NotBlank(message = "Color hex code is required")
    private String color;

    private Boolean active = true;
}