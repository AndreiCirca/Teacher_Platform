package com.example.TeacherPlatform.controller;

import com.example.TeacherPlatform.controller.generic.GenericController;
import com.example.TeacherPlatform.dataTransferObject.CourseCategoryRequest;
import com.example.TeacherPlatform.dataTransferObject.CourseCategoryResponse;
import com.example.TeacherPlatform.model.CourseCategory;
import com.example.TeacherPlatform.service.CourseCategoryService;
import com.example.TeacherPlatform.service.generic.GenericService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CourseCategoryController extends GenericController<CourseCategory, CourseCategoryRequest, CourseCategoryResponse> {

    private final CourseCategoryService courseCategoryService;

    @Override
    protected GenericService<CourseCategory, CourseCategoryRequest, CourseCategoryResponse> getService() {
        return courseCategoryService;
    }

    // GET /api/categories — categorii active (toți)
    @Override
    public ResponseEntity<List<CourseCategoryResponse>> getAll() {
        return ResponseEntity.ok(courseCategoryService.findAllActive());
    }

    // GET /api/categories/all — toate categoriile (ADMIN)
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CourseCategoryResponse>> getAllIncludingInactive() {
        return ResponseEntity.ok(courseCategoryService.findAll());
    }

    // PUT /api/categories/{id}/toggle-active (ADMIN)
    @PutMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseCategoryResponse> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(courseCategoryService.toggleActive(id));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseCategoryResponse> create(@Valid @RequestBody CourseCategoryRequest request) {
        return super.create(request);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseCategoryResponse> update(@PathVariable Long id,
                                                         @Valid @RequestBody CourseCategoryRequest request) {
        return super.update(id, request);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return super.delete(id);
    }
}