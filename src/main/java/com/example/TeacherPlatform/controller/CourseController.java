package com.example.TeacherPlatform.controller;

import com.example.TeacherPlatform.controller.generic.GenericController;
import com.example.TeacherPlatform.dataTransferObject.CourseRequest;
import com.example.TeacherPlatform.dataTransferObject.CourseResponse;
import com.example.TeacherPlatform.model.Course;
import com.example.TeacherPlatform.model.enums.CourseStatus;
import com.example.TeacherPlatform.service.CourseService;
import com.example.TeacherPlatform.service.generic.GenericService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController extends GenericController<Course, CourseRequest, CourseResponse> {

    private final CourseService courseService;

    @Override
    protected GenericService<Course, CourseRequest, CourseResponse> getService() {
        return courseService;
    }

    // GET /api/courses — cursuri disponibile (toți)
    @Override
    public ResponseEntity<List<CourseResponse>> getAll() {
        return ResponseEntity.ok(courseService.findAvailableCourses());
    }

    // GET /api/courses/upcoming
    @GetMapping("/upcoming")
    public ResponseEntity<List<CourseResponse>> getUpcoming() {
        return ResponseEntity.ok(courseService.findUpcomingCourses());
    }

    // GET /api/courses/status/{status} (ADMIN, FORMATOR)
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FORMATOR')")
    public ResponseEntity<List<CourseResponse>> getByStatus(@PathVariable CourseStatus status) {
        return ResponseEntity.ok(courseService.findByStatus(status));
    }

    // GET /api/courses/trainer/{trainerId} (ADMIN, FORMATOR)
    @GetMapping("/trainer/{trainerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FORMATOR')")
    public ResponseEntity<List<CourseResponse>> getByTrainer(@PathVariable Long trainerId) {
        return ResponseEntity.ok(courseService.findByTrainerId(trainerId));
    }

    // GET /api/courses/category/{categoryId}
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<CourseResponse>> getByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(courseService.findByCategoryId(categoryId));
    }

    // GET /api/courses/pending-approval (ADMIN)
    @GetMapping("/pending-approval")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CourseResponse>> getPendingApproval() {
        return ResponseEntity.ok(courseService.findPendingApprovalCourses());
    }

    // PUT /api/courses/{id}/status (ADMIN)
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam CourseStatus status) {
        return ResponseEntity.ok(courseService.updateStatus(id, status));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'FORMATOR')")
    public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
        return super.create(request);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'FORMATOR')")
    public ResponseEntity<CourseResponse> update(@PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        return super.update(id, request);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return super.delete(id);
    }
}
