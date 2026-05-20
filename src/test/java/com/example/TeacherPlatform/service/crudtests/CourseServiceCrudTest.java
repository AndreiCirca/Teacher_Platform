package com.example.TeacherPlatform.service.crudtests;

import com.example.TeacherPlatform.dataTransferObject.CourseRequest;
import com.example.TeacherPlatform.dataTransferObject.CourseResponse;
import com.example.TeacherPlatform.model.Course;
import com.example.TeacherPlatform.model.CourseCategory;
import com.example.TeacherPlatform.model.User;
import com.example.TeacherPlatform.model.enums.CourseStatus;
import com.example.TeacherPlatform.model.enums.UserRole;
import com.example.TeacherPlatform.repository.CourseCategoryRepository;
import com.example.TeacherPlatform.repository.CourseRepository;
import com.example.TeacherPlatform.repository.UserRepository;
import com.example.TeacherPlatform.service.CourseService;
import com.example.TeacherPlatform.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseService - Business Logic Tests")
class CourseServiceCrudTest {

    @Mock private CourseRepository courseRepository;
    @Mock private CourseCategoryRepository categoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;
    @Mock private Authentication authentication; // Mockăm obiectul de securitate

    @InjectMocks
    private CourseService courseService;

    private User trainer;
    private CourseCategory category;
    private Course course;
    private CourseRequest request;

    @BeforeEach
    void setUp() {
        trainer = new User();
        trainer.setId(1L);
        trainer.setEmail("trainer@edu.ro");
        trainer.setRole(UserRole.FORMATOR);

        category = new CourseCategory();
        category.setId(1L);
        category.setName("TIC");

        course = new Course();
        course.setId(1L);
        course.setTitle("Curs de Test");
        course.setTrainer(trainer);
        course.setCategory(category);
        course.setStatus(CourseStatus.PENDING_APPROVAL);

        request = new CourseRequest();
        request.setTitle("Curs de Test");
        request.setCategoryId(1L);
        request.setTrainerId(1L);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(5));
        request.setCreditHours(20);
        request.setMaxParticipants(30);
        request.setIsOnline(true);
        request.setMeetingLink("https://zoom.us/test");
    }

    @Test
    @DisplayName("Propose Course - Formatorul propune curs și statusul devine PENDING_APPROVAL")
    void proposeCourse_savesAsPending() {
        // Configurăm mock-ul de securitate să ne dea email-ul formatorului
        when(authentication.getName()).thenReturn("trainer@edu.ro");
        when(userRepository.findByEmail("trainer@edu.ro")).thenReturn(Optional.of(trainer));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        when(courseRepository.save(any(Course.class))).thenAnswer(i -> {
            Course c = i.getArgument(0);
            c.setId(10L);
            return c;
        });

        CourseResponse response = courseService.proposeCourse(request, authentication);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getStatus()).isEqualTo(CourseStatus.PENDING_APPROVAL);
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("Approve Course - Adminul aprobă, cursul devine ACTIVE și se trimite notificare")
    void approveCourse_setsActiveAndNotifies() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenReturn(course); // mock save

        CourseResponse response = courseService.approveCourse(1L);

        assertThat(response.getStatus()).isEqualTo(CourseStatus.ACTIVE);
        verify(courseRepository).save(course);
        // Verificăm dacă serviciul de notificări a fost apelat o dată
        verify(notificationService, times(1)).sendNotification(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Reject Course - Adminul respinge, cursul redevine DRAFT")
    void rejectCourse_setsDraftAndNotifies() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        CourseResponse response = courseService.rejectCourse(1L, "Ai greșit data");

        assertThat(response.getStatus()).isEqualTo(CourseStatus.DRAFT);
        verify(courseRepository).save(course);
        verify(notificationService, times(1)).sendNotification(any(), any(), any(), any(), any());
    }
}