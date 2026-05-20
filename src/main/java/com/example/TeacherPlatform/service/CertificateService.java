package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.CertificateRequest;
import com.example.TeacherPlatform.dataTransferObject.CertificateResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.Certificate;
import com.example.TeacherPlatform.model.Course;
import com.example.TeacherPlatform.model.Enrollment;
import com.example.TeacherPlatform.model.enums.AttendanceStatus;
import com.example.TeacherPlatform.model.enums.CertificateStatus;
import com.example.TeacherPlatform.model.enums.EnrollmentStatus;
import com.example.TeacherPlatform.repository.*;
import com.example.TeacherPlatform.service.generic.GenericService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CertificateService extends GenericService<Certificate, CertificateRequest, CertificateResponse> {

    private final CertificateRepository certificateRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final CourseSessionRepository courseSessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final int minAttendancePercentage;

    public CertificateService(
            CertificateRepository certificateRepository,
            EnrollmentRepository enrollmentRepository,
            CourseRepository courseRepository,
            CourseSessionRepository courseSessionRepository,
            AttendanceRepository attendanceRepository,
            @Value("${app.certificates.min-attendance-percentage:75}") int minAttendancePercentage) {
        this.certificateRepository = certificateRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.courseSessionRepository = courseSessionRepository;
        this.attendanceRepository = attendanceRepository;
        this.minAttendancePercentage = minAttendancePercentage;
    }

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
                response.setTeacherFullName(entity.getEnrollment().getTeacher().getFirstName()
                        + " " + entity.getEnrollment().getTeacher().getLastName());
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
                .stream().map(this::toResponse).toList();
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
        certificate.setStatus(CertificateStatus.REVOKED);
        return toResponse(certificateRepository.save(certificate));
    }

    @Transactional
    public List<CertificateResponse> generateBulkCertificatesForCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        long totalSessions = course.getSessionCount() != null ? course.getSessionCount() : 0;
        if (totalSessions == 0) {
            throw new IllegalStateException("Cannot generate certificates for a course with 0 sessions.");
        }

        List<Enrollment> confirmedEnrollments = enrollmentRepository.findConfirmedEnrollmentsByCourse(courseId);
        List<Certificate> generatedCertificates = new ArrayList<>();

        for (Enrollment enrollment : confirmedEnrollments) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
            enrollmentRepository.save(enrollment);

            long presentSessions = attendanceRepository.countPresentSessionsByEnrollment(
                    enrollment.getId(), AttendanceStatus.PRESENT);
            double attendancePercentage = ((double) presentSessions / totalSessions) * 100;

            if (attendancePercentage >= minAttendancePercentage) {
                boolean alreadyHas = certificateRepository.findCertificatesByTeacher(enrollment.getTeacher().getId())
                        .stream().anyMatch(c -> c.getEnrollment().getId().equals(enrollment.getId()));
                if (!alreadyHas) {
                    Certificate certificate = new Certificate();
                    certificate.setEnrollment(enrollment);
                    String uniqueSuffix = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
                    String generatedCode = "FORM-" + LocalDate.now().getYear() + "-" + uniqueSuffix;
                    certificate.setCertificateCode(generatedCode);
                    certificate.setIssuedDate(LocalDate.now());
                    certificate.setStatus(CertificateStatus.ACTIVE);
                    certificate.setCertificateUrl("certificates/" + generatedCode + ".pdf");
                    generatedCertificates.add(certificateRepository.save(certificate));
                }
            }
        }

        return generatedCertificates.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CertificateResponse downloadCertificate(Long id, String email) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with id: " + id));
        if (!certificate.getEnrollment().getTeacher().getEmail().equals(email)) {
            throw new RuntimeException("Access Denied. You can only download your own certificates.");
        }
        return toResponse(certificate);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getCertificateStats() {
        return Map.of(
                "total", certificateRepository.count(),
                "active", certificateRepository.countByStatus(CertificateStatus.ACTIVE),
                "revoked", certificateRepository.countByStatus(CertificateStatus.REVOKED),
                "pending", certificateRepository.countByStatus(CertificateStatus.PENDING)
        );
    }
}