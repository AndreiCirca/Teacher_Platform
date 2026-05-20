package com.example.TeacherPlatform.controller;

import com.example.TeacherPlatform.dataTransferObject.AuthRequest;
import com.example.TeacherPlatform.dataTransferObject.AuthResponse;
import com.example.TeacherPlatform.dataTransferObject.RegisterRequest;
import com.example.TeacherPlatform.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        return ResponseEntity.ok(Map.of("message", authService.verifyEmail(token)));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(Map.of("message", authService.forgotPassword(payload.get("email"))));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(Map.of("message", authService.resetPassword(payload.get("token"), payload.get("newPassword"))));
    }

    @PostMapping("/refresh")
    @PreAuthorize("isAuthenticated()") // Poate fi chemată doar dacă au un token valid
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(authService.refreshToken(token));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> logout() {
        // Cu JWT (Stateless), logout-ul real se face ștergând token-ul pe Frontend.
        return ResponseEntity.ok(Map.of("message", "Delogare cu succes. Vă rugăm să ștergeți token-ul din client."));
    }
}