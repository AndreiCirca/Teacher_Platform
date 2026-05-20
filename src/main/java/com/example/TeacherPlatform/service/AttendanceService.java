package com.example.TeacherPlatform.service;

import com.example.TeacherPlatform.dataTransferObject.AttendanceRequest;
import com.example.TeacherPlatform.dataTransferObject.AttendanceResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.Attendance;
import com.example.TeacherPlatform.model.CourseSession;
import com.example.TeacherPlatform.model.Enrollment;
import com.example.TeacherPlatform.model.enums.AttendanceStatus;
import com.example.TeacherPlatform.repository.AttendanceRepository;
import com.example.TeacherPlatform.repository.BaseRepository;
import com.example.TeacherPlatform.repository.CourseSessionRepository;
import com.example.TeacherPlatform.repository.EnrollmentRepository;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

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
        Attendance attendance = new Attendance();
        CourseSession session = courseSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Sesiunea nu a fost găsită"));
        Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Înscrierea nu a fost găsită"));
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
        if (entity.getEnrollment().getTeacher() != null) {
            response.setTeacherId(entity.getEnrollment().getTeacher().getId());
            response.setTeacherFirstName(entity.getEnrollment().getTeacher().getFirstName());
            response.setTeacherLastName(entity.getEnrollment().getTeacher().getLastName());
        }
        response.setStatus(entity.getStatus());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    @Override
    protected void updateEntity(Attendance entity, AttendanceRequest request) {
        entity.setStatus(request.getStatus());
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

    @Transactional
    public List<AttendanceResponse> saveBulkAttendance(Long sessionId, List<AttendanceRequest> requests) {
        CourseSession session = courseSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sesiunea nu a fost găsită"));

        List<Attendance> saved = requests.stream().map(req -> {
            Attendance att = attendanceRepository.findBySessionIdAndEnrollmentId(sessionId, req.getEnrollmentId())
                    .orElseGet(() -> {
                        Attendance newAtt = new Attendance();
                        newAtt.setSession(session);
                        newAtt.setEnrollment(enrollmentRepository.findById(req.getEnrollmentId())
                                .orElseThrow(() -> new ResourceNotFoundException("Înscriere inexistentă")));
                        return newAtt;
                    });
            att.setStatus(req.getStatus());
            return attendanceRepository.save(att);
        }).toList();

        session.setAttendanceMarked(true);
        courseSessionRepository.save(session);
        return saved.stream().map(this::toResponse).toList();
    }

    @Transactional
    public List<AttendanceResponse> markAll(Long sessionId, AttendanceStatus status) {
        CourseSession session = courseSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sesiunea nu a fost găsită"));

        List<Enrollment> enrollments = enrollmentRepository.findConfirmedEnrollmentsByCourse(session.getCourse().getId());

        List<Attendance> attendances = enrollments.stream().map(e -> {
            Attendance att = attendanceRepository.findBySessionIdAndEnrollmentId(sessionId, e.getId())
                    .orElseGet(() -> new Attendance(session, e, status));
            att.setStatus(status);
            return attendanceRepository.save(att);
        }).toList();

        session.setAttendanceMarked(true);
        courseSessionRepository.save(session);
        return attendances.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getAttendanceStats(Long sessionId) {
        return Map.of(
                "present", attendanceRepository.countPresentTeachersInSession(sessionId, AttendanceStatus.PRESENT),
                "absent", attendanceRepository.countAbsentTeachersInSession(sessionId, AttendanceStatus.ABSENT)
        );
    }
}