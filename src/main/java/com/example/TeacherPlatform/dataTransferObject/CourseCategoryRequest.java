package com.example.TeacherPlatform.dataTransferObject;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CourseCategoryRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    private String description;

    @NotBlank(message = "Color is required")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Color must be a valid hex code (e.g. #0F6E56)")
    private String color;

    private Boolean active = true;
}