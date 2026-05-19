package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.CertificateRequest;
import com.example.TeacherPlatform.dataTransferObject.CertificateResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.Certificate;
import com.example.TeacherPlatform.model.Enrollment;
import com.example.TeacherPlatform.model.enums.CertificateStatus;
import com.example.TeacherPlatform.repository.BaseRepository;
import com.example.TeacherPlatform.repository.CertificateRepository;
import com.example.TeacherPlatform.repository.EnrollmentRepository;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    @Transactional
    public CertificateResponse create(CertificateRequest request) {
        if (certificateRepository.findByCertificateCode(request.getCertificateCode()).isPresent()) {
            throw new RuntimeException("A certificate with this code already exists");
        }
        return super.create(request);
    }

    @Override
    protected Certificate toEntity(CertificateRequest request) {
        Certificate certificate = new Certificate();

        Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + request.getEnrollmentId()));
        certificate.setEnrollment(enrollment);

        certificate.setCertificateCode(request.getCertificateCode());
        certificate.setIssuedDate(request.getIssuedDate());
        certificate.setStatus(request.getStatus() != null ? request.getStatus() : CertificateStatus.ACTIVE);
        certificate.setCertificateUrl(request.getCertificateUrl());
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
        entity.setCertificateCode(request.getCertificateCode());
        entity.setIssuedDate(request.getIssuedDate());
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        entity.setCertificateUrl(request.getCertificateUrl());
    }

    @Transactional(readOnly = true)
    public List<CertificateResponse> findMyCertificates(Long teacherId) {
        return certificateRepository.findCertificatesByTeacher(teacherId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CertificateResponse verifyCertificate(String code) {
        Certificate certificate = certificateRepository.findByCertificateCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with code: " + code));
        return toResponse(certificate);
    }

    @Transactional
    public CertificateResponse revokeCertificate(Long id) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with id: " + id));
        certificate.setStatus(CertificateStatus.REVOKED); // Sau starea corespunzătoare din enum-ul tău (ex: INACTIVE)
        return toResponse(certificateRepository.save(certificate));
    }
}