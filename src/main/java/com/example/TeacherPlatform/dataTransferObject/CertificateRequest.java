package com.example.TeacherPlatform.dataTransferObject;

import com.example.TeacherPlatform.model.enums.CertificateStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CertificateRequest {
    @NotNull(message = "Enrollment ID is required")
    private Long enrollmentId;

    @NotBlank(message = "Certificate code is required")
    private String certificateCode;

    @NotNull(message = "Issued date is required")
    private LocalDate issuedDate;

    private CertificateStatus status = CertificateStatus.ACTIVE;

    private String certificateUrl;
}