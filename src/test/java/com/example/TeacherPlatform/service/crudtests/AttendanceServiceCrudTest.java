package com.example.TeacherPlatform.service.crudtests;

import com.example.TeacherPlatform.dataTransferObject.AttendanceRequest;
import com.example.TeacherPlatform.dataTransferObject.AttendanceResponse;
import com.example.TeacherPlatform.model.Attendance;
import com.example.TeacherPlatform.model.Course;
import com.example.TeacherPlatform.model.CourseSession;
import com.example.TeacherPlatform.model.Enrollment;
import com.example.TeacherPlatform.model.User;
import com.example.TeacherPlatform.model.enums.AttendanceStatus;
import com.example.TeacherPlatform.repository.AttendanceRepository;
import com.example.TeacherPlatform.repository.CourseSessionRepository;
import com.example.TeacherPlatform.repository.EnrollmentRepository;
import com.example.TeacherPlatform.service.AttendanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceService - Business Logic Tests")
class AttendanceServiceCrudTest {

    @Mock private AttendanceRepository attendanceRepository;
    @Mock private CourseSessionRepository sessionRepository;
    @Mock private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private CourseSession session;
    private Enrollment enrollment1;
    private Enrollment enrollment2;

    @BeforeEach
    void setUp() {
        Course course = new Course();
        course.setId(1L);

        session = new CourseSession();
        session.setId(1L);
        session.setCourse(course);
        session.setAttendanceMarked(false);

        User teacher1 = new User(); teacher1.setId(10L);
        enrollment1 = new Enrollment();
        enrollment1.setId(100L);
        enrollment1.setTeacher(teacher1);

        User teacher2 = new User(); teacher2.setId(20L);
        enrollment2 = new Enrollment();
        enrollment2.setId(200L);
        enrollment2.setTeacher(teacher2);
    }

    @Test
    @DisplayName("Mark All - Marchează toți participanții cu un status dat și setează attendanceMarked = true")
    void markAll_marksAllParticipantsAndUpdatesSession() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        // Returnăm 2 participanți confirmați pentru acest curs
        when(enrollmentRepository.findConfirmedEnrollmentsByCourse(1L)).thenReturn(List.of(enrollment1, enrollment2));

        // Simulăm că nu au prezența marcată anterior (Optional.empty)
        when(attendanceRepository.findBySessionIdAndEnrollmentId(eq(1L), anyLong())).thenReturn(Optional.empty());

        // Mock save
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(i -> i.getArgument(0));

        // Acțiune: Formatorul apasă butonul "Toți Prezenți"
        List<AttendanceResponse> responses = attendanceService.markAll(1L, AttendanceStatus.PRESENT);

        // Assertions
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getStatus()).isEqualTo(AttendanceStatus.PRESENT);
        assertThat(responses.get(1).getStatus()).isEqualTo(AttendanceStatus.PRESENT);

        // Verificăm dacă sesiunea a fost marcată ca având catalogul completat
        assertThat(session.getAttendanceMarked()).isTrue();
        verify(sessionRepository).save(session);

        // Verificăm că a salvat prezența pentru fiecare în parte
        verify(attendanceRepository, times(2)).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Save Bulk Attendance - Actualizează lista de prezențe trimisă de formator")
    void saveBulkAttendance_success() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        // Formatorul trimite o listă: primul prezent, al doilea absent
        AttendanceRequest req1 = new AttendanceRequest(); req1.setEnrollmentId(100L); req1.setStatus(AttendanceStatus.PRESENT);
        AttendanceRequest req2 = new AttendanceRequest(); req2.setEnrollmentId(200L); req2.setStatus(AttendanceStatus.ABSENT);

        when(enrollmentRepository.findById(100L)).thenReturn(Optional.of(enrollment1));
        when(enrollmentRepository.findById(200L)).thenReturn(Optional.of(enrollment2));
        when(attendanceRepository.findBySessionIdAndEnrollmentId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(i -> i.getArgument(0));

        List<AttendanceResponse> responses = attendanceService.saveBulkAttendance(1L, List.of(req1, req2));

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getStatus()).isEqualTo(AttendanceStatus.PRESENT);
        assertThat(responses.get(1).getStatus()).isEqualTo(AttendanceStatus.ABSENT);

        assertThat(session.getAttendanceMarked()).isTrue();
        verify(sessionRepository).save(session);
    }
}