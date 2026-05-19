package com.example.TeacherPlatform.service.functinalitiestests;

import com.example.TeacherPlatform.dataTransferObject.EnrollmentRequest;
import com.example.TeacherPlatform.dataTransferObject.EnrollmentResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.*;
import com.example.TeacherPlatform.model.enums.CourseStatus;
import com.example.TeacherPlatform.model.enums.EnrollmentStatus;
import com.example.TeacherPlatform.model.enums.UserRole;
import com.example.TeacherPlatform.repository.*;
import com.example.TeacherPlatform.service.EnrollmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EnrollmentService - Functionality Tests")
class EnrollmentServiceTest {

    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private UserRepository userRepository;
    @Mock private Authentication authentication;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private User trainer;
    private User teacher;
    private Course course;
    private Enrollment pendingEnrollment;
    private Enrollment confirmedEnrollment;
    private Enrollment completedEnrollment;

    @BeforeEach
    void setUp() {
        trainer = buildUser(1L, "Ana", "Ionescu", "ana@tech.ro", UserRole.FORMATOR);
        teacher = buildUser(2L, "Ion", "Pop", "ion@edu.ro", UserRole.PROFESOR);

        course = buildCourse(1L, "Clean Code", trainer, 20, 5);

        pendingEnrollment = buildEnrollment(1L, course, teacher, EnrollmentStatus.PENDING);
        confirmedEnrollment = buildEnrollment(2L, course, teacher, EnrollmentStatus.CONFIRMED);
        completedEnrollment = buildEnrollment(3L, course, teacher, EnrollmentStatus.COMPLETED);
    }

