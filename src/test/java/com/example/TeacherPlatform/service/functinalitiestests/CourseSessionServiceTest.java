package com.example.TeacherPlatform.service.functinalitiestests;

import com.example.TeacherPlatform.dataTransferObject.CourseSessionResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.*;
import com.example.TeacherPlatform.model.enums.EnrollmentStatus;
import com.example.TeacherPlatform.model.enums.UserRole;
import com.example.TeacherPlatform.repository.*;
import com.example.TeacherPlatform.service.CourseSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseSessionService - Functionality Tests")
class CourseSessionServiceTest {

    @Mock private CourseSessionRepository courseSessionRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private Authentication authentication;

    @InjectMocks
    private CourseSessionService courseSessionService;

    private User trainer;
    private User teacher;
    private Course course;
    private CourseSession session1;
    private CourseSession session2;

    @BeforeEach
    void setUp() {
        trainer = buildUser(1L, "Ana", "Ionescu", "ana@tech.ro", UserRole.FORMATOR);
        teacher = buildUser(2L, "Ion", "Pop", "ion@edu.ro", UserRole.PROFESOR);

        course = buildCourse(1L, "Clean Code", trainer);

        session1 = buildSession(1L, course, "SOLID Principles", 1,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), false);
        session2 = buildSession(2L, course, "Design Patterns", 2,
                LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(3).plusHours(2), false);
    }

    // -------------------------------------------------------------------------
    // findByCourseId
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findByCourseId - FORMATOR vede sesiunile fara verificare inscriere")
    void findByCourseId_formatorCanSeeSessions() {
        mockAuthAs("ana@tech.ro", UserRole.FORMATOR);
        when(courseRepository.existsById(1L)).thenReturn(true);
        when(courseSessionRepository.findByCourseIdOrdered(1L)).thenReturn(List.of(session1, session2));

        List<CourseSessionResponse> result = courseSessionService.findByCourseId(1L, authentication);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CourseSessionResponse::getSessionNumber)
                .containsExactly(1, 2);
    }

    @Test
    @DisplayName("findByCourseId - PROFESOR cu inscriere confirmata vede sesiunile")
    void findByCourseId_profesorWithConfirmedEnrollment_canSeeSessions() {
        mockAuthAs("ion@edu.ro", UserRole.PROFESOR);
        when(courseRepository.existsById(1L)).thenReturn(true);
        when(enrollmentRepository.hasConfirmedOrCompletedEnrollment(1L, "ion@edu.ro")).thenReturn(true);
        when(courseSessionRepository.findByCourseIdOrdered(1L)).thenReturn(List.of(session1));

        List<CourseSessionResponse> result = courseSessionService.findByCourseId(1L, authentication);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("findByCourseId - PROFESOR fara inscriere primeste exceptie")
    void findByCourseId_profesorWithoutEnrollment_throwsException() {
        mockAuthAs("ion@edu.ro", UserRole.PROFESOR);
        when(courseRepository.existsById(1L)).thenReturn(true);
        when(enrollmentRepository.hasConfirmedOrCompletedEnrollment(1L, "ion@edu.ro")).thenReturn(false);

        assertThatThrownBy(() -> courseSessionService.findByCourseId(1L, authentication))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    @DisplayName("findByCourseId - arunca exceptie daca cursul nu exista")
    void findByCourseId_throwsException_whenCourseNotFound() {
        when(courseRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> courseSessionService.findByCourseId(99L, authentication))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("findByCourseId - mapeaza corect campurile response")
    void findByCourseId_mapsFieldsCorrectly() {
        mockAuthAs("ana@tech.ro", UserRole.FORMATOR);
        when(courseRepository.existsById(1L)).thenReturn(true);
        when(courseSessionRepository.findByCourseIdOrdered(1L)).thenReturn(List.of(session1));

        List<CourseSessionResponse> result = courseSessionService.findByCourseId(1L, authentication);

        CourseSessionResponse resp = result.get(0);
        assertThat(resp.getCourseId()).isEqualTo(1L);
        assertThat(resp.getCourseTitle()).isEqualTo("Clean Code");
        assertThat(resp.getTopic()).isEqualTo("SOLID Principles");
        assertThat(resp.getSessionNumber()).isEqualTo(1);
        assertThat(resp.getAttendanceMarked()).isFalse();
    }

    // -------------------------------------------------------------------------
    // findSessionsByTimeRange
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findSessionsByTimeRange - returneaza sesiunile din intervalul specificat")
    void findSessionsByTimeRange_returnsSessionsInRange() {
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = LocalDateTime.now().plusDays(7);
        when(courseSessionRepository.findSessionsByTimeRange(from, to)).thenReturn(List.of(session1, session2));

        List<CourseSessionResponse> result = courseSessionService.findSessionsByTimeRange(from, to);

        assertThat(result).hasSize(2);
        verify(courseSessionRepository).findSessionsByTimeRange(from, to);
    }

    @Test
    @DisplayName("findSessionsByTimeRange - returneaza lista goala daca nu exista sesiuni in interval")
    void findSessionsByTimeRange_returnsEmptyList_whenNoSessions() {
        LocalDateTime from = LocalDateTime.now().plusDays(30);
        LocalDateTime to = LocalDateTime.now().plusDays(60);
        when(courseSessionRepository.findSessionsByTimeRange(from, to)).thenReturn(List.of());

        List<CourseSessionResponse> result = courseSessionService.findSessionsByTimeRange(from, to);

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // findUnmarkedAttendanceSessions
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findUnmarkedAttendanceSessions - returneaza sesiunile fara prezenta marcata")
    void findUnmarkedAttendanceSessions_returnsUnmarkedSessions() {
        when(courseSessionRepository.findUnmarkedAttendanceSessions()).thenReturn(List.of(session1, session2));

        List<CourseSessionResponse> result = courseSessionService.findUnmarkedAttendanceSessions();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CourseSessionResponse::getAttendanceMarked)
                .containsOnly(false);
        verify(courseSessionRepository).findUnmarkedAttendanceSessions();
    }

    @Test
    @DisplayName("findUnmarkedAttendanceSessions - returneaza lista goala daca toate au prezenta marcata")
    void findUnmarkedAttendanceSessions_returnsEmptyList_whenAllMarked() {
        when(courseSessionRepository.findUnmarkedAttendanceSessions()).thenReturn(List.of());

        List<CourseSessionResponse> result = courseSessionService.findUnmarkedAttendanceSessions();

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // findThisWeekSessions
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findThisWeekSessions - returneaza sesiunile formatorului din saptamana curenta")
    void findThisWeekSessions_returnsTrainerSessionsThisWeek() {
        LocalDateTime startOfWeek = LocalDateTime.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).withHour(0).withMinute(0);
        LocalDateTime endOfWeek = LocalDateTime.now()
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).withHour(23).withMinute(59);

        CourseSession thisWeekSession = buildSession(3L, course, "This Week", 3,
                LocalDateTime.now(), LocalDateTime.now().plusHours(2), false);

        when(authentication.getName()).thenReturn("ana@tech.ro");
        when(userRepository.findByEmail("ana@tech.ro")).thenReturn(Optional.of(trainer));
        when(courseSessionRepository.findSessionsByTimeRange(any(), any()))
                .thenReturn(List.of(thisWeekSession));

        List<CourseSessionResponse> result = courseSessionService.findThisWeekSessions(authentication);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTopic()).isEqualTo("This Week");
    }

    @Test
    @DisplayName("findThisWeekSessions - nu include sesiunile altor formatori")
    void findThisWeekSessions_excludesOtherTrainersSessions() {
        User otherTrainer = buildUser(99L, "Alt", "Formator", "alt@tech.ro", UserRole.FORMATOR);
        Course otherCourse = buildCourse(2L, "Alt Curs", otherTrainer);
        CourseSession otherSession = buildSession(4L, otherCourse, "Other Topic", 1,
                LocalDateTime.now(), LocalDateTime.now().plusHours(2), false);

        when(authentication.getName()).thenReturn("ana@tech.ro");
        when(userRepository.findByEmail("ana@tech.ro")).thenReturn(Optional.of(trainer));
        when(courseSessionRepository.findSessionsByTimeRange(any(), any()))
                .thenReturn(List.of(otherSession));

        List<CourseSessionResponse> result = courseSessionService.findThisWeekSessions(authentication);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findThisWeekSessions - arunca exceptie daca formatorul nu exista")
    void findThisWeekSessions_throwsException_whenTrainerNotFound() {
        when(authentication.getName()).thenReturn("unknown@tech.ro");
        when(userRepository.findByEmail("unknown@tech.ro")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseSessionService.findThisWeekSessions(authentication))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void mockAuthAs(String email, UserRole role) {
        when(authentication.getName()).thenReturn(email);
        Collection<GrantedAuthority> authorities = List.of(() -> role.name());
        doReturn(authorities).when(authentication).getAuthorities();
    }

    private User buildUser(Long id, String first, String last, String email, UserRole role) {
        User u = new User();
        u.setId(id); u.setFirstName(first); u.setLastName(last);
        u.setEmail(email); u.setRole(role); u.setActive(true);
        return u;
    }

    private Course buildCourse(Long id, String title, User trainer) {
        Course c = new Course();
        c.setId(id); c.setTitle(title); c.setTrainer(trainer);
        c.setMaxParticipants(20); c.setCurrentEnrolled(0); c.setSessionCount(0);
        return c;
    }

    private CourseSession buildSession(Long id, Course course, String topic, int number,
                                       LocalDateTime start, LocalDateTime end, boolean attendanceMarked) {
        CourseSession cs = new CourseSession();
        cs.setId(id); cs.setCourse(course); cs.setTopic(topic);
        cs.setSessionNumber(number); cs.setStartTime(start); cs.setEndTime(end);
        cs.setAttendanceMarked(attendanceMarked);
        cs.setCreatedAt(LocalDateTime.now()); cs.setUpdatedAt(LocalDateTime.now());
        return cs;
    }
}