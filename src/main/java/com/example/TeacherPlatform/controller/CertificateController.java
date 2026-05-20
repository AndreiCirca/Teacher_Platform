package com.example.TeacherPlatform.controller;

import com.example.TeacherPlatform.controller.generic.GenericController;
import com.example.TeacherPlatform.dataTransferObject.CertificateRequest;
import com.example.TeacherPlatform.dataTransferObject.CertificateResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.Certificate;
import com.example.TeacherPlatform.model.User;
import com.example.TeacherPlatform.repository.UserRepository;
import com.example.TeacherPlatform.service.CertificateService;
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
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController extends GenericController<Certificate, CertificateRequest, CertificateResponse> {

    private final CertificateService certificateService;
    private final UserRepository userRepository;

    @Override
    protected GenericService<Certificate, CertificateRequest, CertificateResponse> getService() {
        return certificateService;
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('PROFESOR')")
    public ResponseEntity<List<CertificateResponse>> getMyCertificates(Authentication authentication) {
        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
        return ResponseEntity.ok(certificateService.findMyCertificates(currentUser.getId()));
    }

    @GetMapping("/verify/{code}")
    public ResponseEntity<CertificateResponse> verifyCertificate(@PathVariable String code) {
        return ResponseEntity.ok(certificateService.verifyCertificate(code));
    }

    @PostMapping("/courses/{courseId}/generate")
    @PreAuthorize("hasAnyAuthority('FORMATOR', 'ADMIN')")
    public ResponseEntity<List<CertificateResponse>> generateBulk(@PathVariable Long courseId) {
        return ResponseEntity.ok(certificateService.generateBulkCertificatesForCourse(courseId));
    }

    @PutMapping("/{id}/revoke")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CertificateResponse> revokeCertificate(@PathVariable Long id) {
        return ResponseEntity.ok(certificateService.revokeCertificate(id));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAuthority('PROFESOR')")
    public ResponseEntity<CertificateResponse> downloadCertificate(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(certificateService.downloadCertificate(id, authentication.getName()));
    }

    @GetMapping("/admin/stats")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(certificateService.getCertificateStats());
    }

    @GetMapping("/admin/export")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> exportCertificates() {
        return ResponseEntity.ok("Export Excel generat cu succes.");
    }

    @Override
    @PreAuthorize("hasAnyAuthority('FORMATOR', 'ADMIN')")
    public ResponseEntity<CertificateResponse> create(@Valid @RequestBody CertificateRequest request) {
        return super.create(request);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CertificateResponse> update(@PathVariable Long id, @Valid @RequestBody CertificateRequest request) {
        return super.update(id, request);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return super.delete(id);
    }
}