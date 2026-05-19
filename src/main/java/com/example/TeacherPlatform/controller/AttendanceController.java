package com.example.TeacherPlatform.controller;

import com.example.TeacherPlatform.controller.generic.GenericController;
import com.example.TeacherPlatform.dataTransferObject.AttendanceRequest;
import com.example.TeacherPlatform.dataTransferObject.AttendanceResponse;
import com.example.TeacherPlatform.model.Attendance;
import com.example.TeacherPlatform.service.AttendanceService;
import com.example.TeacherPlatform.service.generic.GenericService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController extends GenericController<Attendance, AttendanceRequest, AttendanceResponse> {

    private final AttendanceService attendanceService;

    @Override
    protected GenericService<Attendance, AttendanceRequest, AttendanceResponse> getService() {
        return attendanceService;
    }

    // GET /api/attendance/session/{sessionId} (ADMIN, FORMATOR)
    @GetMapping("/session/{sessionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FORMATOR')")
    public ResponseEntity<List<AttendanceResponse>> getBySession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(attendanceService.findBySessionId(sessionId));
    }

    // GET /api/attendance/enrollment/{enrollmentId}
    @GetMapping("/enrollment/{enrollmentId}")
    public ResponseEntity<List<AttendanceResponse>> getByEnrollment(@PathVariable Long enrollmentId) {
        return ResponseEntity.ok(attendanceService.findByEnrollmentId(enrollmentId));
    }

    // GET /api/attendance/enrollment/{enrollmentId}/count-present
    @GetMapping("/enrollment/{enrollmentId}/count-present")
    public ResponseEntity<Long> countPresent(@PathVariable Long enrollmentId) {
        return ResponseEntity.ok(attendanceService.countPresentSessions(enrollmentId));
    }

    // GET /api/attendance/session/{sessionId}/count-present
    @GetMapping("/session/{sessionId}/count-present")
    @PreAuthorize("hasAnyRole('ADMIN', 'FORMATOR')")
    public ResponseEntity<Long> countPresentInSession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(attendanceService.countPresentTeachersInSession(sessionId));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'FORMATOR')")
    public ResponseEntity<AttendanceResponse> create(@Valid @RequestBody AttendanceRequest request) {
        return super.create(request);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'FORMATOR')")
    public ResponseEntity<AttendanceResponse> update(@PathVariable Long id, @Valid @RequestBody AttendanceRequest request) {
        return super.update(id, request);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return super.delete(id);
    }
}
