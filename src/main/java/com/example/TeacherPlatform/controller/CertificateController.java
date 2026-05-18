package com.example.TeacherPlatform.controller;

import com.example.TeacherPlatform.controller.generic.GenericController;
import com.example.TeacherPlatform.dataTransferObject.CertificateRequest;
import com.example.TeacherPlatform.dataTransferObject.CertificateResponse;
import com.example.TeacherPlatform.model.Certificate;
import com.example.TeacherPlatform.service.CertificateService;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController extends GenericController<Certificate, CertificateRequest, CertificateResponse> {

    private final CertificateService certificateService;

    @Override
    protected GenericService<Certificate, CertificateRequest, CertificateResponse> getService() {
        return certificateService;
    }
}