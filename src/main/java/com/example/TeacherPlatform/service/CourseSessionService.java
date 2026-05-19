package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.CourseSessionRequest;
import com.example.TeacherPlatform.dataTransferObject.CourseSessionResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.Course;
import com.example.TeacherPlatform.model.CourseSession;
import com.example.TeacherPlatform.repository.BaseRepository;
import com.example.TeacherPlatform.repository.CourseRepository;
import com.example.TeacherPlatform.repository.CourseSessionRepository;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseSessionService extends GenericService<CourseSession, CourseSessionRequest, CourseSessionResponse> {

    private final CourseSessionRepository courseSessionRepository;
    private final CourseRepository courseRepository;

    @Override
    protected BaseRepository<CourseSession> getRepository() {
        return courseSessionRepository;
    }

    @Override
    protected CourseSession toEntity(CourseSessionRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + request.getCourseId()));

        CourseSession session = new CourseSession();
        session.setCourse(course);
        session.setTopic(request.getTopic());
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());
        session.setMeetingLink(request.getMeetingLink());
        session.setSessionNumber(request.getSessionNumber());
        session.setAttendanceMarked(false);
        return session;
    }

    @Override
    protected CourseSessionResponse toResponse(CourseSession entity) {
        CourseSessionResponse response = new CourseSessionResponse();
        response.setId(entity.getId());
        response.setCourseId(entity.getCourse().getId());
        response.setCourseTitle(entity.getCourse().getTitle());
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

    @Override
    protected void updateEntity(CourseSession entity, CourseSessionRequest request) {
        entity.setTopic(request.getTopic());
        entity.setStartTime(request.getStartTime());
        entity.setEndTime(request.getEndTime());
        entity.setMeetingLink(request.getMeetingLink());
        entity.setSessionNumber(request.getSessionNumber());
    }

    @Transactional(readOnly = true)
    public List<CourseSessionResponse> findByCourseId(Long courseId) {
        return courseSessionRepository.findCourseSessionsOrdered(courseId)
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
}