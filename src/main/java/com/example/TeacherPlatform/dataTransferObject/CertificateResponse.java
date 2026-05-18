package com.example.TeacherPlatform.dataTransferObject;

import com.example.TeacherPlatform.model.enums.CertificateStatus;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CertificateResponse {
    private Long id;
    private Long enrollmentId;
    private String teacherFullName;
    private String courseTitle;
    private String certificateCode;
    private LocalDate issuedDate;
    private CertificateStatus status;
    private String certificateUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}