package com.example.TeacherPlatform.dataTransferObject;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SchoolResponse {
    private Long id;
    private String name;
    private String county;
    private String taxId;
    private String address;
    private String directorEmail;
    private Integer teacherCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}