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

    /**
     * CORECTIE CRITICA DE SECURITATE:
     * Versiunea originala folosea "Long currentTeacherId = 1L" — orice profesor autentificat
     * vedea certificatele profesorului cu ID=1, indiferent de cine era logat.
     * Acum extragem ID-ul real din JWT prin obiectul Authentication.
     */
    @GetMapping("/certificates/my")
    @PreAuthorize("hasAuthority('PROFESOR')")
    public ResponseEntity<List<CertificateResponse>> getMyCertificates(Authentication authentication) {
        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
        return ResponseEntity.ok(certificateService.findMyCertificates(currentUser.getId()));
    }

    // GET /api/certificates/verify/{code} — Validare publica (fara token obligatoriu)
    // CORECTIE: Returneaza acum detalii complete (Nume profesor, Curs, Ore, Data) nu doar un mesaj,
    // conform cerintei UI din specificatiile Gemini.
    @GetMapping("/certificates/verify/{code}")
    public ResponseEntity<CertificateResponse> verifyCertificate(@PathVariable String code) {
        return ResponseEntity.ok(certificateService.verifyCertificate(code));
    }

    // POST /api/courses/{courseId}/certificates/generate
    @PostMapping("/courses/{courseId}/certificates/generate")
    @PreAuthorize("hasAnyAuthority('FORMATOR', 'ADMIN')")
    public ResponseEntity<List<CertificateResponse>> generateBulk(@PathVariable Long courseId) {
        return ResponseEntity.ok(certificateService.generateBulkCertificatesForCourse(courseId));
    }

    // PUT /api/certificates/{id}/revoke
    @PutMapping("/certificates/{id}/revoke")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CertificateResponse> revokeCertificate(@PathVariable Long id) {
        return ResponseEntity.ok(certificateService.revokeCertificate(id));
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