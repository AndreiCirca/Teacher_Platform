package com.example.TeacherPlatform.service.crudtests;

import com.example.TeacherPlatform.dataTransferObject.CertificateRequest;
import com.example.TeacherPlatform.dataTransferObject.CertificateResponse;
import com.example.TeacherPlatform.model.Certificate;
import com.example.TeacherPlatform.model.Course;
import com.example.TeacherPlatform.model.Enrollment;
import com.example.TeacherPlatform.model.User;
import com.example.TeacherPlatform.model.enums.CertificateStatus;
import com.example.TeacherPlatform.repository.*;
import com.example.TeacherPlatform.service.CertificateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CertificateService - Business Logic Tests")
class CertificateServiceCrudTest {

    @Mock private CertificateRepository certificateRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private CourseSessionRepository courseSessionRepository;
    @Mock private AttendanceRepository attendanceRepository;

    private CertificateService certificateService;

    private User teacher;
    private Course course;
    private Enrollment enrollment;
    private Certificate certificate;
    private CertificateRequest request;

    @BeforeEach
    void setUp() {
        // Injectăm manual serviciul pentru a putea seta "minAttendancePercentage" = 75
        certificateService = new CertificateService(
                certificateRepository, enrollmentRepository, courseRepository,
                courseSessionRepository, attendanceRepository, 75
        );

        teacher = new User();
        teacher.setId(1L);
        teacher.setEmail("profesor@edu.ro");

        course = new Course();
        course.setId(1L);
        course.setSessionCount(4); // Cursul are 4 sesiuni în total

        enrollment = new Enrollment();
        enrollment.setId(1L);
        enrollment.setCourse(course);
        enrollment.setTeacher(teacher);

        certificate = new Certificate();
        certificate.setId(1L);
        certificate.setCertificateCode("FORM-2026-TEST");
        certificate.setEnrollment(enrollment);

        request = new CertificateRequest();
        request.setCertificateCode("FORM-2026-TEST");
    }

    @Test
    @DisplayName("Create - Aruncă excepție dacă codul certificatului există deja")
    void create_throwsException_whenCodeExists() {
        when(certificateRepository.findByCertificateCode("FORM-2026-TEST")).thenReturn(Optional.of(certificate));

        assertThatThrownBy(() -> certificateService.create(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Generate Bulk - Emite certificat dacă prezența este >= 75%")
    void generateBulk_success_whenAttendanceIsSufficient() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findConfirmedEnrollmentsByCourse(1L)).thenReturn(List.of(enrollment));

        // AICI ESTE CORECtIA: Doar 1 parametru (1L) pentru enrollmentId
        when(attendanceRepository.countPresentSessionsByEnrollment(1L)).thenReturn(3L);
        when(certificateRepository.findCertificatesByTeacher(1L)).thenReturn(List.of());

        when(certificateRepository.save(any(Certificate.class))).thenAnswer(i -> {
            Certificate c = i.getArgument(0);
            c.setId(10L);
            return c;
        });

        List<CertificateResponse> generated = certificateService.generateBulkCertificatesForCourse(1L);

        assertThat(generated).hasSize(1);
        verify(certificateRepository).save(any(Certificate.class));
    }

    @Test
    @DisplayName("Generate Bulk - NU emite certificat dacă prezența e sub 75%")
    void generateBulk_fails_whenAttendanceIsInsufficient() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findConfirmedEnrollmentsByCourse(1L)).thenReturn(List.of(enrollment));

        // AICI ESTE CORECȚIA: Doar 1 parametru (1L) pentru enrollmentId
        when(attendanceRepository.countPresentSessionsByEnrollment(1L)).thenReturn(2L);

        List<CertificateResponse> generated = certificateService.generateBulkCertificatesForCourse(1L);

        assertThat(generated).isEmpty();
        verify(certificateRepository, never()).save(any(Certificate.class));
    }

    @Test
    @DisplayName("Download Certificate - Succes dacă profesorul cere propriul certificat")
    void downloadCertificate_success_forOwner() {
        when(certificateRepository.findById(1L)).thenReturn(Optional.of(certificate));

        CertificateResponse response = certificateService.downloadCertificate(1L, "profesor@edu.ro");

        assertThat(response.getCertificateCode()).isEqualTo("FORM-2026-TEST");
    }

    @Test
    @DisplayName("Download Certificate - Aruncă excepție dacă profesorul cere certificatul altcuiva")
    void downloadCertificate_throwsException_forOtherUser() {
        when(certificateRepository.findById(1L)).thenReturn(Optional.of(certificate));

        assertThatThrownBy(() -> certificateService.downloadCertificate(1L, "alt_profesor@edu.ro"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access Denied");
    }

    @Test
    @DisplayName("Revoke Certificate - Schimbă statusul în REVOKED")
    void revokeCertificate_changesStatusToRevoked() {
        when(certificateRepository.findById(1L)).thenReturn(Optional.of(certificate));
        when(certificateRepository.save(any(Certificate.class))).thenReturn(certificate);

        CertificateResponse response = certificateService.revokeCertificate(1L);

        assertThat(response.getStatus()).isEqualTo(CertificateStatus.REVOKED);
        verify(certificateRepository).save(certificate);
    }
}