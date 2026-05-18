package com.example.TeacherPlatform.dataTransferObject;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CourseMaterialResponse {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String fileUrl;
    private String description;
    private Integer downloadCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}