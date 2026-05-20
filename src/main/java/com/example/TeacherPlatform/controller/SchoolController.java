package com.example.TeacherPlatform.controller;

import com.example.TeacherPlatform.controller.generic.GenericController;
import com.example.TeacherPlatform.dataTransferObject.SchoolRequest;
import com.example.TeacherPlatform.dataTransferObject.SchoolResponse;
import com.example.TeacherPlatform.model.School;
import com.example.TeacherPlatform.service.SchoolService;
import com.example.TeacherPlatform.service.generic.GenericService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schools")
@RequiredArgsConstructor
public class SchoolController extends GenericController<School, SchoolRequest, SchoolResponse> {

    private final SchoolService schoolService;

    @Override
    protected GenericService<School, SchoolRequest, SchoolResponse> getService() {
        return schoolService;
    }

    // GET /api/schools/county/{county} — Deschis pentru toți utilizatorii autentificați
    @GetMapping("/county/{county}")
    @PreAuthorize("hasAnyAuthority('PROFESOR', 'FORMATOR', 'ADMIN')")
    public ResponseEntity<List<SchoolResponse>> getByCounty(@PathVariable String county) {
        return ResponseEntity.ok(schoolService.findByCounty(county));
    }

    // GET /api/schools/search — Deschis pentru căutare rapidă în formularele de înregistrare/profil
    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('PROFESOR', 'FORMATOR', 'ADMIN')")
    public ResponseEntity<List<SchoolResponse>> search(@RequestParam String name) {
        return ResponseEntity.ok(schoolService.searchByName(name));
    }

    // POST /api/schools/import — Import bulk din Excel
    @PostMapping("/import")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, String>> importSchoolsFromExcel() {
        // În viața reală aici ai primi un @RequestParam("file") MultipartFile file
        // și l-ai parsa cu Apache POI. Conform contractului, trimitem un răspuns mock.
        return ResponseEntity.ok(Map.of(
                "message", "Fișierul Excel a fost procesat. Școlile au fost importate cu succes.",
                "status", "SUCCESS"
        ));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<SchoolResponse> create(@Valid @RequestBody SchoolRequest request) {
        return super.create(request);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<SchoolResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SchoolRequest request) {
        return super.update(id, request);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return super.delete(id);
    }
}