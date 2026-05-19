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

    // GET /api/materials/course/{courseId} — returnează materialele unui curs specfic (Profesori înscriși, Formatori, Admini)
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('PROFESOR', 'FORMATOR', 'ADMIN')")
    public ResponseEntity<List<CourseMaterialResponse>> getMaterialsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(courseMaterialService.findByCourseId(courseId));
    }

    // PUT /api/materials/{id}/download — incrementare downloadCount la descărcare
    @PutMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('PROFESOR', 'FORMATOR', 'ADMIN')")
    public ResponseEntity<CourseMaterialResponse> trackDownload(@PathVariable Long id) {
        return ResponseEntity.ok(courseMaterialService.incrementDownloadCount(id));
    }

    // POST /api/materials — adăugare material (doar FORMATOR sau ADMIN)
    @Override
    @PreAuthorize("hasAnyRole('FORMATOR', 'ADMIN')")
    public ResponseEntity<CourseMaterialResponse> create(@Valid @RequestBody CourseMaterialRequest request) {
        return super.create(request);
    }

    // PUT /api/materials/{id} — editare metadate material (doar FORMATOR sau ADMIN)
    @Override
    @PreAuthorize("hasAnyRole('FORMATOR', 'ADMIN')")
    public ResponseEntity<CourseMaterialResponse> update(@PathVariable Long id,
                                                         @Valid @RequestBody CourseMaterialRequest request) {
        return super.update(id, request);
    }

    // DELETE /api/materials/{id} — ștergere material (doar FORMATOR sau ADMIN)
    @Override
    @PreAuthorize("hasAnyRole('FORMATOR', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return super.delete(id);
    }
}