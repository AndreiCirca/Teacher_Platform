package com.example.TeacherPlatform.service.crudtests;

import com.example.TeacherPlatform.dataTransferObject.CourseSessionRequest;
import com.example.TeacherPlatform.dataTransferObject.CourseSessionResponse;
import com.example.TeacherPlatform.model.Course;
import com.example.TeacherPlatform.model.CourseSession;
import com.example.TeacherPlatform.model.User;
import com.example.TeacherPlatform.repository.CourseRepository;
import com.example.TeacherPlatform.repository.CourseSessionRepository;
import com.example.TeacherPlatform.service.CourseSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseSessionService - Business Logic Tests")
class CourseSessionServiceCrudTest {

    @Mock private CourseSessionRepository sessionRepository;
    @Mock private CourseRepository courseRepository;

    @InjectMocks
    private CourseSessionService sessionService;

    private Course course;
    private User trainer;
    private CourseSession session;
    private CourseSessionRequest request;

    @BeforeEach
    void setUp() {
        trainer = new User();
        trainer.setId(1L);

        course = new Course();
        course.setId(1L);
        course.setTitle("Java Basics");
        course.setTrainer(trainer);
        course.setSessionCount(2);

        session = new CourseSession();
        session.setId(1L);
        session.setCourse(course);
        session.setStartTime(LocalDateTime.of(2026, 10, 1, 10, 0));
        session.setEndTime(LocalDateTime.of(2026, 10, 1, 12, 0));
        session.setAttendanceMarked(false);

        request = new CourseSessionRequest();
        request.setCourseId(1L);
        request.setTopic("Introducere");
        request.setStartTime(LocalDateTime.of(2026, 10, 2, 10, 0));
        request.setEndTime(LocalDateTime.of(2026, 10, 2, 12, 0));
    }

    @Test
    @DisplayName("Create - Adaugă sesiune cu succes și incrementează contorul de sesiuni al cursului")
    void create_createsSessionAndIncrementsCourseCount() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(sessionRepository.findAll()).thenReturn(List.of()); // Nu există suprapuneri
        when(sessionRepository.save(any(CourseSession.class))).thenAnswer(i -> {
            CourseSession s = i.getArgument(0);
            s.setId(10L);
            return s;
        });

        CourseSessionResponse response = sessionService.create(request);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(course.getSessionCount()).isEqualTo(3); // A crescut de la 2 la 3
        verify(courseRepository).save(course);
    }

    @Test
    @DisplayName("Create - Aruncă excepție dacă formatorul are altă sesiune în acel interval (Overlapping)")
    void create_throwsException_whenTrainerHasOverlap() {
        CourseSession overlappingSession = new CourseSession();
        overlappingSession.setId(99L);
        overlappingSession.setCourse(course); // Același curs/trainer
        // Se suprapune parțial (11:00 - 13:00) cu request-ul (10:00 - 12:00)
        overlappingSession.setStartTime(LocalDateTime.of(2026, 10, 2, 11, 0));
        overlappingSession.setEndTime(LocalDateTime.of(2026, 10, 2, 13, 0));

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(sessionRepository.findAll()).thenReturn(List.of(overlappingSession));

        assertThatThrownBy(() -> sessionService.create(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Formatorul are deja programată o altă sesiune");
    }

    @Test
    @DisplayName("Delete - Șterge sesiunea și decrementează contorul cursului")
    void delete_deletesSuccessfully_andDecrementsCourseCount() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        sessionService.delete(1L);

        assertThat(course.getSessionCount()).isEqualTo(1); // Scade de la 2 la 1
        verify(sessionRepository).delete(session);
        verify(courseRepository).save(course);
    }

    @Test
    @DisplayName("Delete - Aruncă excepție dacă prezența a fost deja marcată")
    void delete_throwsException_whenAttendanceMarked() {
        session.setAttendanceMarked(true); // Gata, catalogul e completat
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> sessionService.delete(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot delete a session where attendance has already been marked");

        verify(sessionRepository, never()).delete(any());
    }
}