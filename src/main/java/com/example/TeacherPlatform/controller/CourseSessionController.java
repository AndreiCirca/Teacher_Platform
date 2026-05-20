package com.example.TeacherPlatform.controller;

import com.example.TeacherPlatform.controller.generic.GenericController;
import com.example.TeacherPlatform.dataTransferObject.CourseSessionRequest;
import com.example.TeacherPlatform.dataTransferObject.CourseSessionResponse;
import com.example.TeacherPlatform.model.CourseSession;
import com.example.TeacherPlatform.service.CourseSessionService;
import com.example.TeacherPlatform.service.generic.GenericService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyAuthority('PROFESOR', 'FORMATOR', 'ADMIN')")
    public ResponseEntity<List<CourseSessionResponse>> getSessionsByCourse(
            @PathVariable Long courseId, Authentication authentication) {
        return ResponseEntity.ok(courseSessionService.findByCourseId(courseId, authentication));
    }

    @GetMapping("/unmarked")
    @PreAuthorize("hasAuthority('FORMATOR')")
    public ResponseEntity<List<CourseSessionResponse>> getUnmarkedAttendance() {
        return ResponseEntity.ok(courseSessionService.findUnmarkedAttendanceSessions());
    }

    @GetMapping("/time-range")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'FORMATOR')")
    public ResponseEntity<List<CourseSessionResponse>> getByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(courseSessionService.findSessionsByTimeRange(from, to));
    }

    @Override
    @PreAuthorize("hasAuthority('FORMATOR')")
    public ResponseEntity<CourseSessionResponse> create(@Valid @RequestBody CourseSessionRequest request) {
        return super.create(request);
    }

    @Override
    @PreAuthorize("hasAuthority('FORMATOR')")
    public ResponseEntity<CourseSessionResponse> update(@PathVariable Long id, @Valid @RequestBody CourseSessionRequest request) {
        return super.update(id, request);
    }

    @Override
    @PreAuthorize("hasAuthority('FORMATOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return super.delete(id);
    }
}