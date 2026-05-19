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
@RequestMapping("/api/attendances")
@RequiredArgsConstructor
public class AttendanceController extends GenericController<Attendance, AttendanceRequest, AttendanceResponse> {

    private final AttendanceService attendanceService;

    @Override
    protected GenericService<Attendance, AttendanceRequest, AttendanceResponse> getService() {
        return attendanceService;
    }

    // GET /api/attendance/session/{sessionId} (ADMIN, FORMATOR)
    @GetMapping("/attendance/session/{sessionId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'FORMATOR')")
    public ResponseEntity<List<AttendanceResponse>> getBySession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(attendanceService.findBySessionId(sessionId));
    }

    /**
     * GĂURI SPECIFICAȚII - RUTA DE UI:
     * GET /api/enrollments/{id}/attendance/pills
     * Returnează matricea vizuală de prezență (cercul verde/roșu per sesiune) din contul prof.
     */
    @GetMapping("/enrollments/{enrollmentId}/attendance/pills")
    @PreAuthorize("hasAnyAuthority('PROFESOR', 'FORMATOR', 'ADMIN')")
    public ResponseEntity<List<AttendanceResponse>> getAttendancePills(@PathVariable Long enrollmentId) {
        return ResponseEntity.ok(attendanceService.findByEnrollmentId(enrollmentId));
    }

    // POST /api/sessions/{id}/attendance/save
    @PostMapping("/sessions/{sessionId}/attendance/save")
    @PreAuthorize("hasAnyAuthority('FORMATOR', 'ADMIN')")
    public ResponseEntity<List<AttendanceResponse>> saveBulk(@PathVariable Long sessionId, @Valid @RequestBody List<AttendanceRequest> requests) {
        return ResponseEntity.ok(attendanceService.saveBulkAttendance(sessionId, requests));
    }

    // PUT /api/sessions/{id}/attendance/mark-all-present
    @PutMapping("/sessions/{sessionId}/attendance/mark-all-present")
    @PreAuthorize("hasAnyAuthority('FORMATOR', 'ADMIN')")
    public ResponseEntity<List<AttendanceResponse>> markAllPresent(@PathVariable Long sessionId) {
        return ResponseEntity.ok(attendanceService.markAllPresent(sessionId));
    }

    @GetMapping("/attendance/enrollment/{enrollmentId}/count-present")
    @PreAuthorize("hasAnyAuthority('PROFESOR', 'FORMATOR', 'ADMIN')")
    public ResponseEntity<Long> countPresent(@PathVariable Long enrollmentId) {
        return ResponseEntity.ok(attendanceService.countPresentSessions(enrollmentId));
    }

    @GetMapping("/attendance/session/{sessionId}/count-present")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'FORMATOR')")
    public ResponseEntity<Long> countPresentInSession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(attendanceService.countPresentTeachersInSession(sessionId));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ADMIN', 'FORMATOR')")
    public ResponseEntity<AttendanceResponse> create(@Valid @RequestBody AttendanceRequest request) {
        return super.create(request);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ADMIN', 'FORMATOR')")
    public ResponseEntity<AttendanceResponse> update(@PathVariable Long id, @Valid @RequestBody AttendanceRequest request) {
        return super.update(id, request);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return super.delete(id);
    }
}