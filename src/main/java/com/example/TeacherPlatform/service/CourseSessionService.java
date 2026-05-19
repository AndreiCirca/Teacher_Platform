package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.CourseSessionRequest;
import com.example.TeacherPlatform.dataTransferObject.CourseSessionResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.Course;
import com.example.TeacherPlatform.model.CourseSession;
import com.example.TeacherPlatform.repository.BaseRepository;
import com.example.TeacherPlatform.repository.CourseRepository;
import com.example.TeacherPlatform.repository.CourseSessionRepository;
import com.example.TeacherPlatform.repository.EnrollmentRepository;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseSessionService extends GenericService<CourseSession, CourseSessionRequest, CourseSessionResponse> {

    private final CourseSessionRepository courseSessionRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    protected BaseRepository<CourseSession> getRepository() {
        return courseSessionRepository;
    }

    @Override
    @Transactional
    protected CourseSession toEntity(CourseSessionRequest request) {
        CourseSession session = new CourseSession();
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + request.getCourseId()));

        // CORECTAT: Validare suprapunere orară formator la CREARE (ID-ul sesiunii curente este null)
        validateTrainerAvailability(course.getTrainer().getId(), request.getStartTime(), request.getEndTime(), null);

        session.setCourse(course);
        session.setTopic(request.getTopic());
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());
        session.setMeetingLink(request.getMeetingLink());
        session.setSessionNumber(request.getSessionNumber());
        session.setAttendanceMarked(false);

        // CORECTAT: Sincronizăm sessionCount în Course pentru ca algoritmul bulk din CertificateService să funcționeze corect
        int currentCount = course.getSessionCount() != null ? course.getSessionCount() : 0;
        course.setSessionCount(currentCount + 1);
        courseRepository.save(course);

        return session;
    }

    @Override
    @Transactional
    protected void updateEntity(CourseSession entity, CourseSessionRequest request) {
        // CORECTAT: Validăm disponibilitatea și la UPDATE, transmițând entity.getId() pentru a fi ignorat din verificare
        if (entity.getCourse() != null && entity.getCourse().getTrainer() != null) {
            validateTrainerAvailability(entity.getCourse().getTrainer().getId(), request.getStartTime(), request.getEndTime(), entity.getId());
        }

        entity.setTopic(request.getTopic());
        entity.setStartTime(request.getStartTime());
        entity.setEndTime(request.getEndTime());
        entity.setMeetingLink(request.getMeetingLink());
        entity.setSessionNumber(request.getSessionNumber());
    }

    @Override
    protected CourseSessionResponse toResponse(CourseSession entity) {
        CourseSessionResponse response = new CourseSessionResponse();
        response.setId(entity.getId());

        if (entity.getCourse() != null) {
            response.setCourseId(entity.getCourse().getId());
            response.setCourseTitle(entity.getCourse().getTitle());
        }

        response.setTopic(entity.getTopic());
        response.setStartTime(entity.getStartTime());
        response.setEndTime(entity.getEndTime());
        response.setMeetingLink(entity.getMeetingLink());
        response.setSessionNumber(entity.getSessionNumber());
        response.setAttendanceMarked(entity.getAttendanceMarked());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    @Transactional(readOnly = true)
    public List<CourseSessionResponse> findByCourseId(Long courseId, Authentication authentication) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }

        String username = authentication.getName();
        boolean isTeacher = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> "PROFESOR".equals(auth));

        // Securitate la nivel de rând: Profesorii văd sesiunile doar dacă sunt înscriși confirmat
        if (isTeacher) {
            boolean hasAccess = enrollmentRepository.hasConfirmedOrCompletedEnrollment(courseId, username);
            if (!hasAccess) {
                throw new RuntimeException("Access denied. You must have a confirmed enrollment to view course sessions.");
            }
        }

        return courseSessionRepository.findByCourseIdOrdered(courseId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseSessionResponse> findSessionsByTimeRange(LocalDateTime from, LocalDateTime to) {
        return courseSessionRepository.findSessionsByTimeRange(from, to)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseSessionResponse> findUnmarkedAttendanceSessions() {
        return courseSessionRepository.findUnmarkedAttendanceSessions()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * METODĂ PRIVATĂ HELPER: Validează dacă formatorul este liber în intervalul selectat.
     * Dacă se face update, se trimite currentSessionId pentru ca sesiunea curentă să fie exclusă (să nu se autoblocheze).
     */
    private void validateTrainerAvailability(Long trainerId, LocalDateTime start, LocalDateTime end, Long currentSessionId) {
        boolean hasOverlap = courseSessionRepository.findAll().stream()
                .filter(s -> s.getCourse() != null && s.getCourse().getTrainer() != null
                        && s.getCourse().getTrainer().getId().equals(trainerId))
                .filter(s -> currentSessionId == null || !s.getId().equals(currentSessionId)) // exclude sesiunea curentă la editare
                .anyMatch(s -> start.isBefore(s.getEndTime()) && end.isAfter(s.getStartTime()));

        if (hasOverlap) {
            throw new RuntimeException("Formatorul are deja programată o altă sesiune de curs în acest interval orar!");
        }
    }
}