    // -------------------------------------------------------------------------
    // createEnrollment
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("createEnrollment - profesor se poate inscrie la un curs disponibil")
    void createEnrollment_profesorCanEnroll() {
        EnrollmentRequest req = new EnrollmentRequest();
        req.setCourseId(1L);

        when(authentication.getName()).thenReturn("ion@edu.ro");
        when(userRepository.findByEmail("ion@edu.ro")).thenReturn(Optional.of(teacher));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByCourseIdAndTeacherId(1L, 2L)).thenReturn(Optional.empty());
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> {
            Enrollment e = inv.getArgument(0);
            e.setId(10L);
            e.setCreatedAt(LocalDateTime.now());
            return e;
        });

        EnrollmentResponse result = enrollmentService.createEnrollment(req, authentication);

        assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.PENDING);
        assertThat(result.getCourseId()).isEqualTo(1L);
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("createEnrollment - arunca exceptie daca profesorul este deja inscris")
    void createEnrollment_throwsException_whenAlreadyEnrolled() {
        EnrollmentRequest req = new EnrollmentRequest();
        req.setCourseId(1L);

        when(authentication.getName()).thenReturn("ion@edu.ro");
        when(userRepository.findByEmail("ion@edu.ro")).thenReturn(Optional.of(teacher));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByCourseIdAndTeacherId(1L, 2L)).thenReturn(Optional.of(pendingEnrollment));

        assertThatThrownBy(() -> enrollmentService.createEnrollment(req, authentication))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("deja înscris");
    }

    @Test
    @DisplayName("createEnrollment - arunca exceptie daca cursul este plin")
    void createEnrollment_throwsException_whenCourseIsFull() {
        course.setCurrentEnrolled(20);
        course.setMaxParticipants(20);

        EnrollmentRequest req = new EnrollmentRequest();
        req.setCourseId(1L);

        when(authentication.getName()).thenReturn("ion@edu.ro");
        when(userRepository.findByEmail("ion@edu.ro")).thenReturn(Optional.of(teacher));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByCourseIdAndTeacherId(1L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.createEnrollment(req, authentication))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("capacitatea maximă");
    }

    // -------------------------------------------------------------------------
    // getMyEnrollments
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getMyEnrollments - returneaza toate inscrierile profesorului")
    void getMyEnrollments_returnsAllEnrollments() {
        when(authentication.getName()).thenReturn("ion@edu.ro");
        when(userRepository.findByEmail("ion@edu.ro")).thenReturn(Optional.of(teacher));
        when(enrollmentRepository.findByTeacherId(2L))
                .thenReturn(List.of(pendingEnrollment, confirmedEnrollment, completedEnrollment));

        List<EnrollmentResponse> result = enrollmentService.getMyEnrollments(authentication);

        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("getMyEnrollments - returneaza lista goala daca nu are inscrieri")
    void getMyEnrollments_returnsEmptyList_whenNoEnrollments() {
        when(authentication.getName()).thenReturn("ion@edu.ro");
        when(userRepository.findByEmail("ion@edu.ro")).thenReturn(Optional.of(teacher));
        when(enrollmentRepository.findByTeacherId(2L)).thenReturn(List.of());

        List<EnrollmentResponse> result = enrollmentService.getMyEnrollments(authentication);

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // getMyActiveEnrollments
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getMyActiveEnrollments - returneaza doar inscrierile confirmate")
    void getMyActiveEnrollments_returnsOnlyConfirmed() {
        when(authentication.getName()).thenReturn("ion@edu.ro");
        when(userRepository.findByEmail("ion@edu.ro")).thenReturn(Optional.of(teacher));
        when(enrollmentRepository.findConfirmedEnrollmentsByTeacher(2L)).thenReturn(List.of(confirmedEnrollment));

        List<EnrollmentResponse> result = enrollmentService.getMyActiveEnrollments(authentication);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(EnrollmentStatus.CONFIRMED);
    }

    // -------------------------------------------------------------------------
    // getMyCompletedEnrollments
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getMyCompletedEnrollments - returneaza doar inscrierile finalizate")
    void getMyCompletedEnrollments_returnsOnlyCompleted() {
        when(authentication.getName()).thenReturn("ion@edu.ro");
        when(userRepository.findByEmail("ion@edu.ro")).thenReturn(Optional.of(teacher));
        when(enrollmentRepository.findByTeacherAndStatus(2L, EnrollmentStatus.COMPLETED))
                .thenReturn(List.of(completedEnrollment));

        List<EnrollmentResponse> result = enrollmentService.getMyCompletedEnrollments(authentication);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(EnrollmentStatus.COMPLETED);
    }

    // -------------------------------------------------------------------------
    // checkEnrollment
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("checkEnrollment - returneaza true daca profesorul este inscris")
    void checkEnrollment_returnsTrue_whenEnrolled() {
        when(authentication.getName()).thenReturn("ion@edu.ro");
        when(userRepository.findByEmail("ion@edu.ro")).thenReturn(Optional.of(teacher));
        when(enrollmentRepository.findByCourseIdAndTeacherId(1L, 2L)).thenReturn(Optional.of(pendingEnrollment));

        boolean result = enrollmentService.checkEnrollment(1L, authentication);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("checkEnrollment - returneaza false daca profesorul nu este inscris")
    void checkEnrollment_returnsFalse_whenNotEnrolled() {
        when(authentication.getName()).thenReturn("ion@edu.ro");
        when(userRepository.findByEmail("ion@edu.ro")).thenReturn(Optional.of(teacher));
        when(enrollmentRepository.findByCourseIdAndTeacherId(1L, 2L)).thenReturn(Optional.empty());

        boolean result = enrollmentService.checkEnrollment(1L, authentication);

        assertThat(result).isFalse();
    }

    // -------------------------------------------------------------------------
    // cancelEnrollment (PROFESOR)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("cancelEnrollment - profesorul isi poate anula propria inscriere")
    void cancelEnrollment_profesorCanCancelOwnEnrollment() {
        when(authentication.getName()).thenReturn("ion@edu.ro");
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(pendingEnrollment));
        when(userRepository.findByEmail("ion@edu.ro")).thenReturn(Optional.of(teacher));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));

        enrollmentService.cancelEnrollment(1L, authentication);

        assertThat(pendingEnrollment.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
        verify(enrollmentRepository).save(pendingEnrollment);
    }

    @Test
    @DisplayName("cancelEnrollment - anularea unei inscrieri CONFIRMED decrementeaza currentEnrolled")
    void cancelEnrollment_confirmedEnrollment_decrementsCurrentEnrolled() {
        course.setCurrentEnrolled(5);
        when(authentication.getName()).thenReturn("ion@edu.ro");
        when(enrollmentRepository.findById(2L)).thenReturn(Optional.of(confirmedEnrollment));
        when(userRepository.findByEmail("ion@edu.ro")).thenReturn(Optional.of(teacher));
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));

        enrollmentService.cancelEnrollment(2L, authentication);

        assertThat(course.getCurrentEnrolled()).isEqualTo(4);
        assertThat(confirmedEnrollment.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
    }

    @Test
    @DisplayName("cancelEnrollment - arunca exceptie daca profesorul nu este owner")
    void cancelEnrollment_throwsException_whenNotOwner() {
        User anotherTeacher = buildUser(99L, "Alt", "Profesor", "alt@edu.ro", UserRole.PROFESOR);
        when(authentication.getName()).thenReturn("alt@edu.ro");
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(pendingEnrollment));
        when(userRepository.findByEmail("alt@edu.ro")).thenReturn(Optional.of(anotherTeacher));

        assertThatThrownBy(() -> enrollmentService.cancelEnrollment(1L, authentication))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access Denied");
    }

    // -------------------------------------------------------------------------
    // cancelEnrollmentAsTrainer (FORMATOR)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("cancelEnrollmentAsTrainer - formatorul poate anula inscrierea unui participant")
    void cancelEnrollmentAsTrainer_trainerCanCancelParticipant() {
        when(authentication.getName()).thenReturn("ana@tech.ro");
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(pendingEnrollment));
        when(userRepository.findByEmail("ana@tech.ro")).thenReturn(Optional.of(trainer));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));

        enrollmentService.cancelEnrollmentAsTrainer(1L, authentication);

        assertThat(pendingEnrollment.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
    }

    @Test
    @DisplayName("cancelEnrollmentAsTrainer - arunca exceptie daca formatorul nu este trainer-ul cursului")
    void cancelEnrollmentAsTrainer_throwsException_whenNotCourseTrainer() {
        User otherTrainer = buildUser(99L, "Alt", "Formator", "alt@tech.ro", UserRole.FORMATOR);
        when(authentication.getName()).thenReturn("alt@tech.ro");
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(pendingEnrollment));
        when(userRepository.findByEmail("alt@tech.ro")).thenReturn(Optional.of(otherTrainer));

        assertThatThrownBy(() -> enrollmentService.cancelEnrollmentAsTrainer(1L, authentication))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access Denied");
    }

    // -------------------------------------------------------------------------
    // confirmEnrollment
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("confirmEnrollment - confirma o inscriere PENDING si incrementeaza currentEnrolled")
    void confirmEnrollment_confirmsPendingAndIncrementsEnrolled() {
        course.setCurrentEnrolled(5);
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(pendingEnrollment));
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));

        EnrollmentResponse result = enrollmentService.confirmEnrollment(1L);

        assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.CONFIRMED);
        assertThat(course.getCurrentEnrolled()).isEqualTo(6);
    }

    @Test
    @DisplayName("confirmEnrollment - arunca exceptie daca inscrierea nu este PENDING")
    void confirmEnrollment_throwsException_whenNotPending() {
        when(enrollmentRepository.findById(2L)).thenReturn(Optional.of(confirmedEnrollment));

        assertThatThrownBy(() -> enrollmentService.confirmEnrollment(2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    @DisplayName("confirmEnrollment - arunca exceptie daca cursul este plin")
    void confirmEnrollment_throwsException_whenCourseFull() {
        course.setCurrentEnrolled(20);
        course.setMaxParticipants(20);
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(pendingEnrollment));

        assertThatThrownBy(() -> enrollmentService.confirmEnrollment(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("plin");
    }

    // -------------------------------------------------------------------------
    // getCourseEnrollmentStats
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getCourseEnrollmentStats - returneaza statisticile corecte")
    void getCourseEnrollmentStats_returnsCorrectStats() {
        Enrollment cancelledEnrollment = buildEnrollment(4L, course, teacher, EnrollmentStatus.CANCELLED);
        when(enrollmentRepository.findByCourseId(1L))
                .thenReturn(List.of(pendingEnrollment, confirmedEnrollment, completedEnrollment, cancelledEnrollment));

        Map<String, Long> stats = enrollmentService.getCourseEnrollmentStats(1L);

        assertThat(stats.get("total")).isEqualTo(4L);
        assertThat(stats.get("confirmed")).isEqualTo(1L);
        assertThat(stats.get("pending")).isEqualTo(1L);
        assertThat(stats.get("cancelled")).isEqualTo(1L);
    }

    // -------------------------------------------------------------------------
    // getEnrollmentsThisMonth
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getEnrollmentsThisMonth - returneaza inscrierile din luna curenta")
    void getEnrollmentsThisMonth_returnsCurrentMonthEnrollments() {
        pendingEnrollment.setCreatedAt(LocalDateTime.now());
        confirmedEnrollment.setCreatedAt(LocalDateTime.now().minusMonths(2));

        when(enrollmentRepository.findAll()).thenReturn(List.of(pendingEnrollment, confirmedEnrollment));

        List<EnrollmentResponse> result = enrollmentService.getEnrollmentsThisMonth();

        assertThat(result).hasSize(1);
    }

    // -------------------------------------------------------------------------
    // completeEnrollment
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("completeEnrollment - finalizeaza o inscriere CONFIRMED")
    void completeEnrollment_completesConfirmedEnrollment() {
        when(enrollmentRepository.findById(2L)).thenReturn(Optional.of(confirmedEnrollment));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));

        EnrollmentResponse result = enrollmentService.completeEnrollment(2L);

        assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.COMPLETED);
    }

    @Test
    @DisplayName("completeEnrollment - arunca exceptie daca inscrierea nu este CONFIRMED")
    void completeEnrollment_throwsException_whenNotConfirmed() {
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(pendingEnrollment));

        assertThatThrownBy(() -> enrollmentService.completeEnrollment(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("CONFIRMED");
    }

    // -------------------------------------------------------------------------
    // getEnrollmentsByCourse
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getEnrollmentsByCourse - returneaza toate inscrierile unui curs")
    void getEnrollmentsByCourse_returnsAllEnrollmentsForCourse() {
        when(enrollmentRepository.findByCourseId(1L))
                .thenReturn(List.of(pendingEnrollment, confirmedEnrollment));

        List<EnrollmentResponse> result = enrollmentService.getEnrollmentsByCourse(1L);

        assertThat(result).hasSize(2);
    }

    // -------------------------------------------------------------------------
    // getPendingEnrollments
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getPendingEnrollments - returneaza inscrierile in asteptare")
    void getPendingEnrollments_returnsPendingEnrollments() {
        when(enrollmentRepository.findPendingEnrollments()).thenReturn(List.of(pendingEnrollment));

        List<EnrollmentResponse> result = enrollmentService.getPendingEnrollments();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(EnrollmentStatus.PENDING);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private User buildUser(Long id, String first, String last, String email, UserRole role) {
        User u = new User();
        u.setId(id); u.setFirstName(first); u.setLastName(last);
        u.setEmail(email); u.setRole(role); u.setActive(true);
        return u;
    }

    private Course buildCourse(Long id, String title, User trainer, int maxParticipants, int currentEnrolled) {
        Course c = new Course();
        c.setId(id); c.setTitle(title); c.setTrainer(trainer);
        c.setMaxParticipants(maxParticipants); c.setCurrentEnrolled(currentEnrolled);
        c.setStatus(CourseStatus.ACTIVE); c.setSessionCount(0);
        c.setStartDate(LocalDate.now().plusDays(1));
        c.setEndDate(LocalDate.now().plusDays(30));
        c.setThumbnailUrl(""); c.setCreditHours(10);
        return c;
    }

    private Enrollment buildEnrollment(Long id, Course course, User teacher, EnrollmentStatus status) {
        Enrollment e = new Enrollment();
        e.setId(id); e.setCourse(course); e.setTeacher(teacher);
        e.setStatus(status); e.setCertificateGenerated(false);
        e.setCreatedAt(LocalDateTime.now());
        return e;
    }
}