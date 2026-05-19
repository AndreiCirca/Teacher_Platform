package com.example.TeacherPlatform.service.functinalitiestests;

import com.example.TeacherPlatform.dataTransferObject.CertificateResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.*;
import com.example.TeacherPlatform.model.enums.*;
import com.example.TeacherPlatform.repository.*;
import com.example.TeacherPlatform.service.CertificateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CertificateService - Functionality Tests (non-CRUD)")
class CertificateServiceTest {

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseSessionRepository courseSessionRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    // CertificateService does NOT use @RequiredArgsConstructor — it has a manual constructor
    // with @Value injection for minAttendancePercentage, so we build it manually.
    private CertificateService certificateService;

    private School school;
    private User teacherFilip;
    private User teacherVictor;
    private Course course;
    private Enrollment enrollmentFilip;
    private Enrollment enrollmentVictor;
    private Certificate certFilip;

    @BeforeEach
    void setUp() {
        certificateService = new CertificateService(
                certificateRepository,
                enrollmentRepository,
                courseRepository,
                courseSessionRepository,
                attendanceRepository,
                75 // minAttendancePercentage
        );

        school = buildSchool(1L, "Colegiul Național Silvania", "Sălaj");

        teacherFilip  = buildUser(10L, "Filip",  "Mureșan", "filip@edu.ro",  UserRole.PROFESOR, school);
        teacherVictor = buildUser(11L, "Victor", "Dan",     "victor@edu.ro", UserRole.PROFESOR, school);

        CourseCategory category = new CourseCategory();
        category.setId(1L);
        category.setName("Robotică");

        course = buildCourse(100L, "Navigație Autonomă cu ROS 2", teacherFilip, category, 4);

        enrollmentFilip  = buildEnrollment(200L, course, teacherFilip,  EnrollmentStatus.CONFIRMED);
        enrollmentVictor = buildEnrollment(201L, course, teacherVictor, EnrollmentStatus.CONFIRMED);

        certFilip = buildCertificate(300L, enrollmentFilip, "FORM-2024-AAAAA",
                LocalDate.now().minusDays(5), CertificateStatus.ACTIVE, "certificates/FORM-2024-AAAAA.pdf");
    }

    // -------------------------------------------------------------------------
    // findMyCertificates
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findMyCertificates - returns certificates belonging to the given teacher")
    void findMyCertificates_returnsTeacherCertificates() {
        when(certificateRepository.findCertificatesByTeacher(10L)).thenReturn(List.of(certFilip));

        List<CertificateResponse> result = certificateService.findMyCertificates(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCertificateCode()).isEqualTo("FORM-2024-AAAAA");
        assertThat(result.get(0).getTeacherFullName()).isEqualTo("Filip Mureșan");
        assertThat(result.get(0).getCourseTitle()).isEqualTo("Navigație Autonomă cu ROS 2");
        verify(certificateRepository).findCertificatesByTeacher(10L);
    }

