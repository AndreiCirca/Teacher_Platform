package com.example.TeacherPlatform.service.functinalitiestests;

import com.example.TeacherPlatform.dataTransferObject.AttendanceRequest;
import com.example.TeacherPlatform.dataTransferObject.AttendanceResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.*;
import com.example.TeacherPlatform.model.enums.AttendanceStatus;
import com.example.TeacherPlatform.model.enums.EnrollmentStatus;
import com.example.TeacherPlatform.repository.*;
import com.example.TeacherPlatform.service.AttendanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceService - Functionality Tests")
class AttendanceServiceTest {

    @Mock private AttendanceRepository attendanceRepository;
    @Mock private CourseSessionRepository courseSessionRepository;
    @Mock private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private Course course;
    private CourseSession session;
    private User teacher;
    private School school;
    private Enrollment enrollment;
    private Attendance attendance;

    @BeforeEach
    void setUp() {
        school = buildSchool(1L, "Colegiul Test", "Cluj");

        teacher = buildUser(1L, "Ion", "Pop", "ion@edu.ro", school);

        course = buildCourse(1L, "Clean Code");

        session = buildSession(1L, course, "SOLID Principles", 1);

        enrollment = buildEnrollment(1L, course, teacher);

        attendance = buildAttendance(1L, session, enrollment, AttendanceStatus.PRESENT);
    }

