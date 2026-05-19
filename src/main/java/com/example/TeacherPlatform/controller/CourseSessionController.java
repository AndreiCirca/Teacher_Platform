package com.example.TeacherPlatform.controller;

import com.example.TeacherPlatform.controller.generic.GenericController;
import com.example.TeacherPlatform.dataTransferObject.CourseSessionRequest;
import com.example.TeacherPlatform.dataTransferObject.CourseSessionResponse;
import com.example.TeacherPlatform.model.CourseSession;
import com.example.TeacherPlatform.service.CourseSessionService;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class CourseSessionController extends GenericController<CourseSession, CourseSessionRequest, CourseSessionResponse> {

    private final CourseSessionService courseSessionService;

    @Override
    protected GenericService<CourseSession, CourseSessionRequest, CourseSessionResponse> getService() {
        return courseSessionService;
    }

    // GET /api/sessions/course/{courseId} — sesiunile unui curs (toți autentificați)
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<CourseSessionResponse>> getByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(courseSessionService.findByCourseId(courseId));
    }

    // GET /api/sessions/time-range?from=...&to=... (ADMIN, FORMATOR)
    @GetMapping("/time-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'FORMATOR')")
    public ResponseEntity<List<CourseSessionResponse>> getByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(courseSessionService.findSessionsByTimeRange(from, to));
    }

    // GET /api/sessions/unmarked-attendance (ADMIN, FORMATOR)
    @GetMapping("/unmarked-attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'FORMATOR')")
    public ResponseEntity<List<CourseSessionResponse>> getUnmarkedAttendance() {
        return ResponseEntity.ok(courseSessionService.findUnmarkedAttendanceSessions());
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'FORMATOR')")
    public ResponseEntity<CourseSessionResponse> create(
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody CourseSessionRequest request) {
        return super.create(request);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'FORMATOR')")
    public ResponseEntity<CourseSessionResponse> update(
            @PathVariable Long id,
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody CourseSessionRequest request) {
        return super.update(id, request);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'FORMATOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return super.delete(id);
    }
}
 