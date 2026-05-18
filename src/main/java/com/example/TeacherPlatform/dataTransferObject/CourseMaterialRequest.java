package com.example.TeacherPlatform.dataTransferObject;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseMaterialRequest {
    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotBlank(message = "File name is required")
    private String fileName;

    @NotBlank(message = "File type is required")
    private String fileType;

    @NotNull(message = "File size is required")
    private Long fileSize;

    @NotBlank(message = "File URL is required")
    private String fileUrl;

    private String description;

    private Integer downloadCount = 0;
}