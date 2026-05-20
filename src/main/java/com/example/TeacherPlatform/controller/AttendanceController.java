package com.example.TeacherPlatform.controller;

import com.example.TeacherPlatform.controller.generic.GenericController;
import com.example.TeacherPlatform.dataTransferObject.AttendanceRequest;
import com.example.TeacherPlatform.dataTransferObject.AttendanceResponse;
import com.example.TeacherPlatform.model.Attendance;
import com.example.TeacherPlatform.model.enums.AttendanceStatus;
import com.example.TeacherPlatform.service.AttendanceService;
import com.example.TeacherPlatform.service.generic.GenericService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController extends GenericController<Attendance, AttendanceRequest, AttendanceResponse> {

    private final AttendanceService attendanceService;

    @Override
    protected GenericService<Attendance, AttendanceRequest, AttendanceResponse> getService() {
        return attendanceService;
    }

    @GetMapping("/session/{sessionId}")
    @PreAuthorize("hasAnyAuthority('FORMATOR', 'ADMIN')")
    public ResponseEntity<List<AttendanceResponse>> getBySession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(attendanceService.findBySessionId(sessionId));
    }

    // Ruta pentru "pills" (matricea vizuală) a profesorului
    @GetMapping("/enrollment/{enrollmentId}")
    @PreAuthorize("hasAnyAuthority('PROFESOR', 'FORMATOR', 'ADMIN')")
    public ResponseEntity<List<AttendanceResponse>> getByEnrollment(@PathVariable Long enrollmentId) {
        return ResponseEntity.ok(attendanceService.findByEnrollmentId(enrollmentId));
    }

    @PostMapping("/session/{sessionId}/save")
    @PreAuthorize("hasAnyAuthority('FORMATOR', 'ADMIN')")
    public ResponseEntity<List<AttendanceResponse>> saveBulk(
            @PathVariable Long sessionId,
            @Valid @RequestBody List<AttendanceRequest> requests) {
        return ResponseEntity.ok(attendanceService.saveBulkAttendance(sessionId, requests));
    }

    @PutMapping("/session/{sessionId}/mark-all-present")
    @PreAuthorize("hasAnyAuthority('FORMATOR', 'ADMIN')")
    public ResponseEntity<List<AttendanceResponse>> markAllPresent(@PathVariable Long sessionId) {
        return ResponseEntity.ok(attendanceService.markAll(sessionId, AttendanceStatus.PRESENT));
    }

    @PutMapping("/session/{sessionId}/mark-all-absent")
    @PreAuthorize("hasAnyAuthority('FORMATOR', 'ADMIN')")
    public ResponseEntity<List<AttendanceResponse>> markAllAbsent(@PathVariable Long sessionId) {
        return ResponseEntity.ok(attendanceService.markAll(sessionId, AttendanceStatus.ABSENT));
    }

    @GetMapping("/session/{sessionId}/stats")
    @PreAuthorize("hasAnyAuthority('FORMATOR', 'ADMIN')")
    public ResponseEntity<Map<String, Long>> getStats(@PathVariable Long sessionId) {
        return ResponseEntity.ok(attendanceService.getAttendanceStats(sessionId));
    }
}