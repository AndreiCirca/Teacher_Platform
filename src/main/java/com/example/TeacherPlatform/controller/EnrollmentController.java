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

    @Override
    @PreAuthorize("hasRole('PROFESOR')")
    public ResponseEntity<EnrollmentResponse> create(@Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(enrollmentService.create(request));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PROFESOR')")
    public ResponseEntity<List<EnrollmentResponse>> getMyEnrollments() {
        return ResponseEntity.ok(enrollmentService.getMyEnrollments());
    }

    @GetMapping("/my/active")
    @PreAuthorize("hasRole('PROFESOR')")
    public ResponseEntity<List<EnrollmentResponse>> getMyActiveEnrollments() {
        return ResponseEntity.ok(enrollmentService.getMyActiveEnrollments());
    }

    @GetMapping("/my/completed")
    @PreAuthorize("hasRole('PROFESOR')")
    public ResponseEntity<List<EnrollmentResponse>> getMyCompletedEnrollments() {
        return ResponseEntity.ok(enrollmentService.getMyCompletedEnrollments());
    }

    @GetMapping("/check/{courseId}")
    @PreAuthorize("hasRole('PROFESOR')")
    public ResponseEntity<Map<String, Boolean>> checkEnrollment(@PathVariable Long courseId) {
        return ResponseEntity.ok(
                Map.of("enrolled", enrollmentService.checkEnrollment(courseId))
        );
    }

    /**
     * Override metoda delete din GenericController
     * pentru a evita Ambiguous mapping.
     */
    @Override
    @PreAuthorize("hasRole('PROFESOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        enrollmentService.cancelEnrollment(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // FORMATOR
    // -------------------------------------------------------------------------

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasRole('FORMATOR')")
    public ResponseEntity<EnrollmentResponse> confirmEnrollment(@PathVariable Long id) {
        return ResponseEntity.ok(
                enrollmentService.confirmEnrollment(id)
        );
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('FORMATOR', 'ADMIN')")
    public ResponseEntity<EnrollmentResponse> completeEnrollment(@PathVariable Long id) {
        return ResponseEntity.ok(
                enrollmentService.completeEnrollment(id)
        );
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('FORMATOR', 'ADMIN')")
    public ResponseEntity<List<EnrollmentResponse>> getByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(
                enrollmentService.getEnrollmentsByCourse(courseId)
        );
    }

    @GetMapping("/course/{courseId}/confirmed")
    @PreAuthorize("hasAnyRole('FORMATOR', 'ADMIN')")
    public ResponseEntity<List<EnrollmentResponse>> getConfirmedByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(
                enrollmentService.getConfirmedEnrollmentsByCourse(courseId)
        );
    }

    // -------------------------------------------------------------------------
    // ADMIN
    // -------------------------------------------------------------------------

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnrollmentResponse>> getPendingEnrollments() {
        return ResponseEntity.ok(
                enrollmentService.getPendingEnrollments()
        );
    }
}