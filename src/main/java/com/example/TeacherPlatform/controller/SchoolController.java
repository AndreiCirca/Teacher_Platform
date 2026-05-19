package com.example.TeacherPlatform.controller;

import com.example.TeacherPlatform.controller.generic.GenericController;
import com.example.TeacherPlatform.dataTransferObject.SchoolRequest;
import com.example.TeacherPlatform.dataTransferObject.SchoolResponse;
import com.example.TeacherPlatform.model.School;
import com.example.TeacherPlatform.service.SchoolService;
import com.example.TeacherPlatform.service.generic.GenericService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schools")
@RequiredArgsConstructor
public class SchoolController extends GenericController<School, SchoolRequest, SchoolResponse> {

    private final SchoolService schoolService;

    @Override
    protected GenericService<School, SchoolRequest, SchoolResponse> getService() {
        return schoolService;
    }


    @GetMapping("/county/{county}")
    public ResponseEntity<List<SchoolResponse>> getByCounty(@PathVariable String county) {
        return ResponseEntity.ok(schoolService.findByCounty(county));
    }

    @GetMapping("/search")
    public ResponseEntity<List<SchoolResponse>> search(@RequestParam String name) {
        return ResponseEntity.ok(schoolService.searchByName(name));
    }


    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SchoolResponse> create(@Valid @RequestBody SchoolRequest request) {
        return super.create(request);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SchoolResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody SchoolRequest request) {
        return super.update(id, request);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return super.delete(id);
    }
}