package com.example.TeacherPlatform.controller;

import com.example.TeacherPlatform.controller.generic.GenericController;
import com.example.TeacherPlatform.dataTransferObject.CourseMaterialRequest;
import com.example.TeacherPlatform.dataTransferObject.CourseMaterialResponse;
import com.example.TeacherPlatform.model.CourseMaterial;
import com.example.TeacherPlatform.service.CourseMaterialService;
import com.example.TeacherPlatform.service.generic.GenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/course-materials")
@RequiredArgsConstructor
public class CourseMaterialController extends GenericController<CourseMaterial, CourseMaterialRequest, CourseMaterialResponse> {

    private final CourseMaterialService courseMaterialService;

    @Override
    protected GenericService<CourseMaterial, CourseMaterialRequest, CourseMaterialResponse> getService() {
        return courseMaterialService;
    }
}