package com.example.TeacherPlatform.controller;

import com.example.TeacherPlatform.controller.generic.GenericController;
import com.example.TeacherPlatform.dataTransferObject.CertificateRequest;
import com.example.TeacherPlatform.dataTransferObject.CertificateResponse;
import com.example.TeacherPlatform.model.Certificate;
import com.example.TeacherPlatform.service.CertificateService;
import com.example.TeacherPlatform.service.generic.GenericService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController extends GenericController<Certificate, CertificateRequest, CertificateResponse> {

    private final CertificateService certificateService;

    @Override
    protected GenericService<Certificate, CertificateRequest, CertificateResponse> getService() {
        return certificateService;
    }

    // GET /api/certificates/my — Certificatele profesorului logat (PROFESOR)
    @GetMapping("/my")
    @PreAuthorize("hasRole('PROFESOR')")
    public ResponseEntity<List<CertificateResponse>> getMyCertificates() {
        // Înlocuiește 1L cu ID-ul utilizatorului extras din token-ul de autentificare JWT ulterior
        Long currentTeacherId = 1L;
        return ResponseEntity.ok(certificateService.findMyCertificates(currentTeacherId));
    }

    // GET /api/certificates/verify/{code} — Validare publică certificat (Public)
    @GetMapping("/verify/{code}")
    public ResponseEntity<CertificateResponse> verifyCertificate(@PathVariable String code) {
        return ResponseEntity.ok(certificateService.verifyCertificate(code));
    }

    // PUT /api/certificates/{id}/revoke — Revocare certificat de către administrator (ADMIN)
    @PutMapping("/{id}/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CertificateResponse> revokeCertificate(@PathVariable Long id) {
        return ResponseEntity.ok(certificateService.revokeCertificate(id));
    }

    // POST /api/certificates — Generare certificat (FORMATOR sau ADMIN)
    @Override
    @PreAuthorize("hasAnyRole('FORMATOR', 'ADMIN')")
    public ResponseEntity<CertificateResponse> create(@Valid @RequestBody CertificateRequest request) {
        return super.create(request);
    }

    // PUT /api/certificates/{id} — Modificare metadate certificat (ADMIN)
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CertificateResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody CertificateRequest request) {
        return super.update(id, request);
    }

    // DELETE /api/certificates/{id} — Ștergere certificat (ADMIN)
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return super.delete(id);
    }
}