package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.CertificateRequest;
import com.example.TeacherPlatform.dataTransferObject.CertificateResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.Certificate;
import com.example.TeacherPlatform.model.Enrollment;
import com.example.TeacherPlatform.repository.BaseRepository;
import com.example.TeacherPlatform.repository.CertificateRepository;
import com.example.TeacherPlatform.repository.EnrollmentRepository;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CertificateService extends GenericService<Certificate, CertificateRequest, CertificateResponse> {

    private final CertificateRepository certificateRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    protected BaseRepository<Certificate> getRepository() {
        return certificateRepository;
    }

    @Override
    protected Certificate toEntity(CertificateRequest request) {
        Certificate certificate = new Certificate();
        mapFields(certificate, request);
        return certificate;
    }

    @Override
    protected CertificateResponse toResponse(Certificate entity) {
        CertificateResponse response = new CertificateResponse();
        response.setId(entity.getId());

        if (entity.getEnrollment() != null) {
            response.setEnrollmentId(entity.getEnrollment().getId());
            if (entity.getEnrollment().getTeacher() != null) {
                response.setTeacherFullName(entity.getEnrollment().getTeacher().getFullName());
            }
            if (entity.getEnrollment().getCourse() != null) {
                response.setCourseTitle(entity.getEnrollment().getCourse().getTitle());
            }
        }

        response.setCertificateCode(entity.getCertificateCode());
        response.setIssuedDate(entity.getIssuedDate());
        response.setStatus(entity.getStatus());
        response.setCertificateUrl(entity.getCertificateUrl());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    @Override
    protected void updateEntity(Certificate entity, CertificateRequest request) {
        mapFields(entity, request);
    }

    private void mapFields(Certificate entity, CertificateRequest request) {
        entity.setCertificateCode(request.getCertificateCode());
        entity.setIssuedDate(request.getIssuedDate());
        entity.setStatus(request.getStatus());
        entity.setCertificateUrl(request.getCertificateUrl());

        Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + request.getEnrollmentId()));
        entity.setEnrollment(enrollment);
    }
}