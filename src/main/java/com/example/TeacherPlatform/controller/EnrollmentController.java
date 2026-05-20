package com.example.TeacherPlatform.controller;

import com.example.TeacherPlatform.controller.generic.GenericController;
import com.example.TeacherPlatform.dataTransferObject.EnrollmentRequest;
import com.example.TeacherPlatform.dataTransferObject.EnrollmentResponse;
import com.example.TeacherPlatform.model.Enrollment;
import com.example.TeacherPlatform.service.EnrollmentService;
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
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController extends GenericController<Enrollment, EnrollmentRequest, EnrollmentResponse> {

    private final EnrollmentService enrollmentService;

    @Override
    protected GenericService<Enrollment, EnrollmentRequest, EnrollmentResponse> getService() {
        return enrollmentService;
    }

    // -------------------------------------------------------------------------
    // PROFESOR
    // -------------------------------------------------------------------------

    @PostMapping("/secure")
    @PreAuthorize("hasAuthority('PROFESOR')")
    public ResponseEntity<EnrollmentResponse> createEnrollment(
            @Valid @RequestBody EnrollmentRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.createEnrollment(request, authentication));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('PROFESOR')")
    public ResponseEntity<List<EnrollmentResponse>> getMyEnrollments(Authentication authentication) {
        return ResponseEntity.ok(enrollmentService.getMyEnrollments(authentication));
    }

    @GetMapping("/my/active")
    @PreAuthorize("hasAuthority('PROFESOR')")
    public ResponseEntity<List<EnrollmentResponse>> getMyActiveEnrollments(Authentication authentication) {
        return ResponseEntity.ok(enrollmentService.getMyActiveEnrollments(authentication));
    }

    @GetMapping("/my/completed")
    @PreAuthorize("hasAuthority('PROFESOR')")
    public ResponseEntity<List<EnrollmentResponse>> getMyCompletedEnrollments(Authentication authentication) {
        return ResponseEntity.ok(enrollmentService.getMyCompletedEnrollments(authentication));
    }

    @GetMapping("/check/{courseId}")
    @PreAuthorize("hasAuthority('PROFESOR')")
    public ResponseEntity<Map<String, Boolean>> checkEnrollment(@PathVariable Long courseId, Authentication authentication) {
        return ResponseEntity.ok(Map.of("enrolled", enrollmentService.checkEnrollment(courseId, authentication)));
    }

    @DeleteMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('PROFESOR')")
    public ResponseEntity<Void> cancelEnrollment(@PathVariable Long id, Authentication authentication) {
        enrollmentService.cancelEnrollment(id, authentication);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // FORMATOR
    // -------------------------------------------------------------------------

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyAuthority('FORMATOR', 'ADMIN')")
    public ResponseEntity<EnrollmentResponse> confirmEnrollment(@PathVariable Long id) {
        return ResponseEntity.ok(enrollmentService.confirmEnrollment(id));
    }

    @PutMapping("/{id}/cancel-participant")
    @PreAuthorize("hasAuthority('FORMATOR')")
    public ResponseEntity<Void> cancelParticipant(@PathVariable Long id, Authentication authentication) {
        enrollmentService.cancelEnrollmentAsTrainer(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/course/{courseId}/stats")
    @PreAuthorize("hasAnyAuthority('FORMATOR', 'ADMIN')")
    public ResponseEntity<Map<String, Long>> getCourseStats(@PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentService.getCourseEnrollmentStats(courseId));
    }

    // -------------------------------------------------------------------------
    // ADMIN / COMUN
    // -------------------------------------------------------------------------

    @GetMapping("/admin/this-month")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<EnrollmentResponse>> getEnrollmentsThisMonth() {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsThisMonth());
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyAuthority('FORMATOR', 'ADMIN')")
    public ResponseEntity<EnrollmentResponse> completeEnrollment(@PathVariable Long id) {
        return ResponseEntity.ok(enrollmentService.completeEnrollment(id));
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyAuthority('FORMATOR', 'ADMIN')")
    public ResponseEntity<List<EnrollmentResponse>> getByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByCourse(courseId));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'FORMATOR')")
    public ResponseEntity<List<EnrollmentResponse>> getPendingEnrollments() {
        return ResponseEntity.ok(enrollmentService.getPendingEnrollments());
    }

    // Invalidare metode generice
    @Override
    public ResponseEntity<EnrollmentResponse> create(@Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @Override
    public ResponseEntity<EnrollmentResponse> update(@PathVariable Long id, @Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return super.delete(id);
    }
}