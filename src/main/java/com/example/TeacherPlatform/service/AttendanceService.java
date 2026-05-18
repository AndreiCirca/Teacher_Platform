package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.AttendanceRequest;
import com.example.TeacherPlatform.dataTransferObject.AttendanceResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.Attendance;
import com.example.TeacherPlatform.model.CourseSession;
import com.example.TeacherPlatform.model.Enrollment;
import com.example.TeacherPlatform.repository.AttendanceRepository;
import com.example.TeacherPlatform.repository.BaseRepository;
import com.example.TeacherPlatform.repository.CourseSessionRepository;
import com.example.TeacherPlatform.repository.EnrollmentRepository;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttendanceService extends GenericService<Attendance, AttendanceRequest, AttendanceResponse> {

    private final AttendanceRepository attendanceRepository;
    private final CourseSessionRepository sessionRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    protected BaseRepository<Attendance> getRepository() {
        return attendanceRepository;
    }

    @Override
    protected Attendance toEntity(AttendanceRequest request) {
        Attendance attendance = new Attendance();
        mapFields(attendance, request);
        return attendance;
    }

    @Override
    protected AttendanceResponse toResponse(Attendance entity) {
        AttendanceResponse response = new AttendanceResponse();
        response.setId(entity.getId());

        if (entity.getSession() != null) {
            response.setSessionId(entity.getSession().getId());
            response.setSessionTopic(entity.getSession().getTopic());
        }

        if (entity.getEnrollment() != null && entity.getEnrollment().getTeacher() != null) {
            response.setEnrollmentId(entity.getEnrollment().getId());
            response.setTeacherFullName(entity.getEnrollment().getTeacher().getFullName());
        }

        response.setStatus(entity.getStatus());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    @Override
    protected void updateEntity(Attendance entity, AttendanceRequest request) {
        mapFields(entity, request);
    }

    private void mapFields(Attendance entity, AttendanceRequest request) {
        entity.setStatus(request.getStatus());

        CourseSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Course session not found with id: " + request.getSessionId()));
        entity.setSession(session);

        Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + request.getEnrollmentId()));
        entity.setEnrollment(enrollment);
    }
}