package com.example.TeacherPlatform.service.functinalitiestests;

import com.example.TeacherPlatform.dataTransferObject.CourseRequest;
import com.example.TeacherPlatform.dataTransferObject.CourseResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.*;
import com.example.TeacherPlatform.model.enums.CourseStatus;
import com.example.TeacherPlatform.model.enums.UserRole;
import com.example.TeacherPlatform.repository.*;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseService - Functionality Tests")
class CourseServiceTest {

    @Mock private CourseRepository courseRepository;
    @Mock private CourseCategoryRepository courseCategoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;
    @Mock private Authentication authentication;

    @InjectMocks
    private CourseService courseService;

    private CourseCategory category;
    private User trainer;
    private Course activeCourse;
    private Course pendingCourse;
    private Course draftCourse;

    @BeforeEach
    void setUp() {
        category = buildCategory(1L, "Robotică");
        trainer = buildUser(1L, "Ana", "Ionescu", "ana@tech.ro", UserRole.FORMATOR);

        activeCourse = buildCourse(1L, "Clean Code", CourseStatus.ACTIVE, trainer, category, 5, 20);
        pendingCourse = buildCourse(2L, "ROS 2", CourseStatus.PENDING_APPROVAL, trainer, category, 0, 15);
        draftCourse = buildCourse(3L, "Docker", CourseStatus.DRAFT, trainer, category, 0, 10);
    }

    // -------------------------------------------------------------------------
    // findAvailableCourses
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findAvailableCourses - returneaza cursurile cu locuri disponibile")
    void findAvailableCourses_returnsCoursesWithAvailableSpots() {
        when(courseRepository.findAvailableCourses()).thenReturn(List.of(activeCourse));

        List<CourseResponse> result = courseService.findAvailableCourses();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(CourseStatus.ACTIVE);
        verify(courseRepository).findAvailableCourses();
    }

    @Test
    @DisplayName("findAvailableCourses - returneaza lista goala daca nu exista cursuri disponibile")
    void findAvailableCourses_returnsEmptyList_whenNoneAvailable() {
        when(courseRepository.findAvailableCourses()).thenReturn(List.of());

        List<CourseResponse> result = courseService.findAvailableCourses();

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // findUpcomingCourses
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findUpcomingCourses - returneaza cursurile viitoare")
    void findUpcomingCourses_returnsFutureCourses() {
        when(courseRepository.findUpcomingCourses(any(LocalDate.class))).thenReturn(List.of(activeCourse));

        List<CourseResponse> result = courseService.findUpcomingCourses();

        assertThat(result).hasSize(1);
        verify(courseRepository).findUpcomingCourses(any(LocalDate.class));
    }

    // -------------------------------------------------------------------------
    // findPopularCourses
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findPopularCourses - returneaza maxim 6 cursuri ordonate dupa inscrieri")
    void findPopularCourses_returnsTop6ByEnrollments() {
        Course c1 = buildCourse(1L, "C1", CourseStatus.ACTIVE, trainer, category, 10, 20);
        Course c2 = buildCourse(2L, "C2", CourseStatus.ACTIVE, trainer, category, 8, 20);
        Course c3 = buildCourse(3L, "C3", CourseStatus.ACTIVE, trainer, category, 6, 20);
        Course c4 = buildCourse(4L, "C4", CourseStatus.ACTIVE, trainer, category, 4, 20);
        Course c5 = buildCourse(5L, "C5", CourseStatus.ACTIVE, trainer, category, 2, 20);
        Course c6 = buildCourse(6L, "C6", CourseStatus.ACTIVE, trainer, category, 1, 20);
        Course c7 = buildCourse(7L, "C7", CourseStatus.ACTIVE, trainer, category, 0, 20);

        when(courseRepository.findByStatus(CourseStatus.ACTIVE)).thenReturn(List.of(c1, c2, c3, c4, c5, c6, c7));

        List<CourseResponse> result = courseService.findPopularCourses();

        assertThat(result).hasSize(6);
        assertThat(result.get(0).getCurrentEnrolled()).isGreaterThanOrEqualTo(result.get(1).getCurrentEnrolled());
    }

    @Test
    @DisplayName("findPopularCourses - returneaza mai putin de 6 daca exista mai putine cursuri active")
    void findPopularCourses_returnsLessThan6_whenFewerCourses() {
        when(courseRepository.findByStatus(CourseStatus.ACTIVE)).thenReturn(List.of(activeCourse));

        List<CourseResponse> result = courseService.findPopularCourses();

        assertThat(result).hasSize(1);
    }

    // -------------------------------------------------------------------------
    // findByCategoryId
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findByCategoryId - returneaza cursurile dintr-o categorie")
    void findByCategoryId_returnsCoursesByCategory() {
        when(courseRepository.findByCategoryId(1L)).thenReturn(List.of(activeCourse));

        List<CourseResponse> result = courseService.findByCategoryId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategoryId()).isEqualTo(1L);
        assertThat(result.get(0).getCategoryName()).isEqualTo("Robotică");
    }

