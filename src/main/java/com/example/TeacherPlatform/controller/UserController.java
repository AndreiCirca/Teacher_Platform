package com.example.TeacherPlatform.controller;

import com.example.TeacherPlatform.controller.generic.GenericController;
import com.example.TeacherPlatform.dataTransferObject.UserRequest;
import com.example.TeacherPlatform.dataTransferObject.UserResponse;
import com.example.TeacherPlatform.model.User;
import com.example.TeacherPlatform.service.UserService;
import com.example.TeacherPlatform.service.generic.GenericService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')") // Securizează implicit toate rutele din acest controller pentru ADMIN
public class UserController extends GenericController<User, UserRequest, UserResponse> {

    private final UserService userService;

    @Override
    protected GenericService<User, UserRequest, UserResponse> getService() {
        return userService;
    }

    @Override
    public ResponseEntity<List<UserResponse>> getAll() {
        return super.getAll();
    }

    @Override
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return super.getById(id);
    }

    @Override
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        return super.create(request);
    }

    @Override
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return super.update(id, request);
    }

    @Override
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return super.delete(id);
    }
}