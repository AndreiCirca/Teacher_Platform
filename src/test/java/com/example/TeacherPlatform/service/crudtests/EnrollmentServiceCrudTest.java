package com.example.TeacherPlatform.service.crudtests;

import com.example.TeacherPlatform.dataTransferObject.EnrollmentRequest;
import com.example.TeacherPlatform.dataTransferObject.EnrollmentResponse;
import com.example.TeacherPlatform.model.Course;
import com.example.TeacherPlatform.model.Enrollment;
import com.example.TeacherPlatform.model.User;
import com.example.TeacherPlatform.model.enums.EnrollmentStatus;
import com.example.TeacherPlatform.repository.CourseRepository;
import com.example.TeacherPlatform.repository.EnrollmentRepository;
import com.example.TeacherPlatform.repository.UserRepository;
import com.example.TeacherPlatform.service.EnrollmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EnrollmentService - Business Logic Tests")
class EnrollmentServiceCrudTest {

    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private UserRepository userRepository;
    @Mock private Authentication authentication;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private User teacher;
    private Course course;
    private Enrollment enrollment;
    private EnrollmentRequest request;

    @BeforeEach
    void setUp() {
        teacher = new User();
        teacher.setId(1L);
        teacher.setEmail("profesor@edu.ro");

        course = new Course();
        course.setId(1L);
        course.setTitle("Robotica 101");
        course.setMaxParticipants(25);
        course.setCurrentEnrolled(10); // 10 locuri ocupate din 25

        enrollment = new Enrollment();
        enrollment.setId(1L);
        enrollment.setCourse(course);
        enrollment.setTeacher(teacher);
        enrollment.setStatus(EnrollmentStatus.PENDING);

        request = new EnrollmentRequest();
        request.setCourseId(1L);
    }

    @Test
    @DisplayName("Create Enrollment - Profesorul se înscrie, statusul devine PENDING")
    void createEnrollment_success() {
        when(authentication.getName()).thenReturn("profesor@edu.ro");
        when(userRepository.findByEmail("profesor@edu.ro")).thenReturn(Optional.of(teacher));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByCourseIdAndTeacherId(1L, 1L)).thenReturn(Optional.empty()); // Nu e deja înscris

        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(i -> {
            Enrollment e = i.getArgument(0);
            e.setId(100L);
            return e;
        });

        EnrollmentResponse response = enrollmentService.createEnrollment(request, authentication);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getStatus()).isEqualTo(EnrollmentStatus.PENDING);
    }

    @Test
    @DisplayName("Create Enrollment - Aruncă excepție dacă profesorul e deja înscris")
    void createEnrollment_throwsException_whenAlreadyEnrolled() {
        when(authentication.getName()).thenReturn("profesor@edu.ro");
        when(userRepository.findByEmail("profesor@edu.ro")).thenReturn(Optional.of(teacher));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByCourseIdAndTeacherId(1L, 1L)).thenReturn(Optional.of(enrollment));

        assertThatThrownBy(() -> enrollmentService.createEnrollment(request, authentication))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ești deja înscris la acest curs");
    }

    @Test
    @DisplayName("Create Enrollment - Aruncă excepție dacă cursul e plin")
    void createEnrollment_throwsException_whenCourseIsFull() {
        course.setCurrentEnrolled(25); // Capacitate atinsă

        when(authentication.getName()).thenReturn("profesor@edu.ro");
        when(userRepository.findByEmail("profesor@edu.ro")).thenReturn(Optional.of(teacher));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByCourseIdAndTeacherId(1L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.createEnrollment(request, authentication))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Acest curs și-a atins capacitatea maximă");
    }

    @Test
    @DisplayName("Confirm Enrollment - Schimbă statusul și ocupă un loc în curs")
    void confirmEnrollment_success() {
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);

        EnrollmentResponse response = enrollmentService.confirmEnrollment(1L);

        assertThat(response.getStatus()).isEqualTo(EnrollmentStatus.CONFIRMED);
        assertThat(course.getCurrentEnrolled()).isEqualTo(11); // A crescut de la 10 la 11
        verify(courseRepository).save(course);
    }

    @Test
    @DisplayName("Cancel Enrollment - Formatorul anulează o înscriere CONFIRMATĂ, eliberează un loc")
    void cancelEnrollment_freesUpSpot_ifConfirmed() {
        enrollment.setStatus(EnrollmentStatus.CONFIRMED); // Deja confirmat

        User trainer = new User();
        trainer.setId(2L);
        course.setTrainer(trainer); // Setăm trainer-ul cursului

        when(authentication.getName()).thenReturn("trainer@edu.ro");
        when(userRepository.findByEmail("trainer@edu.ro")).thenReturn(Optional.of(trainer));
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));

        enrollmentService.cancelEnrollmentAsTrainer(1L, authentication);

        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
        assertThat(course.getCurrentEnrolled()).isEqualTo(9); // A scăzut de la 10 la 9
        verify(courseRepository).save(course);
        verify(enrollmentRepository).save(enrollment);
    }
}