    @Test
    @DisplayName("findMyCertificates - returns empty list when teacher has no certificates")
    void findMyCertificates_returnsEmpty_whenNoCertificates() {
        when(certificateRepository.findCertificatesByTeacher(11L)).thenReturn(List.of());

        List<CertificateResponse> result = certificateService.findMyCertificates(11L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findMyCertificates - maps all response fields correctly")
    void findMyCertificates_mapsResponseFields() {
        when(certificateRepository.findCertificatesByTeacher(10L)).thenReturn(List.of(certFilip));

        CertificateResponse response = certificateService.findMyCertificates(10L).get(0);

        assertThat(response.getId()).isEqualTo(300L);
        assertThat(response.getEnrollmentId()).isEqualTo(200L);
        assertThat(response.getStatus()).isEqualTo(CertificateStatus.ACTIVE);
        assertThat(response.getCertificateUrl()).isEqualTo("certificates/FORM-2024-AAAAA.pdf");
        assertThat(response.getIssuedDate()).isEqualTo(certFilip.getIssuedDate());
    }

    // -------------------------------------------------------------------------
    // verifyCertificate
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("verifyCertificate - returns certificate when code is valid")
    void verifyCertificate_returnsResponse_forValidCode() {
        when(certificateRepository.findByCertificateCode("FORM-2024-AAAAA")).thenReturn(Optional.of(certFilip));

        CertificateResponse result = certificateService.verifyCertificate("FORM-2024-AAAAA");

        assertThat(result.getCertificateCode()).isEqualTo("FORM-2024-AAAAA");
        assertThat(result.getStatus()).isEqualTo(CertificateStatus.ACTIVE);
    }

    @Test
    @DisplayName("verifyCertificate - throws ResourceNotFoundException for unknown code")
    void verifyCertificate_throwsException_forUnknownCode() {
        when(certificateRepository.findByCertificateCode("INVALID-CODE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> certificateService.verifyCertificate("INVALID-CODE"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("INVALID-CODE");
    }

    // -------------------------------------------------------------------------
    // revokeCertificate
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("revokeCertificate - changes certificate status to REVOKED")
    void revokeCertificate_changesStatusToRevoked() {
        when(certificateRepository.findById(300L)).thenReturn(Optional.of(certFilip));
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(inv -> inv.getArgument(0));

        CertificateResponse result = certificateService.revokeCertificate(300L);

        assertThat(result.getStatus()).isEqualTo(CertificateStatus.REVOKED);
        verify(certificateRepository).save(certFilip);
    }

    @Test
    @DisplayName("revokeCertificate - throws ResourceNotFoundException when certificate not found")
    void revokeCertificate_throwsException_whenNotFound() {
        when(certificateRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> certificateService.revokeCertificate(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(certificateRepository, never()).save(any());
    }

    @Test
    @DisplayName("revokeCertificate - can revoke an already-active certificate")
    void revokeCertificate_revokesActiveStatus() {
        assertThat(certFilip.getStatus()).isEqualTo(CertificateStatus.ACTIVE);

        when(certificateRepository.findById(300L)).thenReturn(Optional.of(certFilip));
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(inv -> inv.getArgument(0));

        CertificateResponse result = certificateService.revokeCertificate(300L);

        assertThat(result.getStatus()).isEqualTo(CertificateStatus.REVOKED);
    }

    // -------------------------------------------------------------------------
    // generateBulkCertificatesForCourse
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("generateBulkCertificatesForCourse - generates certificate only for enrollment meeting attendance threshold (75%)")
    void generateBulk_generatesCertificateAboveThreshold() {
        // Course has 4 sessions; Filip attended 3 (75%) → eligible; Victor attended 2 (50%) → not eligible
        when(courseRepository.findById(100L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findConfirmedEnrollmentsByCourse(100L))
                .thenReturn(List.of(enrollmentFilip, enrollmentVictor));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));

        when(attendanceRepository.countPresentSessionsByEnrollment(200L)).thenReturn(3L); // Filip: 3/4 = 75% ✓
        when(attendanceRepository.countPresentSessionsByEnrollment(201L)).thenReturn(2L); // Victor: 2/4 = 50% ✗

        // Filip has no previous cert for this course
        when(certificateRepository.findCertificatesByTeacher(10L)).thenReturn(List.of());
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(inv -> inv.getArgument(0));

        List<CertificateResponse> result = certificateService.generateBulkCertificatesForCourse(100L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTeacherFullName()).isEqualTo("Filip Mureșan");
        verify(certificateRepository, times(1)).save(any(Certificate.class));
    }

    @Test
    @DisplayName("generateBulkCertificatesForCourse - skips enrollment if certificate already exists")
    void generateBulk_skipsDuplicateCertificate() {
        when(courseRepository.findById(100L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findConfirmedEnrollmentsByCourse(100L))
                .thenReturn(List.of(enrollmentFilip));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(attendanceRepository.countPresentSessionsByEnrollment(200L)).thenReturn(4L); // 100% ✓

        // Filip already has a certificate for this enrollment
        when(certificateRepository.findCertificatesByTeacher(10L)).thenReturn(List.of(certFilip));

        List<CertificateResponse> result = certificateService.generateBulkCertificatesForCourse(100L);

        assertThat(result).isEmpty();
        verify(certificateRepository, never()).save(any(Certificate.class));
    }

    @Test
    @DisplayName("generateBulkCertificatesForCourse - throws IllegalStateException when course has 0 sessions")
    void generateBulk_throwsException_whenCourseHasZeroSessions() {
        course.setSessionCount(0);
        when(courseRepository.findById(100L)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> certificateService.generateBulkCertificatesForCourse(100L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("0 sessions");
    }

    @Test
    @DisplayName("generateBulkCertificatesForCourse - throws ResourceNotFoundException when course not found")
    void generateBulk_throwsException_whenCourseNotFound() {
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> certificateService.generateBulkCertificatesForCourse(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("generateBulkCertificatesForCourse - marks all confirmed enrollments as COMPLETED")
    void generateBulk_marksEnrollmentsCompleted() {
        when(courseRepository.findById(100L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findConfirmedEnrollmentsByCourse(100L))
                .thenReturn(List.of(enrollmentFilip, enrollmentVictor));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(attendanceRepository.countPresentSessionsByEnrollment(anyLong())).thenReturn(0L); // below threshold → no certs

        certificateService.generateBulkCertificatesForCourse(100L);

        assertThat(enrollmentFilip.getStatus()).isEqualTo(EnrollmentStatus.COMPLETED);
        assertThat(enrollmentVictor.getStatus()).isEqualTo(EnrollmentStatus.COMPLETED);
        verify(enrollmentRepository, times(2)).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("generateBulkCertificatesForCourse - returns empty list when no enrollment meets threshold")
    void generateBulk_returnsEmpty_whenNoneEligible() {
        when(courseRepository.findById(100L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findConfirmedEnrollmentsByCourse(100L))
                .thenReturn(List.of(enrollmentFilip, enrollmentVictor));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(attendanceRepository.countPresentSessionsByEnrollment(200L)).thenReturn(2L); // 50% ✗
        when(attendanceRepository.countPresentSessionsByEnrollment(201L)).thenReturn(1L); // 25% ✗

        List<CertificateResponse> result = certificateService.generateBulkCertificatesForCourse(100L);

        assertThat(result).isEmpty();
        verify(certificateRepository, never()).save(any(Certificate.class));
    }

    // -------------------------------------------------------------------------
    // downloadCertificate
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("downloadCertificate - returns certificate when owner requests it")
    void downloadCertificate_returnsResponse_forOwner() {
        when(certificateRepository.findById(300L)).thenReturn(Optional.of(certFilip));

        CertificateResponse result = certificateService.downloadCertificate(300L, "filip@edu.ro");

        assertThat(result.getCertificateCode()).isEqualTo("FORM-2024-AAAAA");
    }

    @Test
    @DisplayName("downloadCertificate - throws RuntimeException when requester is not the certificate owner")
    void downloadCertificate_throwsException_whenNotOwner() {
        when(certificateRepository.findById(300L)).thenReturn(Optional.of(certFilip));

        // certFilip belongs to filip@edu.ro but victor@edu.ro is requesting
        assertThatThrownBy(() -> certificateService.downloadCertificate(300L, "victor@edu.ro"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access Denied");
    }

    @Test
    @DisplayName("downloadCertificate - throws ResourceNotFoundException when certificate not found")
    void downloadCertificate_throwsException_whenCertificateNotFound() {
        when(certificateRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> certificateService.downloadCertificate(999L, "filip@edu.ro"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // -------------------------------------------------------------------------
    // getCertificateStats
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getCertificateStats - returns map with total, active, revoked and pending counts")
    void getCertificateStats_returnsCorrectStats() {
        when(certificateRepository.count()).thenReturn(10L);
        when(certificateRepository.countByStatus(CertificateStatus.ACTIVE)).thenReturn(7L);
        when(certificateRepository.countByStatus(CertificateStatus.REVOKED)).thenReturn(2L);
        when(certificateRepository.countByStatus(CertificateStatus.PENDING)).thenReturn(1L);

        Map<String, Long> stats = certificateService.getCertificateStats();

        assertThat(stats).containsKeys("total", "active", "revoked", "pending");
        assertThat(stats.get("total")).isEqualTo(10L);
        assertThat(stats.get("active")).isEqualTo(7L);
        assertThat(stats.get("revoked")).isEqualTo(2L);
        assertThat(stats.get("pending")).isEqualTo(1L);
    }

    @Test
    @DisplayName("getCertificateStats - returns all zeros when no certificates exist")
    void getCertificateStats_returnsZeros_whenNoCertificates() {
        when(certificateRepository.count()).thenReturn(0L);
        when(certificateRepository.countByStatus(any(CertificateStatus.class))).thenReturn(0L);

        Map<String, Long> stats = certificateService.getCertificateStats();

        assertThat(stats.get("total")).isEqualTo(0L);
        assertThat(stats.get("active")).isEqualTo(0L);
        assertThat(stats.get("revoked")).isEqualTo(0L);
        assertThat(stats.get("pending")).isEqualTo(0L);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private School buildSchool(Long id, String name, String county) {
        School s = new School();
        s.setId(id);
        s.setName(name);
        s.setCounty(county);
        s.setTeacherCount(0);
        s.setCreatedAt(LocalDateTime.now());
        s.setUpdatedAt(LocalDateTime.now());
        return s;
    }

    private User buildUser(Long id, String firstName, String lastName, String email,
                           UserRole role, School school) {
        User u = new User();
        u.setId(id);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setEmail(email);
        u.setPassword("encoded_pass");
        u.setRole(role);
        u.setSchool(school);
        u.setActive(true);
        u.setEmailVerified(true);
        u.setCreatedAt(LocalDateTime.now());
        u.setUpdatedAt(LocalDateTime.now());
        return u;
    }

    private Course buildCourse(Long id, String title, User trainer, CourseCategory category, int sessionCount) {
        Course c = new Course();
        c.setId(id);
        c.setTitle(title);
        c.setTrainer(trainer);
        c.setCategory(category);
        c.setSessionCount(sessionCount);
        c.setMaxParticipants(20);
        c.setCreditHours(20);
        c.setStatus(CourseStatus.ACTIVE);
        c.setStartDate(LocalDate.now().minusDays(10));
        c.setEndDate(LocalDate.now().plusDays(10));
        c.setCreatedAt(LocalDateTime.now());
        c.setUpdatedAt(LocalDateTime.now());
        return c;
    }

    private Enrollment buildEnrollment(Long id, Course course, User teacher, EnrollmentStatus status) {
        Enrollment e = new Enrollment();
        e.setId(id);
        e.setCourse(course);
        e.setTeacher(teacher);
        e.setStatus(status);
        e.setCertificateGenerated(false);
        e.setCreatedAt(LocalDateTime.now());
        e.setUpdatedAt(LocalDateTime.now());
        return e;
    }

    private Certificate buildCertificate(Long id, Enrollment enrollment, String code,
                                         LocalDate issuedDate, CertificateStatus status, String url) {
        Certificate c = new Certificate();
        c.setId(id);
        c.setEnrollment(enrollment);
        c.setCertificateCode(code);
        c.setIssuedDate(issuedDate);
        c.setStatus(status);
        c.setCertificateUrl(url);
        c.setCreatedAt(LocalDateTime.now());
        c.setUpdatedAt(LocalDateTime.now());
        return c;
    }
}