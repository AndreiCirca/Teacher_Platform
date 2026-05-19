package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.AttendanceRequest;
import com.example.TeacherPlatform.dataTransferObject.AttendanceResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.*;
import com.example.TeacherPlatform.model.enums.AttendanceStatus;
import com.example.TeacherPlatform.repository.*;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService extends GenericService<Attendance, AttendanceRequest, AttendanceResponse> {

    private final AttendanceRepository attendanceRepository;
    private final CourseSessionRepository courseSessionRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    protected BaseRepository<Attendance> getRepository() {
        return attendanceRepository;
    }

    @Override
    protected Attendance toEntity(AttendanceRequest request) {
        CourseSession session = courseSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + request.getSessionId()));

        Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + request.getEnrollmentId()));

        Attendance attendance = new Attendance();
        attendance.setSession(session);
        attendance.setEnrollment(enrollment);
        attendance.setStatus(request.getStatus() != null ? request.getStatus() : AttendanceStatus.NOT_MARKED);
        return attendance;
    }

    @Override
    protected AttendanceResponse toResponse(Attendance entity) {
        AttendanceResponse response = new AttendanceResponse();
        response.setId(entity.getId());
        response.setSessionId(entity.getSession().getId());
        response.setSessionNumber(entity.getSession().getSessionNumber());
        response.setSessionTopic(entity.getSession().getTopic());
        response.setEnrollmentId(entity.getEnrollment().getId());
        response.setTeacherId(entity.getEnrollment().getTeacher().getId());
        response.setTeacherFirstName(entity.getEnrollment().getTeacher().getFirstName());
        response.setTeacherLastName(entity.getEnrollment().getTeacher().getLastName());
        response.setStatus(entity.getStatus());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    @Override
    protected void updateEntity(Attendance entity, AttendanceRequest request) {
        entity.setStatus(request.getStatus());

        if (request.getStatus() != AttendanceStatus.NOT_MARKED) {
            CourseSession session = entity.getSession();
            session.setAttendanceMarked(true);
            courseSessionRepository.save(session);
        }
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> findBySessionId(Long sessionId) {
        return attendanceRepository.findBySessionId(sessionId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> findByEnrollmentId(Long enrollmentId) {
        return attendanceRepository.findAttendanceByEnrollmentOrdered(enrollmentId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Long countPresentSessions(Long enrollmentId) {
        return attendanceRepository.countPresentSessionsByEnrollment(enrollmentId);
    }

    @Transactional(readOnly = true)
    public Long countPresentTeachersInSession(Long sessionId) {
        return attendanceRepository.countPresentTeachersInSession(sessionId);
    }

    /**
     * COREMANDE COREIATE: Salvare bulk a catalogului (POST /api/sessions/{id}/attendance/save)
     */
    @Transactional
    public List<AttendanceResponse> saveBulkAttendance(Long sessionId, List<AttendanceRequest> requests) {
        CourseSession session = courseSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));

        List<Attendance> savedList = requests.stream().map(req -> {
            Attendance attendance = attendanceRepository.findBySessionIdAndEnrollmentId(sessionId, req.getEnrollmentId())
                    .orElseGet(() -> {
                        Attendance newAttendance = new Attendance();
                        newAttendance.setSession(session);
                        newAttendance.setEnrollment(enrollmentRepository.findById(req.getEnrollmentId())
                                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found: " + req.getEnrollmentId())));
                        return newAttendance;
                    });
            attendance.setStatus(req.getStatus());
            return attendanceRepository.save(attendance);
        }).toList();

        session.setAttendanceMarked(true);
        courseSessionRepository.save(session);

        return savedList.stream().map(this::toResponse).toList();
    }

    /**
     * REZOLVARE EROARE image_75f549.png:
     * Folosește funcția existentă din EnrollmentRepository: findConfirmedEnrollmentsByCourse
     */
    @Transactional
    public List<AttendanceResponse> markAllPresent(Long sessionId) {
        CourseSession session = courseSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));

        // Corecție directă: apelăm metoda corectă din repository-ul tău!
        List<Enrollment> enrollments = enrollmentRepository.findConfirmedEnrollmentsByCourse(session.getCourse().getId());

        List<Attendance> attendances = enrollments.stream().map(enrollment -> {
            Attendance attendance = attendanceRepository.findBySessionIdAndEnrollmentId(sessionId, enrollment.getId())
                    .orElseGet(() -> {
                        Attendance newAttendance = new Attendance();
                        newAttendance.setSession(session);
                        newAttendance.setEnrollment(enrollment);
                        return newAttendance;
                    });
            attendance.setStatus(AttendanceStatus.PRESENT);
            return attendanceRepository.save(attendance);
        }).toList();

        session.setAttendanceMarked(true);
        courseSessionRepository.save(session);

        return attendances.stream().map(this::toResponse).toList();
    }
}