    // -------------------------------------------------------------------------
    // findBySessionId
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findBySessionId - returneaza prezentele pentru o sesiune")
    void findBySessionId_returnsAttendancesForSession() {
        when(attendanceRepository.findBySessionId(1L)).thenReturn(List.of(attendance));

        List<AttendanceResponse> result = attendanceService.findBySessionId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSessionId()).isEqualTo(1L);
        assertThat(result.get(0).getStatus()).isEqualTo(AttendanceStatus.PRESENT);
        verify(attendanceRepository).findBySessionId(1L);
    }

    @Test
    @DisplayName("findBySessionId - returneaza lista goala daca nu exista prezente")
    void findBySessionId_returnsEmptyList_whenNoAttendances() {
        when(attendanceRepository.findBySessionId(99L)).thenReturn(List.of());

        List<AttendanceResponse> result = attendanceService.findBySessionId(99L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findBySessionId - mapeaza corect datele profesorului din enrollment")
    void findBySessionId_mapsTeacherDataCorrectly() {
        when(attendanceRepository.findBySessionId(1L)).thenReturn(List.of(attendance));

        List<AttendanceResponse> result = attendanceService.findBySessionId(1L);

        AttendanceResponse resp = result.get(0);
        assertThat(resp.getTeacherId()).isEqualTo(1L);
        assertThat(resp.getTeacherFirstName()).isEqualTo("Ion");
        assertThat(resp.getTeacherLastName()).isEqualTo("Pop");
        assertThat(resp.getEnrollmentId()).isEqualTo(1L);
        assertThat(resp.getSessionNumber()).isEqualTo(1);
        assertThat(resp.getSessionTopic()).isEqualTo("SOLID Principles");
    }

    // -------------------------------------------------------------------------
    // findByEnrollmentId
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findByEnrollmentId - returneaza prezentele ordonate pentru o inscriere")
    void findByEnrollmentId_returnsOrderedAttendances() {
        Attendance att2 = buildAttendance(2L, buildSession(2L, course, "Design Patterns", 2), enrollment, AttendanceStatus.ABSENT);
        when(attendanceRepository.findAttendanceByEnrollmentOrdered(1L)).thenReturn(List.of(attendance, att2));

        List<AttendanceResponse> result = attendanceService.findByEnrollmentId(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStatus()).isEqualTo(AttendanceStatus.PRESENT);
        assertThat(result.get(1).getStatus()).isEqualTo(AttendanceStatus.ABSENT);
    }

    @Test
    @DisplayName("findByEnrollmentId - returneaza lista goala daca inscrierea nu are prezente")
    void findByEnrollmentId_returnsEmptyList() {
        when(attendanceRepository.findAttendanceByEnrollmentOrdered(99L)).thenReturn(List.of());

        List<AttendanceResponse> result = attendanceService.findByEnrollmentId(99L);

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // saveBulkAttendance
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("saveBulkAttendance - salveaza prezente noi si marcheaza sesiunea")
    void saveBulkAttendance_savesNewAttendancesAndMarksSession() {
        AttendanceRequest req = buildAttendanceRequest(1L, 1L, AttendanceStatus.PRESENT);

        when(courseSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(attendanceRepository.findBySessionIdAndEnrollmentId(1L, 1L)).thenReturn(Optional.empty());
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);

        List<AttendanceResponse> result = attendanceService.saveBulkAttendance(1L, List.of(req));

        assertThat(result).hasSize(1);
        assertThat(session.getAttendanceMarked()).isTrue();
        verify(courseSessionRepository).save(session);
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    @DisplayName("saveBulkAttendance - actualizeaza prezenta existenta")
    void saveBulkAttendance_updatesExistingAttendance() {
        attendance.setStatus(AttendanceStatus.NOT_MARKED);
        AttendanceRequest req = buildAttendanceRequest(1L, 1L, AttendanceStatus.ABSENT);

        when(courseSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(attendanceRepository.findBySessionIdAndEnrollmentId(1L, 1L)).thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(inv -> inv.getArgument(0));

        List<AttendanceResponse> result = attendanceService.saveBulkAttendance(1L, List.of(req));

        assertThat(result).hasSize(1);
        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.ABSENT);
    }

    @Test
    @DisplayName("saveBulkAttendance - arunca exceptie daca sesiunea nu exista")
    void saveBulkAttendance_throwsException_whenSessionNotFound() {
        when(courseSessionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.saveBulkAttendance(99L, List.of()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("saveBulkAttendance - arunca exceptie daca inscrierea nu exista")
    void saveBulkAttendance_throwsException_whenEnrollmentNotFound() {
        AttendanceRequest req = buildAttendanceRequest(1L, 99L, AttendanceStatus.PRESENT);

        when(courseSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(attendanceRepository.findBySessionIdAndEnrollmentId(1L, 99L)).thenReturn(Optional.empty());
        when(enrollmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.saveBulkAttendance(1L, List.of(req)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // markAll
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("markAll PRESENT - marcheaza toti participantii ca prezenti")
    void markAll_present_marksAllEnrolledAsPresent() {
        when(courseSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(enrollmentRepository.findConfirmedEnrollmentsByCourse(1L)).thenReturn(List.of(enrollment));
        when(attendanceRepository.findBySessionIdAndEnrollmentId(1L, 1L)).thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(inv -> inv.getArgument(0));

        List<AttendanceResponse> result = attendanceService.markAll(1L, AttendanceStatus.PRESENT);

        assertThat(result).hasSize(1);
        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
        assertThat(session.getAttendanceMarked()).isTrue();
        verify(courseSessionRepository).save(session);
    }

    @Test
    @DisplayName("markAll ABSENT - marcheaza toti participantii ca absenti")
    void markAll_absent_marksAllEnrolledAsAbsent() {
        when(courseSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(enrollmentRepository.findConfirmedEnrollmentsByCourse(1L)).thenReturn(List.of(enrollment));
        when(attendanceRepository.findBySessionIdAndEnrollmentId(1L, 1L)).thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(inv -> inv.getArgument(0));

        attendanceService.markAll(1L, AttendanceStatus.ABSENT);

        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.ABSENT);
    }

    @Test
    @DisplayName("markAll - creeaza prezenta noua daca nu exista")
    void markAll_createsNewAttendance_whenNotExists() {
        Attendance newAtt = new Attendance(session, enrollment, AttendanceStatus.PRESENT);
        when(courseSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(enrollmentRepository.findConfirmedEnrollmentsByCourse(1L)).thenReturn(List.of(enrollment));
        when(attendanceRepository.findBySessionIdAndEnrollmentId(1L, 1L)).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(newAtt);

        List<AttendanceResponse> result = attendanceService.markAll(1L, AttendanceStatus.PRESENT);

        assertThat(result).hasSize(1);
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    @DisplayName("markAll - arunca exceptie daca sesiunea nu exista")
    void markAll_throwsException_whenSessionNotFound() {
        when(courseSessionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.markAll(99L, AttendanceStatus.PRESENT))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // getAttendanceStats
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getAttendanceStats - returneaza numarul de prezenti si absenti")
    void getAttendanceStats_returnsCorrectCounts() {
        when(attendanceRepository.countBySessionIdAndStatus(1L, AttendanceStatus.PRESENT)).thenReturn(3L);
        when(attendanceRepository.countBySessionIdAndStatus(1L, AttendanceStatus.ABSENT)).thenReturn(2L);

        Map<String, Long> stats = attendanceService.getAttendanceStats(1L);

        assertThat(stats).containsEntry("present", 3L);
        assertThat(stats).containsEntry("absent", 2L);
    }

    @Test
    @DisplayName("getAttendanceStats - returneaza zero daca nu exista prezente")
    void getAttendanceStats_returnsZero_whenNoAttendances() {
        when(attendanceRepository.countBySessionIdAndStatus(1L, AttendanceStatus.PRESENT)).thenReturn(0L);
        when(attendanceRepository.countBySessionIdAndStatus(1L, AttendanceStatus.ABSENT)).thenReturn(0L);

        Map<String, Long> stats = attendanceService.getAttendanceStats(1L);

        assertThat(stats.get("present")).isZero();
        assertThat(stats.get("absent")).isZero();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private School buildSchool(Long id, String name, String county) {
        School s = new School();
        s.setId(id); s.setName(name); s.setCounty(county); s.setTeacherCount(0);
        return s;
    }

    private User buildUser(Long id, String firstName, String lastName, String email, School school) {
        User u = new User();
        u.setId(id); u.setFirstName(firstName); u.setLastName(lastName);
        u.setEmail(email); u.setSchool(school); u.setActive(true);
        return u;
    }

    private Course buildCourse(Long id, String title) {
        Course c = new Course();
        c.setId(id); c.setTitle(title); c.setMaxParticipants(20); c.setCurrentEnrolled(0);
        return c;
    }

    private CourseSession buildSession(Long id, Course course, String topic, int number) {
        CourseSession cs = new CourseSession();
        cs.setId(id); cs.setCourse(course); cs.setTopic(topic);
        cs.setSessionNumber(number); cs.setAttendanceMarked(false);
        cs.setStartTime(LocalDateTime.now()); cs.setEndTime(LocalDateTime.now().plusHours(2));
        return cs;
    }

    private Enrollment buildEnrollment(Long id, Course course, User teacher) {
        Enrollment e = new Enrollment();
        e.setId(id); e.setCourse(course); e.setTeacher(teacher);
        e.setStatus(EnrollmentStatus.CONFIRMED); e.setCertificateGenerated(false);
        return e;
    }

    private Attendance buildAttendance(Long id, CourseSession session, Enrollment enrollment, AttendanceStatus status) {
        Attendance a = new Attendance();
        a.setId(id); a.setSession(session); a.setEnrollment(enrollment); a.setStatus(status);
        a.setCreatedAt(LocalDateTime.now()); a.setUpdatedAt(LocalDateTime.now());
        return a;
    }

    private AttendanceRequest buildAttendanceRequest(Long sessionId, Long enrollmentId, AttendanceStatus status) {
        AttendanceRequest req = new AttendanceRequest();
        req.setSessionId(sessionId); req.setEnrollmentId(enrollmentId); req.setStatus(status);
        return req;
    }
}