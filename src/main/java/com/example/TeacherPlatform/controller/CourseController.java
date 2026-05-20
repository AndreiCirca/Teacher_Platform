package com.example.TeacherPlatform.controller;

import com.example.TeacherPlatform.controller.generic.GenericController;
import com.example.TeacherPlatform.dataTransferObject.CourseRequest;
import com.example.TeacherPlatform.dataTransferObject.CourseResponse;
import com.example.TeacherPlatform.model.Course;
import com.example.TeacherPlatform.service.CourseService;
import com.example.TeacherPlatform.service.generic.GenericService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController extends GenericController<Course, CourseRequest, CourseResponse> {

    private final CourseService courseService;

    @Override
    protected GenericService<Course, CourseRequest, CourseResponse> getService() {
        return courseService;
    }

    // Public
    @Override
    public ResponseEntity<List<CourseResponse>> getAll() {
        return ResponseEntity.ok(courseService.findUpcomingCourses());
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<CourseResponse>> getUpcoming() {
        return ResponseEntity.ok(courseService.findUpcomingCourses());
    }

    @GetMapping("/available")
    public ResponseEntity<List<CourseResponse>> getAvailable() {
        return ResponseEntity.ok(courseService.findAvailableCourses());
    }

    @GetMapping("/popular")
    public ResponseEntity<List<CourseResponse>> getPopular() {
        return ResponseEntity.ok(courseService.findPopularCourses());
    }

    @GetMapping("/by-professor")
    public ResponseEntity<List<CourseResponse>> getByProfessorEmail(@RequestParam String email) {
        return ResponseEntity.ok(courseService.findByProfesorEmail(email));
    }

    // FORMATOR
    @Override
    @PostMapping
    @PreAuthorize("hasAuthority('FORMATOR')")
    public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('FORMATOR')")
    public ResponseEntity<CourseResponse> createCourseAsTrainer(
            @Valid @RequestBody CourseRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.create(request));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('FORMATOR')")
    public ResponseEntity<List<CourseResponse>> getMyCourses(Authentication authentication) {
        return ResponseEntity.ok(courseService.findMyCoursesAsTrainer(authentication));
    }

    @Override
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('FORMATOR')")
    public ResponseEntity<CourseResponse> update(@PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @PutMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('FORMATOR')")
    public ResponseEntity<CourseResponse> updateCourseAsTrainer(
            @PathVariable Long id, @Valid @RequestBody CourseRequest request, Authentication authentication) {
        return ResponseEntity.ok(courseService.update(id, request));
    }

    @DeleteMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('FORMATOR')")
    public ResponseEntity<Void> cancelCourseAsTrainer(@PathVariable Long id, Authentication authentication) {
        courseService.cancelCourseAsTrainer(id, authentication);
        return ResponseEntity.noContent().build();
    }

    // ADMIN
    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<CourseResponse>> getAllForAdmin() {
        return ResponseEntity.ok(courseService.findAll());
    }

    @GetMapping("/pending-approval")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<CourseResponse>> getPendingApproval() {
        return ResponseEntity.ok(courseService.findPendingApprovalCourses());
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CourseResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.approveCourse(id));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CourseResponse> reject(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(courseService.rejectCourse(id, payload.get("reason")));
    }

    @PutMapping("/{id}/complete-course")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CourseResponse> markCompleted(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.markAsCompleted(id));
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return super.delete(id);
    }
}