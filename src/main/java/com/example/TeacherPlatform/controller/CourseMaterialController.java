package com.example.TeacherPlatform.controller;

import com.example.TeacherPlatform.controller.generic.GenericController;
import com.example.TeacherPlatform.dataTransferObject.CourseMaterialRequest;
import com.example.TeacherPlatform.dataTransferObject.CourseMaterialResponse;
import com.example.TeacherPlatform.model.CourseMaterial;
import com.example.TeacherPlatform.service.CourseMaterialService;
import com.example.TeacherPlatform.service.generic.GenericService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class CourseMaterialController extends GenericController<CourseMaterial, CourseMaterialRequest, CourseMaterialResponse> {

    private final CourseMaterialService courseMaterialService;

    @Override
    protected GenericService<CourseMaterial, CourseMaterialRequest, CourseMaterialResponse> getService() {
        return courseMaterialService;
    }

    // GET /api/materials/course/{courseId} — Returnează materialele doar dacă profesorul este înscris confirmat
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyAuthority('PROFESOR', 'FORMATOR', 'ADMIN')")
    public ResponseEntity<List<CourseMaterialResponse>> getMaterialsByCourse(
            @PathVariable Long courseId,
            Authentication authentication) {

        return ResponseEntity.ok(courseMaterialService.findByCourseId(courseId, authentication));
    }

    // PUT /api/materials/{id}/download — Incrementează downloadCount securizat
    @PutMapping("/{id}/download")
    @PreAuthorize("hasAnyAuthority('PROFESOR', 'FORMATOR', 'ADMIN')")
    public ResponseEntity<CourseMaterialResponse> trackDownload(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(courseMaterialService.incrementDownloadCount(id, authentication));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('FORMATOR', 'ADMIN')")
    public ResponseEntity<CourseMaterialResponse> create(@Valid @RequestBody CourseMaterialRequest request) {
        return super.create(request);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('FORMATOR', 'ADMIN')")
    public ResponseEntity<CourseMaterialResponse> update(@PathVariable Long id, @Valid @RequestBody CourseMaterialRequest request) {
        return super.update(id, request);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('FORMATOR', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return super.delete(id);
    }
}