    @Test
    @DisplayName("findByCategoryId - returneaza lista goala daca categoria nu are cursuri")
    void findByCategoryId_returnsEmptyList_whenNoCourses() {
        when(courseRepository.findByCategoryId(99L)).thenReturn(List.of());

        List<CourseResponse> result = courseService.findByCategoryId(99L);

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // proposeCourse
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("proposeCourse - formator propune un curs cu status PENDING_APPROVAL")
    void proposeCourse_createsCourseWithPendingStatus() {
        CourseRequest req = buildCourseRequest("Noul Curs", 1L, 1L, true, "https://meet.google.com/abc");
        when(authentication.getName()).thenReturn("ana@tech.ro");
        when(userRepository.findByEmail("ana@tech.ro")).thenReturn(Optional.of(trainer));
        when(courseCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> {
            Course c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        CourseResponse result = courseService.proposeCourse(req, authentication);

        assertThat(result.getStatus()).isEqualTo(CourseStatus.PENDING_APPROVAL);
        assertThat(result.getTitle()).isEqualTo("Noul Curs");
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("proposeCourse - arunca exceptie daca formator nu exista")
    void proposeCourse_throwsException_whenTrainerNotFound() {
        CourseRequest req = buildCourseRequest("Curs", 1L, 1L, true, "https://meet.google.com/abc");
        when(authentication.getName()).thenReturn("unknown@tech.ro");
        when(userRepository.findByEmail("unknown@tech.ro")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.proposeCourse(req, authentication))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("proposeCourse - arunca exceptie pentru curs online fara meeting link")
    void proposeCourse_throwsException_whenOnlineCourseHasNoMeetingLink() {
        CourseRequest req = buildCourseRequest("Curs Online", 1L, 1L, true, null);
        when(authentication.getName()).thenReturn("ana@tech.ro");
        when(userRepository.findByEmail("ana@tech.ro")).thenReturn(Optional.of(trainer));
        when(courseCategoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> courseService.proposeCourse(req, authentication))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("meeting link");
    }

    @Test
    @DisplayName("proposeCourse - arunca exceptie pentru curs fizic fara locatie")
    void proposeCourse_throwsException_whenOfflineCourseHasNoLocation() {
        CourseRequest req = buildCourseRequest("Curs Fizic", 1L, 1L, false, null);
        req.setLocation(null);
        when(authentication.getName()).thenReturn("ana@tech.ro");
        when(userRepository.findByEmail("ana@tech.ro")).thenReturn(Optional.of(trainer));
        when(courseCategoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> courseService.proposeCourse(req, authentication))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("location");
    }

    // -------------------------------------------------------------------------
    // findMyCoursesAsTrainer
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findMyCoursesAsTrainer - returneaza cursurile formatorului autentificat")
    void findMyCoursesAsTrainer_returnsTrainerCourses() {
        when(authentication.getName()).thenReturn("ana@tech.ro");
        when(userRepository.findByEmail("ana@tech.ro")).thenReturn(Optional.of(trainer));
        when(courseRepository.findByTrainerId(1L)).thenReturn(List.of(activeCourse, pendingCourse));

        List<CourseResponse> result = courseService.findMyCoursesAsTrainer(authentication);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CourseResponse::getTitle)
                .containsExactlyInAnyOrder("Clean Code", "ROS 2");
    }

    @Test
    @DisplayName("findMyCoursesAsTrainer - returneaza lista goala daca formatorul nu are cursuri")
    void findMyCoursesAsTrainer_returnsEmptyList_whenNoCourses() {
        when(authentication.getName()).thenReturn("ana@tech.ro");
        when(userRepository.findByEmail("ana@tech.ro")).thenReturn(Optional.of(trainer));
        when(courseRepository.findByTrainerId(1L)).thenReturn(List.of());

        List<CourseResponse> result = courseService.findMyCoursesAsTrainer(authentication);

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // updateCourseAsTrainer
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateCourseAsTrainer - formator poate edita un curs DRAFT")
    void updateCourseAsTrainer_trainerCanEditDraftCourse() {
        CourseRequest req = buildCourseRequest("Docker Updated", 1L, 1L, true, "https://meet.google.com/upd");
        when(authentication.getName()).thenReturn("ana@tech.ro");
        when(courseRepository.findById(3L)).thenReturn(Optional.of(draftCourse));
        when(userRepository.findByEmail("ana@tech.ro")).thenReturn(Optional.of(trainer));
        when(courseCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseResponse result = courseService.updateCourseAsTrainer(3L, req, authentication);

        assertThat(result.getTitle()).isEqualTo("Docker Updated");
        assertThat(result.getStatus()).isEqualTo(CourseStatus.PENDING_APPROVAL);
    }

    @Test
    @DisplayName("updateCourseAsTrainer - arunca exceptie daca formatorul nu este owner")
    void updateCourseAsTrainer_throwsException_whenNotOwner() {
        User otherTrainer = buildUser(99L, "Alt", "Trainer", "alt@tech.ro", UserRole.FORMATOR);
        CourseRequest req = buildCourseRequest("Hack", 1L, 1L, true, "https://meet.google.com/x");
        when(authentication.getName()).thenReturn("alt@tech.ro");
        when(courseRepository.findById(1L)).thenReturn(Optional.of(activeCourse));
        when(userRepository.findByEmail("alt@tech.ro")).thenReturn(Optional.of(otherTrainer));

        assertThatThrownBy(() -> courseService.updateCourseAsTrainer(1L, req, authentication))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access Denied");
    }

    @Test
    @DisplayName("updateCourseAsTrainer - arunca exceptie daca cursul este COMPLETED")
    void updateCourseAsTrainer_throwsException_whenCourseIsCompleted() {
        Course completedCourse = buildCourse(4L, "Finalizat", CourseStatus.COMPLETED, trainer, category, 10, 20);
        CourseRequest req = buildCourseRequest("Hack", 1L, 1L, true, "https://meet.google.com/x");
        when(authentication.getName()).thenReturn("ana@tech.ro");
        when(courseRepository.findById(4L)).thenReturn(Optional.of(completedCourse));
        when(userRepository.findByEmail("ana@tech.ro")).thenReturn(Optional.of(trainer));

        assertThatThrownBy(() -> courseService.updateCourseAsTrainer(4L, req, authentication))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DRAFT or PENDING_APPROVAL");
    }

    // -------------------------------------------------------------------------
    // cancelCourseAsTrainer
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("cancelCourseAsTrainer - formator poate anula propriul curs")
    void cancelCourseAsTrainer_trainerCanCancelOwnCourse() {
        when(authentication.getName()).thenReturn("ana@tech.ro");
        when(courseRepository.findById(1L)).thenReturn(Optional.of(activeCourse));
        when(userRepository.findByEmail("ana@tech.ro")).thenReturn(Optional.of(trainer));
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        courseService.cancelCourseAsTrainer(1L, authentication);

        assertThat(activeCourse.getStatus()).isEqualTo(CourseStatus.CANCELLED);
        verify(courseRepository).save(activeCourse);
    }

    @Test
    @DisplayName("cancelCourseAsTrainer - arunca exceptie daca formatorul nu este owner")
    void cancelCourseAsTrainer_throwsException_whenNotOwner() {
        User otherTrainer = buildUser(99L, "Alt", "Trainer", "alt@tech.ro", UserRole.FORMATOR);
        when(authentication.getName()).thenReturn("alt@tech.ro");
        when(courseRepository.findById(1L)).thenReturn(Optional.of(activeCourse));
        when(userRepository.findByEmail("alt@tech.ro")).thenReturn(Optional.of(otherTrainer));

        assertThatThrownBy(() -> courseService.cancelCourseAsTrainer(1L, authentication))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access Denied");
    }

    // -------------------------------------------------------------------------
    // approveCourse
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("approveCourse - schimba statusul in ACTIVE si trimite notificare")
    void approveCourse_setsStatusActiveAndNotifiesTrainer() {
        when(courseRepository.findById(2L)).thenReturn(Optional.of(pendingCourse));
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseResponse result = courseService.approveCourse(2L);

        assertThat(result.getStatus()).isEqualTo(CourseStatus.ACTIVE);
        verify(notificationService).sendNotification(
                eq(trainer.getId()), anyString(), anyString(), any(), anyString());
    }

    @Test
    @DisplayName("approveCourse - arunca exceptie daca cursul nu exista")
    void approveCourse_throwsException_whenCourseNotFound() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.approveCourse(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // rejectCourse
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("rejectCourse - schimba statusul in DRAFT si trimite notificare cu motiv")
    void rejectCourse_setsStatusDraftAndNotifiesTrainer() {
        when(courseRepository.findById(2L)).thenReturn(Optional.of(pendingCourse));
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseResponse result = courseService.rejectCourse(2L, "Continut incomplet");

        assertThat(result.getStatus()).isEqualTo(CourseStatus.DRAFT);
        verify(notificationService).sendNotification(
                eq(trainer.getId()), anyString(), contains("Continut incomplet"), any(), anyString());
    }

    @Test
    @DisplayName("rejectCourse - foloseste motiv necunoscut daca reason este null")
    void rejectCourse_usesDefaultReason_whenReasonIsNull() {
        when(courseRepository.findById(2L)).thenReturn(Optional.of(pendingCourse));
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseResponse result = courseService.rejectCourse(2L, null);

        assertThat(result.getStatus()).isEqualTo(CourseStatus.DRAFT);
        verify(notificationService).sendNotification(
                eq(trainer.getId()), anyString(), contains("Necunoscut"), any(), anyString());
    }

    // -------------------------------------------------------------------------
    // markAsCompleted
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("markAsCompleted - schimba statusul in COMPLETED")
    void markAsCompleted_setsStatusCompleted() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(activeCourse));
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        CourseResponse result = courseService.markAsCompleted(1L);

        assertThat(result.getStatus()).isEqualTo(CourseStatus.COMPLETED);
    }

    @Test
    @DisplayName("markAsCompleted - arunca exceptie daca cursul nu exista")
    void markAsCompleted_throwsException_whenCourseNotFound() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.markAsCompleted(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // findPendingApprovalCourses
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findPendingApprovalCourses - returneaza cursurile in asteptarea aprobarii")
    void findPendingApprovalCourses_returnsPendingCourses() {
        when(courseRepository.findPendingApprovalCourses()).thenReturn(List.of(pendingCourse));

        List<CourseResponse> result = courseService.findPendingApprovalCourses();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(CourseStatus.PENDING_APPROVAL);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private CourseCategory buildCategory(Long id, String name) {
        CourseCategory cat = new CourseCategory();
        cat.setId(id); cat.setName(name); cat.setColor("#000"); cat.setActive(true);
        return cat;
    }

    private User buildUser(Long id, String first, String last, String email, UserRole role) {
        User u = new User();
        u.setId(id); u.setFirstName(first); u.setLastName(last);
        u.setEmail(email); u.setRole(role); u.setActive(true);
        return u;
    }

    private Course buildCourse(Long id, String title, CourseStatus status, User trainer, CourseCategory cat, int enrolled, int max) {
        Course c = new Course();
        c.setId(id); c.setTitle(title); c.setStatus(status);
        c.setTrainer(trainer); c.setCategory(cat);
        c.setCurrentEnrolled(enrolled); c.setMaxParticipants(max);
        c.setSessionCount(0); c.setIsOnline(true);
        c.setStartDate(LocalDate.now().plusDays(1));
        c.setEndDate(LocalDate.now().plusDays(30));
        c.setCreditHours(10); c.setThumbnailUrl("");
        c.setCreatedAt(LocalDateTime.now()); c.setUpdatedAt(LocalDateTime.now());
        return c;
    }

    private CourseRequest buildCourseRequest(String title, Long categoryId, Long trainerId, boolean isOnline, String meetingLink) {
        CourseRequest req = new CourseRequest();
        req.setTitle(title);
        req.setCategoryId(categoryId);
        req.setTrainerId(trainerId);
        req.setIsOnline(isOnline);
        req.setMeetingLink(meetingLink);
        req.setStartDate(LocalDate.now().plusDays(1));
        req.setEndDate(LocalDate.now().plusDays(30));
        req.setCreditHours(10);
        req.setMaxParticipants(20);
        req.setThumbnailUrl("");
        if (!isOnline) req.setLocation("Sala 101");
        return req;
    }
}