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

    // ----------------------------------------------------------------------------------
    // Rute Publice (Acesibile fără autentificare / tuturor)
    // ----------------------------------------------------------------------------------

    @Override
    public ResponseEntity<List<CourseResponse>> getAll() {
        return ResponseEntity.ok(courseService.findAvailableCourses());
    }

    @GetMapping("/available")
    public ResponseEntity<List<CourseResponse>> getAvailable() {
        return ResponseEntity.ok(courseService.findAvailableCourses());
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<CourseResponse>> getUpcoming() {
        return ResponseEntity.ok(courseService.findUpcomingCourses());
    }

    @GetMapping("/popular")
    public ResponseEntity<List<CourseResponse>> getPopular() {
        return ResponseEntity.ok(courseService.findPopularCourses());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<CourseResponse>> getByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(courseService.findByCategoryId(categoryId));
    }

    // ----------------------------------------------------------------------------------
    // Rute FORMATOR
    // ----------------------------------------------------------------------------------

    // FORMATORUL propune un curs nou. Acoperim metoda create() din GenericController.
    @Override
    @PostMapping
    @PreAuthorize("hasAuthority('FORMATOR')")
    public ResponseEntity<CourseResponse> create(
            @Valid @RequestBody CourseRequest request) {
        // Aceasta este doar ca să anulăm mappingul implicit. Nu o folosim.
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    // Metoda reală pentru Formator
    @PostMapping("/propose")
    @PreAuthorize("hasAuthority('FORMATOR')")
    public ResponseEntity<CourseResponse> createCourseAsTrainer(
            @Valid @RequestBody CourseRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.proposeCourse(request, authentication));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('FORMATOR')")
    public ResponseEntity<List<CourseResponse>> getMyCourses(Authentication authentication) {
        return ResponseEntity.ok(courseService.findMyCoursesAsTrainer(authentication));
    }

    // Suprascriem metoda update din GenericController ca să evite "Ambiguous mapping"
    @Override
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('FORMATOR')")
    public ResponseEntity<CourseResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequest request) {
        // Din nou, doar ascundem ruta generică, apelând metoda cu Authentication o vom face explicit
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    // Metoda reală pentru update
    @PutMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('FORMATOR')")
    public ResponseEntity<CourseResponse> updateCourseAsTrainer(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(courseService.updateCourseAsTrainer(id, request, authentication));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('FORMATOR')")
    public ResponseEntity<Void> cancelCourseAsTrainer(@PathVariable Long id, Authentication authentication) {
        courseService.cancelCourseAsTrainer(id, authentication);
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------------------------------------
    // Rute ADMIN
    // ----------------------------------------------------------------------------------

    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<CourseResponse>> getAllForAdmin() {
        return ResponseEntity.ok(courseService.findAll());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<CourseResponse>> getPendingApproval() {
        return ResponseEntity.ok(courseService.findPendingApprovalCourses());
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CourseResponse> approveCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.approveCourse(id));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CourseResponse> rejectCourse(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String reason = payload.get("reason");
        return ResponseEntity.ok(courseService.rejectCourse(id, reason));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CourseResponse> completeCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.markAsCompleted(id));
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return super.delete(id);
    }
}