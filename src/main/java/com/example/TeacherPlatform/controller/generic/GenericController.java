package com.example.TeacherPlatform.controller.generic;

import com.example.TeacherPlatform.model.BaseEntity;
import com.example.TeacherPlatform.service.generic.GenericService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
public abstract class GenericController<T extends BaseEntity, REQ, RES> {

    protected abstract GenericService<T, REQ, RES> getService();

    @GetMapping
    public ResponseEntity<List<RES>> getAll() {
        return ResponseEntity.ok(getService().findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RES> getById(@PathVariable Long id) {
        return ResponseEntity.ok(getService().findById(id));
    }

    @PostMapping
    public ResponseEntity<RES> create(@Valid @RequestBody REQ request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(getService().create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RES> update(@PathVariable Long id, @Valid @RequestBody REQ request) {
        return ResponseEntity.ok(getService().update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        getService().delete(id);
        return ResponseEntity.noContent().build();
    }
}