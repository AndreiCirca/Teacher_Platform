package com.example.TeacherPlatform.dataTransferObject;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SchoolRequest {

    @NotBlank(message = "School name is required")
    private String name;

    @NotBlank(message = "County is required")
    private String county;

    private String taxId;

    private String address;

    @Email(message = "Invalid email address")
    private String directorEmail;

    @Min(value = 0, message = "Teacher count cannot be negative")
    private Integer teacherCount = 0;
}