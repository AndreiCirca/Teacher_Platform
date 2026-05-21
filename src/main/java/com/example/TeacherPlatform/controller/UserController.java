package com.example.TeacherPlatform.controller;

import com.example.TeacherPlatform.controller.generic.GenericController;
import com.example.TeacherPlatform.dataTransferObject.PasswordResetRequestResponse;
import com.example.TeacherPlatform.dataTransferObject.UserRequest;
import com.example.TeacherPlatform.dataTransferObject.UserResponse;
import com.example.TeacherPlatform.model.User;
import com.example.TeacherPlatform.service.UserService;
import com.example.TeacherPlatform.service.generic.GenericService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController extends GenericController<User, UserRequest, UserResponse> {

    private final UserService userService;

    @Override
    protected GenericService<User, UserRequest, UserResponse> getService() {
        return userService;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getMyProfile(authentication));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateMyProfile(@Valid @RequestBody UserRequest request, Authentication authentication) {
        return ResponseEntity.ok(userService.updateMyProfile(request, authentication));
    }

    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changeMyPassword(@RequestBody Map<String, String> payload, Authentication authentication) {
        userService.changeMyPassword(payload.get("oldPassword"), payload.get("newPassword"), authentication);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/avatar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateMyAvatar(@RequestBody Map<String, String> payload, Authentication authentication) {
        return ResponseEntity.ok(userService.updateMyAvatar(payload.get("avatarUrl"), authentication));
    }

    @PutMapping("/{id}/toggle-active")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserResponse> toggleActiveStatus(@PathVariable Long id) {
        return ResponseEntity.ok(userService.toggleActiveStatus(id));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody Map<String, String> payload) {
        userService.createResetRequest(payload.get("email"));
        return ResponseEntity.noContent().build(); // always 204 — don't reveal if email exists
    }

    @GetMapping("/reset-requests")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<PasswordResetRequestResponse>> getPendingResetRequests() {
        return ResponseEntity.ok(userService.getPendingResetRequests());
    }

    @PutMapping("/reset-requests/{id}/resolve")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> resolveResetRequest(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        userService.resolveResetRequest(id, payload.get("newPassword"));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        userService.resetPassword(id, payload.get("newPassword"));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/unverified")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<UserResponse>> getUnverifiedUsers() {
        return ResponseEntity.ok(userService.findUnverifiedUsers());
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, Long>> getUserStats() {
        return ResponseEntity.ok(userService.getUserStats());
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAll() { return super.getAll(); }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) { return super.getById(id); }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) { return super.create(request); }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserRequest request) { return super.update(id, request); }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) { return super.delete(id); }
}