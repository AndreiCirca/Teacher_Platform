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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    @Override
    public ResponseEntity<List<CourseResponse>> getAll() {
        return ResponseEntity.ok(courseService.findAvailableCourses());
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<CourseResponse>> getUpcoming() {
        return ResponseEntity.ok(courseService.findUpcomingCourses(LocalDate.now()));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'FORMATOR')")
    public ResponseEntity<List<CourseResponse>> getByStatus(@PathVariable CourseStatus status) {
        return ResponseEntity.ok(courseService.findByStatus(status));
    }

    @GetMapping("/trainer/{trainerId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'FORMATOR')")
    public ResponseEntity<List<CourseResponse>> getByTrainer(@PathVariable Long trainerId) {
        return ResponseEntity.ok(courseService.findByTrainerId(trainerId));
    }

    @GetMapping("/category/{categoryId}")
    @PreAuthorize("hasAnyAuthority('PROFESOR', 'FORMATOR', 'ADMIN')")
    public ResponseEntity<List<CourseResponse>> getByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(courseService.findByCategoryId(categoryId));
    }

    @GetMapping("/pending-approval")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<List<CourseResponse>> getPendingApproval() {
        return ResponseEntity.ok(courseService.findPendingApprovalCourses());
    }

    // Aprobare Curs din Dashboard Admin
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CourseResponse> approveCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.approveCourse(id));
    }

    // Respingere Curs (Îl trimite înapoi în DRAFT fără câmp de text)
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CourseResponse> rejectCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.rejectCourse(id));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<CourseResponse> updateStatus(@PathVariable Long id, @RequestParam CourseStatus status) {
        return ResponseEntity.ok(courseService.updateStatus(id, status));
    }

    @PostMapping("/secure")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'FORMATOR')")
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.createCourseSecure(request, authentication));
    }

    @PutMapping("/secure/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'FORMATOR')")
    public ResponseEntity<CourseResponse> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseRequest request, Authentication authentication) {
        return ResponseEntity.ok(courseService.updateCourseSecure(id, request, authentication));
    }

    @DeleteMapping("/secure/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'FORMATOR')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id, Authentication authentication) {
        courseService.deleteCourseSecure(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<CourseResponse> update(@PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }
}