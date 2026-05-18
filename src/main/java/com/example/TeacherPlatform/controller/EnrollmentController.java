package com.example.TeacherPlatform.controller;

import com.example.TeacherPlatform.controller.generic.GenericController;
import com.example.TeacherPlatform.dataTransferObject.EnrollmentRequest;
import com.example.TeacherPlatform.dataTransferObject.EnrollmentResponse;
import com.example.TeacherPlatform.model.Enrollment;
import com.example.TeacherPlatform.service.EnrollmentService;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController extends GenericController<Enrollment, EnrollmentRequest, EnrollmentResponse> {

    private final EnrollmentService enrollmentService;

    @Override
    protected GenericService<Enrollment, EnrollmentRequest, EnrollmentResponse> getService() {
        return enrollmentService;
    }
}